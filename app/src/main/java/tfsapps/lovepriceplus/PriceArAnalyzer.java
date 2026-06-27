package tfsapps.lovepriceplus;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.media.Image;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PriceArAnalyzer
 *
 * CameraXのImageAnalysis.Analyzerとして、フレームごとにML Kit日本語OCRを実行し
 * 「金額」と「容量」を正規表現で抽出するクラス。
 *
 * 金額抽出ルール:
 *   - 数字の後に「円」「¥」が続く (例: 198円, ¥298)
 *   - 「税抜」「税別」「本体価格」が数字と「円」の間に挟まる場合 (例: 898 税抜円)
 *   - 同一行に「税込」「本体価格」「税抜」を含む場合の数字
 *
 * 容量抽出ルール:
 *   - 数字の後に「g」「グラム」「ml」「ミリリットル」「個」「本」「枚」「袋」「食」「人前」が続く
 */
public class PriceArAnalyzer implements ImageAnalysis.Analyzer {

    private static final String TAG = "PriceArAnalyzer";

    // ── 抽出結果ボックス ──────────────────────────────────────────────────────

    /** OCR認識されたテキストの種類と位置 */
    public static class DetectedBox {
        /** ML Kit座標系でのBoundingBox */
        public final Rect  rect;
        /** true = 金額、false = 容量 */
        public final boolean isPrice;
        /** 抽出した数値 */
        public final int   value;

        DetectedBox(@NonNull Rect rect, boolean isPrice, int value) {
            this.rect    = rect;
            this.isPrice = isPrice;
            this.value   = value;
        }
    }

    // ── 正規表現パターン ──────────────────────────────────────────────────────

    /**
     * 金額: 数字 + (税抜|税別|本体価格)? + 円
     * 例: 198円, 1,280円, 898 税抜円, 1,480税別円
     */
    private static final Pattern PRICE_SUFFIX_YEN =
            Pattern.compile("([1-9][\\d,]{0,6})\\s*(?:税抜|税別|本体価格)?\\s*円");

    /** 金額: ¥/￥ + 数字 (例: ¥198, ￥1280) */
    private static final Pattern PRICE_PREFIX_YEN =
            Pattern.compile("[¥￥]\\s*([1-9][\\d,]{0,6})");

    /** 税込/税抜/本体価格 が同行にある場合の数字抽出 */
    private static final Pattern PRICE_TAX_LINE =
            Pattern.compile("([1-9][\\d,]{0,6})");

    /** 容量: 数字 + 単位 */
    private static final Pattern VOLUME_PATTERN =
            Pattern.compile("([1-9]\\d{0,5})\\s*" +
                    "(?:g|ｇ|Ｇ|グラム|ml|ｍｌ|ミリリットル|mL|ｍＬ|㎖|㎝|cc|ｃｃ|" +
                    "個|本|枚|袋|食|人前|パック|切)");

    /** 金額認識ライン判定: 税関連キーワード */
    private static final Pattern TAX_KEYWORDS =
            Pattern.compile("税込|税抜|本体価格|税別|消費税");

    /**
     * 日付パターン: このパターンを含む行は金額抽出をスキップする。
     * 例: 「6月30日」の「30」を価格として誤認識するのを防ぐ。
     */
    private static final Pattern DATE_PATTERN =
            Pattern.compile("\\d+月\\d+日");

    // ── コールバック ──────────────────────────────────────────────────────────

    /**
     * フレーム解析完了後に呼ばれるコールバック。
     * ML Kit完了スレッドで呼ばれるため、UI更新はrunOnUiThreadが必要。
     */
    public interface PriceCallback {
        /**
         * @param price    検出した金額（null = 未検出）
         * @param volume   検出した容量（null = 未検出）
         * @param boxes    AR描画用ボックスリスト
         * @param imageWidth   ImageProxy幅（センサー向き）
         * @param imageHeight  ImageProxy高さ（センサー向き）
         * @param rotationDegrees CameraX回転角 (0/90/180/270)
         */
        void onDetected(
                Integer              price,
                Integer              volume,
                List<DetectedBox>    boxes,
                int                  imageWidth,
                int                  imageHeight,
                int                  rotationDegrees
        );
    }

    // ── フィールド ────────────────────────────────────────────────────────────

    private final TextRecognizer mRecognizer;
    private final PriceCallback  mCallback;

    // ── コンストラクタ ─────────────────────────────────────────────────────────

    public PriceArAnalyzer(@NonNull PriceCallback callback) {
        mRecognizer = TextRecognition.getClient(
                new JapaneseTextRecognizerOptions.Builder().build());
        mCallback = callback;
    }

    // ── ImageAnalysis.Analyzer ────────────────────────────────────────────────

    @Override
    @SuppressLint("UnsafeOptInUsageError")
    public void analyze(@NonNull ImageProxy imageProxy) {
        Image mediaImage = imageProxy.getImage();
        if (mediaImage == null) {
            imageProxy.close();
            return;
        }

        final int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
        final int imageWidth      = imageProxy.getWidth();
        final int imageHeight     = imageProxy.getHeight();

        InputImage inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees);

        mRecognizer.process(inputImage)
                .addOnSuccessListener(visionText ->
                        processResult(visionText, imageWidth, imageHeight, rotationDegrees))
                .addOnFailureListener(e ->
                        Log.w(TAG, "OCR failed: " + e.getMessage()))
                .addOnCompleteListener(task ->
                        imageProxy.close()); // 必ずcloseしてCameraXの次フレームをアンブロック
    }

    // ── 結果処理 ──────────────────────────────────────────────────────────────

    private void processResult(@NonNull Text visionText,
                               int imageWidth, int imageHeight, int rotationDegrees) {

        Integer bestPrice  = null;
        Integer bestVolume = null;
        List<DetectedBox> boxes = new ArrayList<>();

        for (Text.TextBlock block : visionText.getTextBlocks()) {

            // ── ブロック全体テキストでも検索 ────────────────────────────
            // 大きな値段数字と「円」が別Lineに分かれて認識されるケースに対応
            StringBuilder blockBuilder = new StringBuilder();
            for (Text.Line line : block.getLines()) {
                blockBuilder.append(line.getText()).append(" ");
            }
            String blockText = blockBuilder.toString().trim();
            Integer blockPrice = extractPrice(blockText);
            if (blockPrice != null && (bestPrice == null || blockPrice > bestPrice)) {
                bestPrice = blockPrice;
            }

            for (Text.Line line : block.getLines()) {
                String lineText = line.getText();

                // ── 金額判定 ──────────────────────────────────────────────
                Integer linePrice = extractPrice(lineText);
                if (linePrice != null) {
                    if (bestPrice == null || linePrice > bestPrice) {
                        bestPrice = linePrice;
                    }
                    Rect box = getPriceBoundingBox(line, lineText);
                    if (box != null) {
                        boxes.add(new DetectedBox(new Rect(box), true, linePrice));
                    }
                }

                // ── 容量判定 ──────────────────────────────────────────────
                Integer lineVolume = extractVolume(lineText);
                if (lineVolume != null) {
                    if (bestVolume == null || lineVolume > bestVolume) {
                        bestVolume = lineVolume;
                    }
                    Rect box = getVolumeBoundingBox(line, lineText);
                    if (box != null) {
                        boxes.add(new DetectedBox(new Rect(box), false, lineVolume));
                    }
                }
            }
        }

        mCallback.onDetected(bestPrice, bestVolume, boxes, imageWidth, imageHeight, rotationDegrees);
    }

    // ── 金額抽出 ──────────────────────────────────────────────────────────────

    /**
     * 1行のテキストから金額を抽出する。
     * 優先順位: ①「円」サフィックス ②「¥/￥」プレフィックス ③税関連キーワード行
     */
    static Integer extractPrice(@NonNull String text) {
        // 日付パターン（例: 6月30日）を含む行は除外
        if (DATE_PATTERN.matcher(text).find()) return null;

        // ① 数字 + (税抜|税別)? + 円
        Matcher m = PRICE_SUFFIX_YEN.matcher(text);
        Integer candidate = null;
        while (m.find()) {
            try {
                int val = Integer.parseInt(m.group(1).replace(",", ""));
                if (isPriceRange(val)) {
                    if (candidate == null || val > candidate) candidate = val;
                }
            } catch (NumberFormatException ignored) {}
        }
        if (candidate != null) return candidate;

        // ② ¥/￥ + 数字
        m = PRICE_PREFIX_YEN.matcher(text);
        while (m.find()) {
            try {
                int val = Integer.parseInt(m.group(1).replace(",", ""));
                if (isPriceRange(val)) {
                    if (candidate == null || val > candidate) candidate = val;
                }
            } catch (NumberFormatException ignored) {}
        }
        if (candidate != null) return candidate;

        // ③ 税込/税抜/本体価格 が同行にある
        if (TAX_KEYWORDS.matcher(text).find()) {
            m = PRICE_TAX_LINE.matcher(text);
            while (m.find()) {
                try {
                    int val = Integer.parseInt(m.group(1).replace(",", ""));
                    if (isPriceRange(val)) {
                        if (candidate == null || val > candidate) candidate = val;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return candidate;
    }

    /** 容量抽出 */
    static Integer extractVolume(@NonNull String text) {
        Matcher m = VOLUME_PATTERN.matcher(text);
        Integer candidate = null;
        while (m.find()) {
            try {
                int val = Integer.parseInt(m.group(1));
                if (isVolumeRange(val)) {
                    if (candidate == null || val > candidate) candidate = val;
                }
            } catch (NumberFormatException ignored) {}
        }
        return candidate;
    }

    /** 金額として妥当な範囲: 100〜999999円 */
    private static boolean isPriceRange(int val) {
        return val >= 100 && val <= 999999;
    }

    /** 容量として妥当な範囲: 1〜99999 */
    private static boolean isVolumeRange(int val) {
        return val >= 1 && val <= 99999;
    }

    // ── BoundingBox取得 ───────────────────────────────────────────────────────

    private static Rect getPriceBoundingBox(@NonNull Text.Line line, @NonNull String lineText) {
        for (Text.Element element : line.getElements()) {
            String t = element.getText();
            if (PRICE_SUFFIX_YEN.matcher(t).find()
                    || PRICE_PREFIX_YEN.matcher(t).find()
                    || (TAX_KEYWORDS.matcher(lineText).find() && PRICE_TAX_LINE.matcher(t).find())) {
                return element.getBoundingBox();
            }
        }
        return line.getBoundingBox();
    }

    private static Rect getVolumeBoundingBox(@NonNull Text.Line line, @NonNull String lineText) {
        for (Text.Element element : line.getElements()) {
            if (VOLUME_PATTERN.matcher(element.getText()).find()) {
                return element.getBoundingBox();
            }
        }
        return line.getBoundingBox();
    }

    // ── クリーンアップ ────────────────────────────────────────────────────────

    /** TextRecognizerを解放する。Activity.onDestroy()から呼ぶこと。 */
    public void shutdown() {
        mRecognizer.close();
    }
}

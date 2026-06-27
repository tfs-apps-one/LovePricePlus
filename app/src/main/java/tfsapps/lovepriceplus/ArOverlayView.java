package tfsapps.lovepriceplus;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * ArOverlayView — AR一発入力モード用オーバーレイ
 *
 * ML Kitが認識したテキスト（金額・容量）のBoundingBoxの上に、
 * 半透明の緑色ベタ塗りをCanvas描画する。
 *
 * 金額 → 薄い緑（#80 00C853）
 * 容量 → 薄い青緑（#80 00BCD4）
 */
public class ArOverlayView extends View {

    // ── ペイント ──────────────────────────────────────────────────────────────
    private final Paint mPriceFill;
    private final Paint mPriceStroke;
    private final Paint mVolumeFill;
    private final Paint mVolumeStroke;
    private final Paint mLabelBgPaint;
    private final Paint mLabelTextPaint;

    // ── 状態 ──────────────────────────────────────────────────────────────────
    private List<PriceArAnalyzer.DetectedBox> mBoxes      = new ArrayList<>();
    private int                               mImageWidth  = 0;
    private int                               mImageHeight = 0;
    private int                               mRotation    = 0;

    // ── コンストラクタ ─────────────────────────────────────────────────────────

    public ArOverlayView(Context context) {
        super(context);
        mPriceFill    = makePriceFillPaint();
        mPriceStroke  = makePriceStrokePaint();
        mVolumeFill   = makeVolumeFillPaint();
        mVolumeStroke = makeVolumeStrokePaint();
        mLabelBgPaint = makeLabelBgPaint();
        mLabelTextPaint = makeLabelTextPaint(context);
    }

    public ArOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPriceFill    = makePriceFillPaint();
        mPriceStroke  = makePriceStrokePaint();
        mVolumeFill   = makeVolumeFillPaint();
        mVolumeStroke = makeVolumeStrokePaint();
        mLabelBgPaint = makeLabelBgPaint();
        mLabelTextPaint = makeLabelTextPaint(context);
    }

    public ArOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPriceFill    = makePriceFillPaint();
        mPriceStroke  = makePriceStrokePaint();
        mVolumeFill   = makeVolumeFillPaint();
        mVolumeStroke = makeVolumeStrokePaint();
        mLabelBgPaint = makeLabelBgPaint();
        mLabelTextPaint = makeLabelTextPaint(context);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public void setResults(@NonNull List<PriceArAnalyzer.DetectedBox> boxes,
                           int imageWidth, int imageHeight, int rotationDegrees) {
        mBoxes       = new ArrayList<>(boxes);
        mImageWidth  = imageWidth;
        mImageHeight = imageHeight;
        mRotation    = rotationDegrees;
        invalidate();
    }

    public void clear() {
        mBoxes.clear();
        invalidate();
    }

    // ── 描画 ──────────────────────────────────────────────────────────────────

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final float vW = getWidth();
        final float vH = getHeight();
        if (vW == 0 || vH == 0 || mImageWidth <= 0 || mImageHeight <= 0) return;

        for (PriceArAnalyzer.DetectedBox box : mBoxes) {
            RectF v = transformRect(box.rect, mImageWidth, mImageHeight, mRotation, vW, vH);
            if (v == null) continue;

            if (box.isPrice) {
                canvas.drawRect(v, mPriceFill);
                canvas.drawRect(v, mPriceStroke);
                drawLabel(canvas, v, "金額", mPriceStroke.getColor());
            } else {
                canvas.drawRect(v, mVolumeFill);
                canvas.drawRect(v, mVolumeStroke);
                drawLabel(canvas, v, "容量", mVolumeStroke.getColor());
            }
        }
    }

    private void drawLabel(Canvas canvas, RectF v, String label, int strokeColor) {
        float textW  = mLabelTextPaint.measureText(label);
        float textH  = mLabelTextPaint.getTextSize();
        float pad    = 4f;
        float bgL    = v.left;
        float bgT    = Math.max(0f, v.top - textH - pad * 2f);
        float bgR    = bgL + textW + pad * 2f;
        float bgB    = bgT + textH + pad * 2f;

        canvas.drawRect(bgL, bgT, bgR, bgB, mLabelBgPaint);

        int savedColor = mLabelTextPaint.getColor();
        mLabelTextPaint.setColor(strokeColor);
        canvas.drawText(label, bgL + pad, bgB - pad - mLabelTextPaint.descent(), mLabelTextPaint);
        mLabelTextPaint.setColor(savedColor);
    }

    // ── 座標変換 ──────────────────────────────────────────────────────────────

    private static RectF transformRect(Rect r,
                                       int imgW, int imgH, int rot,
                                       float vW, float vH) {
        float effectiveW = (rot == 90 || rot == 270) ? imgH : imgW;
        float effectiveH = (rot == 90 || rot == 270) ? imgW : imgH;

        float scale   = Math.max(vW / effectiveW, vH / effectiveH);
        float offsetX = (vW - effectiveW * scale) / 2f;
        float offsetY = (vH - effectiveH * scale) / 2f;

        float l  = r.left   * scale + offsetX;
        float t  = r.top    * scale + offsetY;
        float ri = r.right  * scale + offsetX;
        float b  = r.bottom * scale + offsetY;

        l  = Math.max(0f, l);
        t  = Math.max(0f, t);
        ri = Math.min(vW, ri);
        b  = Math.min(vH, b);

        if (l >= ri || t >= b) return null;
        return new RectF(l, t, ri, b);
    }

    // ── Paintファクトリ ───────────────────────────────────────────────────────

    private static Paint makePriceFillPaint() {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.argb(100, 0, 200, 83));
        return p;
    }

    private static Paint makePriceStrokePaint() {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(Color.rgb(0, 200, 83));
        p.setStrokeWidth(3.5f);
        return p;
    }

    private static Paint makeVolumeFillPaint() {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.argb(100, 0, 188, 212));
        return p;
    }

    private static Paint makeVolumeStrokePaint() {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(Color.rgb(0, 188, 212));
        p.setStrokeWidth(3.5f);
        return p;
    }

    private static Paint makeLabelBgPaint() {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.argb(180, 0, 0, 0));
        return p;
    }

    private static Paint makeLabelTextPaint(Context context) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.WHITE);
        float sp12 = 12f * context.getResources().getDisplayMetrics().scaledDensity;
        p.setTextSize(sp12);
        p.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        return p;
    }
}

package tfsapps.lovepriceplus;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.core.resolutionselector.AspectRatioStrategy;
import androidx.camera.core.resolutionselector.ResolutionSelector;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ArCameraActivity — AR一発入力モード
 *
 * 2ステップのスキャンフロー:
 *   STEP 1: 商品Aの値札/ラベルをスキャン → 金額・容量を認識
 *   STEP 2: 商品Bの値札/ラベルをスキャン → 金額・容量を認識
 *
 * 両方のステップ完了後、自動でカメラを閉じ、
 * MainActivity へ Intent で結果を返す。
 *
 * Intentキー:
 *   EXTRA_PRICE_A  商品Aの金額(int, -1=未取得)
 *   EXTRA_VOLUME_A 商品Aの容量(int, -1=未取得)
 *   EXTRA_PRICE_B  商品Bの金額(int, -1=未取得)
 *   EXTRA_VOLUME_B 商品Bの容量(int, -1=未取得)
 */
public class ArCameraActivity extends AppCompatActivity
        implements PriceArAnalyzer.PriceCallback {

    private static final String TAG = "ArCameraActivity";

    // ── Intent キー（public: MainActivity から参照） ──────────────────────────
    public static final String EXTRA_PRICE_A  = "price_a";
    public static final String EXTRA_VOLUME_A = "volume_a";
    public static final String EXTRA_PRICE_B  = "price_b";
    public static final String EXTRA_VOLUME_B = "volume_b";

    // ── カメラ権限 ────────────────────────────────────────────────────────────
    private static final int CAMERA_PERMISSION_REQUEST = 2001;

    // ── スキャンステップ ───────────────────────────────────────────────────────
    private static final int STEP_A = 0;
    private static final int STEP_B = 1;

    /**
     * 連続何フレーム同じ値を検出したら「確定」とするか。
     * 3フレーム ≈ 100ms @ 30fps
     */
    private static final int STABILITY_REQUIRED = 3;

    /**
     * 認識値が確定してから次のステップへ自動遷移するまでの待機時間(ms)。
     */
    private static final long AUTO_ADVANCE_DELAY_MS = 800L;

    /**
     * STEP A確定後、スキャンを一時停止する時間(ms)。
     */
    private static final long SCAN_PAUSE_MS = 1000L;

    // ── Views ─────────────────────────────────────────────────────────────────
    private PreviewView  mPreviewView;
    private ArOverlayView mOverlayView;
    private TextView     mStepLabel;
    private TextView     mNavText;
    private TextView     mPriceStatus;
    private TextView     mVolumeStatus;
    private Button       mCloseBtn;
    private Button       mNextBtn;
    private Button       mRescanBtn;
    private android.view.View mReadyOverlay;
    private AdView            mAdView;

    // ── CameraX ───────────────────────────────────────────────────────────────
    private ExecutorService  mCameraExecutor;
    private PriceArAnalyzer  mAnalyzer;
    private ProcessCameraProvider mCameraProvider;

    // ── スキャン状態 ──────────────────────────────────────────────────────────
    private volatile int mStep = STEP_A;
    private boolean      mResultSent = false;
    private volatile boolean mScanningPaused = false;

    private Integer mLastPrice  = null;
    private Integer mLastVolume = null;
    private int     mPriceStability  = 0;
    private int     mVolumeStability = 0;

    private Integer mConfirmedPrice  = null;
    private Integer mConfirmedVolume = null;

    private int mResultPriceA  = -1;
    private int mResultVolumeA = -1;
    private int mResultPriceB  = -1;
    private int mResultVolumeB = -1;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    // ── ライフサイクル ────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar_camera);

        mPreviewView  = findViewById(R.id.ar_preview_view);
        mOverlayView  = findViewById(R.id.ar_overlay_view);
        mStepLabel    = findViewById(R.id.ar_step_label);
        mNavText      = findViewById(R.id.ar_nav_text);
        mPriceStatus  = findViewById(R.id.ar_price_status);
        mVolumeStatus = findViewById(R.id.ar_volume_status);
        mCloseBtn     = findViewById(R.id.ar_close_btn);
        mNextBtn      = findViewById(R.id.ar_next_btn);
        mRescanBtn    = findViewById(R.id.ar_rescan_btn);
        mReadyOverlay = findViewById(R.id.ar_ready_overlay);

        // バナー広告ロード
        mAdView = findViewById(R.id.ar_adView);
        mAdView.loadAd(new AdRequest.Builder().build());

        mCameraExecutor = Executors.newSingleThreadExecutor();

        mCloseBtn.setOnClickListener(v -> cancelAndFinish());

        mNextBtn.setOnClickListener(v -> {
            mHandler.removeCallbacks(mAdvanceRunnable);
            mAdvanceRunnable.run();
        });

        mRescanBtn.setOnClickListener(v -> rescanCurrentStep());

        updateUI();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        mCameraExecutor.shutdown();
        if (mAnalyzer != null) mAnalyzer.shutdown();
    }

    // ── CameraX セットアップ ──────────────────────────────────────────────────

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(this);

        future.addListener(() -> {
            try {
                mCameraProvider = future.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(mPreviewView.getSurfaceProvider());

                ResolutionSelector resolutionSelector = new ResolutionSelector.Builder()
                        .setAspectRatioStrategy(
                                AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
                        .build();

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setResolutionSelector(resolutionSelector)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                mAnalyzer = new PriceArAnalyzer(this);
                imageAnalysis.setAnalyzer(mCameraExecutor, mAnalyzer);

                mCameraProvider.unbindAll();
                mCameraProvider.bindToLifecycle(
                        this,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis);

                Log.d(TAG, "Camera started");

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Camera start failed", e);
                Toast.makeText(this, "カメラの起動に失敗しました", Toast.LENGTH_SHORT).show();
                finish();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void stopCamera() {
        if (mCameraProvider != null) {
            mCameraProvider.unbindAll();
        }
    }

    // ── PriceArAnalyzer.PriceCallback ────────────────────────────────────────

    @Override
    public void onDetected(Integer price, Integer volume,
                           List<PriceArAnalyzer.DetectedBox> boxes,
                           int imageWidth, int imageHeight, int rotationDegrees) {

        if (mResultSent) return;
        if (mScanningPaused) return;

        runOnUiThread(() ->
                mOverlayView.setResults(boxes, imageWidth, imageHeight, rotationDegrees));

        runOnUiThread(() -> updateStability(price, volume));
    }

    // ── 安定性フィルタとステート管理 ─────────────────────────────────────────

    private void updateStability(Integer price, Integer volume) {
        if (mResultSent) return;

        // 金額安定性
        if (price != null && isSimilarValue(price, mLastPrice)) {
            mPriceStability++;
            mLastPrice = price;
        } else {
            mPriceStability = (price != null) ? 1 : 0;
            mLastPrice = price;
        }

        // 容量安定性
        if (volume != null && isSimilarValue(volume, mLastVolume)) {
            mVolumeStability++;
            mLastVolume = volume;
        } else {
            mVolumeStability = (volume != null) ? 1 : 0;
            mLastVolume = volume;
        }

        // 確定判定
        if (mPriceStability >= STABILITY_REQUIRED && mConfirmedPrice == null) {
            mConfirmedPrice = mLastPrice;
            Log.d(TAG, "Price confirmed: " + mConfirmedPrice + " (step=" + mStep + ")");
        }
        if (mVolumeStability >= STABILITY_REQUIRED && mConfirmedVolume == null) {
            mConfirmedVolume = mLastVolume;
            Log.d(TAG, "Volume confirmed: " + mConfirmedVolume + " (step=" + mStep + ")");
        }

        updateStatusText();

        if (mConfirmedPrice != null) {
            mNextBtn.setVisibility(android.view.View.VISIBLE);
            mRescanBtn.setVisibility(android.view.View.VISIBLE);
        }
    }

    private static boolean isSimilarValue(Integer a, Integer b) {
        if (a == null || b == null) return false;
        if (a.equals(b)) return true;
        int diff = Math.abs(a - b);
        int base = Math.min(a, b);
        return diff <= Math.max(1, base / 20);
    }

    private final Runnable mAdvanceRunnable = () -> {
        if (mResultSent) return;

        if (mStep == STEP_A) {
            mResultPriceA  = (mConfirmedPrice  != null) ? mConfirmedPrice  : -1;
            mResultVolumeA = (mConfirmedVolume != null) ? mConfirmedVolume : -1;

            Log.d(TAG, "Step A done: price=" + mResultPriceA + " volume=" + mResultVolumeA);

            mScanningPaused = true;
            mNextBtn.setVisibility(android.view.View.GONE);
            mRescanBtn.setVisibility(android.view.View.GONE);
            mReadyOverlay.setVisibility(android.view.View.VISIBLE);

            mHandler.postDelayed(() -> {
                if (mResultSent) return;
                mReadyOverlay.setVisibility(android.view.View.GONE);
                mStep = STEP_B;
                mScanningPaused = false;
                resetStepState();
                updateUI();
            }, SCAN_PAUSE_MS);

        } else {
            mResultPriceB  = (mConfirmedPrice  != null) ? mConfirmedPrice  : -1;
            mResultVolumeB = (mConfirmedVolume != null) ? mConfirmedVolume : -1;

            Log.d(TAG, "Step B done: price=" + mResultPriceB + " volume=" + mResultVolumeB);

            // 同一商品スキャンチェック
            if (mResultPriceA > 0 && mResultPriceB > 0
                    && isSimilarValue(mResultPriceB, mResultPriceA)) {
                showSamePriceWarning();
                return;
            }

            finishWithResult();
        }
    };

    private void showSamePriceWarning() {
        mNextBtn.setVisibility(android.view.View.GONE);
        mRescanBtn.setVisibility(android.view.View.GONE);

        mNavText.setText("⚠️ 同じ商品をスキャンしていませんか？");
        mNavText.setTextColor(0xFFFF8C00);

        mPriceStatus.setText("💴 商品Aと同じ価格が認識されました");
        mPriceStatus.setTextColor(0xFFFF8C00);
        mVolumeStatus.setText("📦 商品Bを改めて写してください");
        mVolumeStatus.setTextColor(0xFFFFFFAA);

        TextView hint = findViewById(R.id.ar_hint_text);
        if (hint != null) hint.setText("2秒後に自動で再スキャンします...");

        mHandler.postDelayed(() -> {
            if (mResultSent) return;
            mNavText.setTextColor(0xFFFFFFFF);
            rescanCurrentStep();
        }, 2500L);
    }

    private void resetStepState() {
        mLastPrice       = null;
        mLastVolume      = null;
        mPriceStability  = 0;
        mVolumeStability = 0;
        mConfirmedPrice  = null;
        mConfirmedVolume = null;
        mOverlayView.clear();
        mNextBtn.setVisibility(android.view.View.GONE);
        mRescanBtn.setVisibility(android.view.View.GONE);
    }

    private void rescanCurrentStep() {
        mHandler.removeCallbacks(mAdvanceRunnable);
        resetStepState();
        updateUI();
    }

    // ── UI更新 ────────────────────────────────────────────────────────────────

    private void updateUI() {
        if (mStep == STEP_A) {
            mStepLabel.setText("STEP 1 / 2  商品A");
            mNavText.setText("商品Aの値札・ラベルを写してください");
            mNextBtn.setText("商品A 確定 ▶");
        } else {
            mStepLabel.setText("STEP 2 / 2  商品B");
            mNavText.setText("次に、商品Bを写してください");
            mNextBtn.setText("商品B 確定 ✓");
        }
        updateStatusText();
    }

    private void updateStatusText() {
        if (mConfirmedPrice != null) {
            mPriceStatus.setText("💴 金額: " + mConfirmedPrice + "円  ✓ 認識完了");
            mPriceStatus.setTextColor(0xFF00C853);
        } else if (mLastPrice != null) {
            mPriceStatus.setText("💴 金額: " + mLastPrice + "円  (" + mPriceStability + "/" + STABILITY_REQUIRED + ")");
            mPriceStatus.setTextColor(0xFFFFFFAA);
        } else {
            mPriceStatus.setText("💴 金額: 認識待ち...");
            mPriceStatus.setTextColor(0xAAFFFFFF);
        }

        if (mConfirmedVolume != null) {
            mVolumeStatus.setText("⚖️ 容量: " + mConfirmedVolume + "  ✓ 認識完了");
            mVolumeStatus.setTextColor(0xFF00BCD4);
        } else if (mLastVolume != null) {
            mVolumeStatus.setText("⚖️ 容量: " + mLastVolume + "  (" + mVolumeStability + "/" + STABILITY_REQUIRED + ")");
            mVolumeStatus.setTextColor(0xFFFFFFAA);
        } else {
            mVolumeStatus.setText("⚖️ 容量: 認識待ち...");
            mVolumeStatus.setTextColor(0xAAFFFFFF);
        }
    }

    // ── 結果返却 ──────────────────────────────────────────────────────────────

    private void finishWithResult() {
        if (mResultSent) return;
        mResultSent = true;

        stopCamera();

        Intent result = new Intent();
        result.putExtra(EXTRA_PRICE_A,  mResultPriceA);
        result.putExtra(EXTRA_VOLUME_A, mResultVolumeA);
        result.putExtra(EXTRA_PRICE_B,  mResultPriceB);
        result.putExtra(EXTRA_VOLUME_B, mResultVolumeB);
        setResult(RESULT_OK, result);

        Log.d(TAG, "finishWithResult: A(" + mResultPriceA + "," + mResultVolumeA
                + ") B(" + mResultPriceB + "," + mResultVolumeB + ")");

        finish();
    }

    private void cancelAndFinish() {
        mResultSent = true;
        stopCamera();
        setResult(RESULT_CANCELED);
        finish();
    }

    // ── パーミッション ────────────────────────────────────────────────────────

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this,
                        "カメラのアクセス許可が必要です",
                        Toast.LENGTH_LONG).show();
                cancelAndFinish();
            }
        }
    }
}

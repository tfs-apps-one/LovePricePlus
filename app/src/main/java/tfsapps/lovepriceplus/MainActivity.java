package tfsapps.lovepriceplus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

//サブスク
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class MainActivity extends AppCompatActivity implements PurchasesUpdatedListener {

    private String db_price_a = "";
    private String db_price_b = "";
    private String db_amount_a = "";
    private String db_amount_b = "";
    private String db_set_a = "";
    private String db_set_b = "";
    private String db_point_a = "";
    private String db_point_b = "";
    private String input_data = "";
    private int now_cursor = 1;

    final int CR_INIT = 0;
    final int CR_PRI_A = 1;
    final int CR_PRI_B = 2;
    final int CR_AMOUNT_A = 3;
    final int CR_AMOUNT_B = 4;
    final int CR_SET_A = 5;
    final int CR_SET_B = 6;
    final int CR_POINT_A = 7;
    final int CR_POINT_B = 8;

    private TextView txt_item_a;
    private TextView txt_item_b;
    private TextView txt_item_title;

    private Button btn_pri_a;
    private Button btn_pri_b;
    private Button btn_amount_a;
    private Button btn_amount_b;
    private Button btn_set_a;
    private Button btn_set_b;
    private Button btn_point_a;
    private Button btn_point_b;
    private Button btn_reset_a;
    private Button btn_reset_b;
    private Button btn_reset_all;

    private double unit_A = 0;
    private double unit_B = 0;

    //  DB関連
    private MyOpenHelper helper;    //DBアクセス
    private int db_isopen = 0;      //DB使用したか
    private String db_history1_a;   //item a
    private String db_history1_b;   //item b
    private String db_history2_a;   //item a
    private String db_history2_b;   //item b
    private String db_history3_a;   //item a
    private String db_history3_b;   //item b
    private String db_history4_a;   //item a
    private String db_history4_b;   //item b
    private String db_history5_a;   //item a
    private String db_history5_b;   //item b
    private int db_system1;
    private int db_system2;
    private int db_system3;
    private int db_system4;
    private int db_system5;

    //評価ポップアップ
    private int ReviewCount = 1;
    //test_make
//    private int REVIEW_POP = 3;
    private int REVIEW_POP = 10;


    // 広告
    private boolean visibleAd = true;
    private AdView mAdview;
    private ImageButton imgTrash;
    private boolean visibleTrash = true;

    //サブスク
    private BillingClient billingClient;
    private static final String TAG = "tag-ad-free-MainActivity";
    private static final String SUBSCRIPTION_ID = "ad_free_plan"; //


    //test_make
    //インタースティシャル広告
    private InterstitialAd mInterstitialAd;
    //本番ID
    private static final String AD_INTER_UNIT_ID = "ca-app-pub-4924620089567925/9156505561"; // 実際のIDに変更
    //テストID
//    private static final String AD_INTER_UNIT_ID = "ca-app-pub-3940256099942544/1033173712";

    private int CAL_MAX_TAP = 39;
    private boolean isInterAdd = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //広告
        /*
        MobileAds.initialize(this, initializationStatus -> {
                    mAdview = findViewById(R.id.adView);
                    AdRequest adRequest = new AdRequest.Builder().build();
                    mAdview.loadAd(adRequest);
                });
        */

        //サブスク
        /*
        //BillingClientを初期化
        billingClient = BillingClient.newBuilder(this)
                .setListener(this)
                .enablePendingPurchases()
                .build();

        // Google Playへの接続
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "@@@@@@ Billing Client connected ");
                    checkSubscriptionStatus();
                } else {
                    Log.e(TAG, "@@@@@@ Billing connection failed: " + billingResult.getDebugMessage());
//                    Log.e(TAG, "Billing Client connection failed");
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.e(TAG, "@@@@@@ Billing Service disconnected");
            }
        });
         */

        DisplayScreenLoad();
        DisplayScreen();

        //バナー広告表示
        MobileAds.initialize(this);
        mAdview = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdview.loadAd(adRequest);

        imgTrash = findViewById(R.id.img_trash);

        //インタースティシャル広告
        loadInterstitialAd();
    }

    /**************************************
     * インタースティシャル広告をロード
     ***************************************/
    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this, AD_INTER_UNIT_ID, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                mInterstitialAd = interstitialAd;
                Log.d("AdMob", "インタースティシャル広告がロードされました");

                // 広告のコールバックを設定（閉じた後の動作）
                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        Log.d("AdMob", "広告が閉じられました");
                        mInterstitialAd = null; // 再ロードの準備
                        loadInterstitialAd(); // 次の広告をロード
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError adError) {
                        Log.d("AdMob", "広告の表示に失敗しました: " + adError.getMessage());
                        mInterstitialAd = null; // 再ロードの準備
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.d("AdMob", "インタースティシャル広告のロードに失敗: " + loadAdError.getMessage());
                mInterstitialAd = null;
            }
        });
    }
    // インタースティシャル広告を表示
    private void showInterstitialAd() {
        if (mInterstitialAd != null) {
            mInterstitialAd.show(this);
        } else {
            Log.d("AdMob", "インタースティシャル広告はまだロードされていません");
            loadInterstitialAd(); // すぐに次の広告をロード
        }
    }

    /**************************************
     * バナー広告
    ***************************************/
    public void AdViewActive(boolean flag){
        visibleAd = flag;
        if (!visibleAd){
            // admob 非表示
            mAdview.setVisibility(View.GONE);
        } else {
            // admob 表示
            mAdview.setVisibility(View.VISIBLE);
        }
    }

    public void TrashActive(boolean flag){
        visibleTrash = flag;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!visibleTrash){
                    imgTrash.setVisibility(View.GONE);
                    imgTrash.requestLayout();
                }
                else{
                    imgTrash.setVisibility(View.VISIBLE);
                    imgTrash.requestLayout();
                }
            }
        });
    }

    private void ShowRatingPopup() {

        String ttl = "";
        String mess = "";

        //アプリを起動して 10回目の時
        if (db_system1 != REVIEW_POP){
            return;
        }
        else {
            db_system1++;
        }
        ttl =   "★☆アプリ評価のお願い☆★";
        mess =  "\nいつもご利用ありがとうございます\n"+
                "\nたくさん利用して頂いている貴方にお願いです。アプリを評価してもらませんか？ 評価して頂けると励みになります。"+
                "\n\n(この通知は今回限りです)"+
                "\n\n\n";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(ttl);
        builder.setMessage(mess);
        builder.setPositiveButton("評価する", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                RedirectToPlayStoreForRating();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("　後で　", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void ShowRatingPopupNG() {
        String ttl = "";
        String mess = "";

        ttl = "接続に失敗しました";
        mess = "\n評価サイトへのアクセスに失敗しました\n" +
                "\n" +
                "\n\n" +
                "\n\n\n";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(ttl);
        builder.setMessage(mess);
        builder.setPositiveButton("確認", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void RedirectToPlayStoreForRating() {
        try {
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            ShowRatingPopupNG();
        }
    }


    /**************************************************
     * 金額計算処理
     *  false:計算できない     true:計算結果あり
     *************************************************/
    public boolean Calculate() {
        double pri_a = 0;
        double pri_b = 0;
        double amount_a = 0;
        double amount_b = 0;
        double set_a = 0;
        double set_b = 0;
        int point_a = 0;
        int point_b = 0;
        double temp_a = 0;
        double temp_b = 0;

        //データ変換
        if (db_price_a.isEmpty() == false) {
            pri_a = Double.parseDouble(db_price_a);
        }
        if (db_price_b.isEmpty() == false) {
            pri_b = Double.parseDouble(db_price_b);
        }
        if (db_amount_a.isEmpty() == false) {
            amount_a = Double.parseDouble(db_amount_a);
        }
        if (db_amount_b.isEmpty() == false) {
            amount_b = Double.parseDouble(db_amount_b);
        }
        if (db_set_a.isEmpty() == false) {
            set_a = Double.parseDouble(db_set_a);
        }
        if (db_set_b.isEmpty() == false) {
            set_b = Double.parseDouble(db_set_b);
        }
        if (db_point_a.isEmpty() == false) {
            point_a = Integer.parseInt(db_point_a);
        }
        if (db_point_b.isEmpty() == false) {
            point_b = Integer.parseInt(db_point_b);
        }

        //必須項目の入力チェック
        if (pri_a <= 0 || (set_a <= 0 && amount_a <=0) || pri_b <= 0 || (set_b <= 0 && amount_b <= 0)) {
            return false;
        }

        temp_a = pri_a;
        temp_b = pri_b;

        if (point_a > 0) {
            temp_a = temp_a - point_a;
        }
        if (point_b > 0) {
            temp_b = temp_b - point_b;
        }
        //１個当たりの金額
        if (temp_a <= 0 || temp_b <= 0) {
            return false;
        }
        if (set_a > 0) {
            temp_a = (temp_a / set_a);
        }
        if (set_b > 0) {
            temp_b = (temp_b / set_b);
        }
        //１容量当たりの金額
        if (temp_a <= 0 || temp_b <= 0) {
            return false;
        }
        if (amount_a > 0) {
            temp_a = (temp_a / amount_a);
        }
        if (amount_b > 0) {
            temp_b = (temp_b / amount_b);
        }

        unit_A = temp_a;
        unit_B = temp_b;

/*  ↓↓↓ 旧計算ロジック ↓↓↓
        //ポイント単価をやめる
        if (point_a > 0) {
            unit_A = temp_a - point_a;
        }
        else{
            unit_A = temp_a;
        }
        if (point_b > 0) {
            unit_B = temp_b - point_b;
        }
        else {
            unit_B = temp_b;
        }
*/
        return true;
    }

    /**************************************************
     * 画面表示更新
     *
     *************************************************/
    //画面表示パーツの共通メモリへのロード処理
    public void DisplayCalculateResult() {

        String temp_a = "";
        String temp_b = "";
        LinearLayout lay_item_a = (LinearLayout)findViewById(R.id.linearLayout_11_1);
        LinearLayout lay_item_b = (LinearLayout)findViewById(R.id.linearLayout_11_3);

        int text_size = 16;

        if (Calculate()){
            if (unit_A < unit_B) {
                temp_a = "★お得★\n";
                temp_b = "\n";
            }
            else if (unit_B < unit_A){
                temp_a = "\n";
                temp_b = "★お得★\n";
            }
            else{
                temp_a = "同じ\n";
                temp_b = "同じ\n";
            }
            temp_a = temp_a + String.format("%.3f", unit_A) + " 円";
            temp_b = temp_b + String.format("%.3f", unit_B) + " 円";

            txt_item_a.setText(temp_a);
            txt_item_a.setTextColor(Color.DKGRAY);
            txt_item_a.setTypeface(Typeface.DEFAULT_BOLD);
            txt_item_a.setTextSize(text_size);

            txt_item_b.setText(temp_b);
            txt_item_b.setTextColor(Color.DKGRAY);
            txt_item_b.setTypeface(Typeface.DEFAULT_BOLD);
            txt_item_b.setTextSize(text_size);
            lay_item_a.setBackgroundResource(R.drawable.bak_noselect);
            lay_item_b.setBackgroundResource(R.drawable.bak_noselect);


            if (unit_A > unit_B){
//                txt_item_title.setText("お得▶︎");
//                txt_item_title.setTextColor(Color.rgb(255,100,100));

                txt_item_b.setTextColor(Color.rgb(255,100,100));
                lay_item_b.setBackgroundResource(R.drawable.bak_select_box);
            }
            else if (unit_A < unit_B){
//                txt_item_title.setText("◀︎お得");
//                txt_item_title.setTextColor(Color.rgb(255,100,100));

                txt_item_a.setTextColor(Color.rgb(255,100,100));
                lay_item_a.setBackgroundResource(R.drawable.bak_select_box);
            }
            txt_item_title.setTextColor(Color.DKGRAY);
            txt_item_title.setText("単価");

            //全面広告処理
            db_system2--;
            if (db_system2 < 0){
                db_system2 = CAL_MAX_TAP;
            }
            if (isInterAdd) {
                if (db_system2 == 1) {
                    //全面広告表示
                    showInterstitialAd();
                }
                if (db_system2 == 5) {
                    Context context = getApplicationContext();
                    Toast.makeText(context, "まもなく全面広告が表示されます....", Toast.LENGTH_SHORT).show();
                }
            }
        }
        else{
            lay_item_a.setBackgroundResource(R.drawable.bak_noselect);
            lay_item_b.setBackgroundResource(R.drawable.bak_noselect);
        }
    }

    //画面表示パーツの共通メモリへのロード処理
    public void DisplayScreenLoad() {
        txt_item_a = findViewById(R.id.text_item_a);
        txt_item_b = findViewById(R.id.text_item_b);
        txt_item_title = findViewById(R.id.text_item_title);

        btn_reset_a = findViewById(R.id.btn_reset_a);
        btn_reset_b = findViewById(R.id.btn_reset_b);
        btn_reset_all = findViewById(R.id.btn_reset_all);
        btn_pri_a = findViewById(R.id.btn_price_a);
        btn_pri_b = findViewById(R.id.btn_price_b);
        btn_amount_a = findViewById(R.id.btn_amount_a);
        btn_amount_b = findViewById(R.id.btn_amount_b);
        btn_set_a = findViewById(R.id.btn_set_a);
        btn_set_b = findViewById(R.id.btn_set_b);
        btn_point_a = findViewById(R.id.btn_point_a);
        btn_point_b = findViewById(R.id.btn_point_b);
    }
    //画面表示処理
    public void DisplayScreen() {
        int select_color = R.drawable.bak_btn_2;
        int no_select_color = R.drawable.bak_btn_1;

        String input_tmp = "";
        int text_size = 16;
    //画面表示
        TextView ttl_pri = findViewById(R.id.text_price_title);
        ttl_pri.setTextColor(Color.GRAY);
        ttl_pri.setTypeface(Typeface.DEFAULT_BOLD);
        ttl_pri.setTextSize(text_size);

        TextView ttl_amount = findViewById(R.id.text_amount_title);
        ttl_amount.setTextColor(Color.GRAY);
        ttl_amount.setTypeface(Typeface.DEFAULT_BOLD);
        ttl_amount.setTextSize(text_size);

        TextView ttl_set = findViewById(R.id.text_set_title);
        ttl_set.setTextColor(Color.GRAY);
        ttl_set.setTypeface(Typeface.DEFAULT_BOLD);
        ttl_set.setTextSize(text_size);

        TextView ttl_point = findViewById(R.id.text_point_title);
        ttl_point.setTextColor(Color.GRAY);
        ttl_point.setTypeface(Typeface.DEFAULT_BOLD);
        ttl_point.setTextSize(text_size-1);

        TextView ttl_item = findViewById(R.id.text_item_title);
        if (unit_A > 0 || unit_B > 0)
        {
            ttl_item.setText("単価");
        }
        else {
            ttl_item.setText("");
        }
        ttl_item.setTextColor(Color.GRAY);
        ttl_item.setTypeface(Typeface.DEFAULT_BOLD);
        ttl_item.setTextSize(text_size);

        txt_item_a.setTextColor(Color.GRAY);
        txt_item_a.setTypeface(Typeface.DEFAULT_BOLD);
        txt_item_a.setTextSize(text_size);

        txt_item_b.setTextColor(Color.GRAY);
        txt_item_b.setTypeface(Typeface.DEFAULT_BOLD);
        txt_item_b.setTextSize(text_size);

    //カーソル表示
        btn_reset_a.setBackgroundTintList(null);
        btn_reset_a.setBackgroundResource(R.drawable.bak_btn_5);
        btn_reset_a.setTextSize(text_size-2);
        btn_reset_a.setTextColor(Color.GRAY);
        btn_reset_a.setTypeface(Typeface.DEFAULT_BOLD);

        btn_reset_b.setBackgroundTintList(null);
        btn_reset_b.setBackgroundResource(R.drawable.bak_btn_5);
        btn_reset_b.setTextSize(text_size-2);
        btn_reset_b.setTextColor(Color.GRAY);
        btn_reset_b.setTypeface(Typeface.DEFAULT_BOLD);

        btn_reset_all.setBackgroundTintList(null);
        btn_reset_all.setBackgroundResource(R.drawable.bak_btn_5);
        btn_reset_all.setTextSize(text_size-2);
        btn_reset_all.setTextColor(Color.rgb(255, 140, 140));
        btn_reset_all.setTypeface(Typeface.DEFAULT_BOLD);

        btn_pri_a.setBackgroundTintList(null);
        btn_pri_a.setBackgroundResource(no_select_color);
        btn_pri_a.setTextSize(text_size);
        btn_pri_a.setTextColor(Color.DKGRAY);
        btn_pri_a.setTypeface(Typeface.DEFAULT_BOLD);

        btn_pri_b.setBackgroundTintList(null);
        btn_pri_b.setBackgroundResource(no_select_color);
        btn_pri_b.setTextSize(text_size);
        btn_pri_b.setTextColor(Color.DKGRAY);
        btn_pri_b.setTypeface(Typeface.DEFAULT_BOLD);

        btn_amount_a.setBackgroundTintList(null);
        btn_amount_a.setBackgroundResource(no_select_color);
        btn_amount_a.setTextSize(text_size);
        btn_amount_a.setTextColor(Color.DKGRAY);
        btn_amount_a.setTypeface(Typeface.DEFAULT_BOLD);

        btn_amount_b.setBackgroundTintList(null);
        btn_amount_b.setBackgroundResource(no_select_color);
        btn_amount_b.setTextSize(text_size);
        btn_amount_b.setTextColor(Color.DKGRAY);
        btn_amount_b.setTypeface(Typeface.DEFAULT_BOLD);

        btn_set_a.setBackgroundTintList(null);
        btn_set_a.setBackgroundResource(no_select_color);
        btn_set_a.setTextSize(text_size);
        btn_set_a.setTextColor(Color.DKGRAY);
        btn_set_a.setTypeface(Typeface.DEFAULT_BOLD);

        btn_set_b.setBackgroundTintList(null);
        btn_set_b.setBackgroundResource(no_select_color);
        btn_set_b.setTextSize(text_size);
        btn_set_b.setTextColor(Color.DKGRAY);
        btn_set_b.setTypeface(Typeface.DEFAULT_BOLD);

        btn_point_a.setBackgroundTintList(null);
        btn_point_a.setBackgroundResource(no_select_color);
        btn_point_a.setTextSize(text_size);
        btn_point_a.setTextColor(Color.DKGRAY);
        btn_point_a.setTypeface(Typeface.DEFAULT_BOLD);

        btn_point_b.setBackgroundTintList(null);
        btn_point_b.setBackgroundResource(no_select_color);
        btn_point_b.setTextSize(text_size);
        btn_point_b.setTextColor(Color.DKGRAY);
        btn_point_b.setTypeface(Typeface.DEFAULT_BOLD);


        switch (now_cursor){
            case CR_PRI_A:      btn_pri_a.setBackgroundResource(select_color);
                                input_tmp = db_price_a;
                                break;
            case CR_PRI_B:      btn_pri_b.setBackgroundResource(select_color);
                                input_tmp = db_price_b;
                                break;
            case CR_AMOUNT_A:   btn_amount_a.setBackgroundResource(select_color);
                                input_tmp = db_amount_a;
                                break;
            case CR_AMOUNT_B:   btn_amount_b.setBackgroundResource(select_color);
                                input_tmp = db_amount_b;
                                break;
            case CR_SET_A:      btn_set_a.setBackgroundResource(select_color);
                                input_tmp = db_set_a;
                                break;
            case CR_SET_B:      btn_set_b.setBackgroundResource(select_color);
                                input_tmp = db_set_b;
                                break;
            case CR_POINT_A:    btn_point_a.setBackgroundResource(select_color);
                                input_tmp = db_point_a;
                                break;
            case CR_POINT_B:    btn_point_b.setBackgroundResource(select_color);
                                input_tmp = db_point_b;
                                break;
        }

    //文字入力
        btn_pri_a.setText(db_price_a);
        btn_pri_b.setText(db_price_b);
        btn_amount_a.setText(db_amount_a);
        btn_amount_b.setText(db_amount_b);
        btn_set_a.setText(db_set_a);
        btn_set_b.setText(db_set_b);
        btn_point_a.setText(db_point_a);
        btn_point_b.setText(db_point_b);
    }
    /**************************************************
     * データ入力処理
     *
     *************************************************/
    //データ数値の入力処理
    public void NumDataInput() {

        switch (now_cursor){
            case CR_PRI_A:
                if (input_data == "." && db_price_a.contains(".")) {
                    return;
                }
                db_price_a += input_data;
                break;
            case CR_PRI_B:
                if (input_data == "." && db_price_b.contains(".")) {
                    return;
                }
                db_price_b += input_data;
                break;

            case CR_AMOUNT_A:
                if (input_data == "." && db_amount_a.contains(".")) {
                    return;
                }
                db_amount_a += input_data;
                break;

            case CR_AMOUNT_B:
                if (input_data == "." && db_amount_b.contains(".")) {
                    return;
                }
                db_amount_b += input_data;
                break;

            case CR_SET_A:
                if (input_data == "." && db_set_a.contains(".")) {
                    return;
                }
                db_set_a += input_data;
                break;

            case CR_SET_B:
                if (input_data == "." && db_set_b.contains(".")) {
                    return;
                }
                db_set_b += input_data;
                break;

            case CR_POINT_A:
//                if (input_data == "." && db_point_a.contains(".")) {
                if (input_data == "." ) {
                    return;
                }
                db_point_a += input_data;
                break;

            case CR_POINT_B:
//                if (input_data == "." && db_point_b.contains(".")) {
                if (input_data == "." ) {
                    return;
                }
                db_point_b += input_data;
                break;
        }
//        StringUtils.chop(str);
        DisplayScreen();
        DisplayCalculateResult();
    }
    //Delete処理
    public void NumDataDelete() {
        int len = 0;
        String tmp = "";
        switch (now_cursor){
            case CR_PRI_A:
                                if (db_price_a.isEmpty())   return;
                                len = db_price_a.length();
                                tmp = db_price_a.substring(0, len - 1);
                                db_price_a = tmp;
                                break;
            case CR_PRI_B:
                                if (db_price_b.isEmpty())   return;
                                len = db_price_b.length();
                                tmp = db_price_b.substring(0, len - 1);
                                db_price_b = tmp;
                                break;
            case CR_AMOUNT_A:
                                if (db_amount_a.isEmpty())   return;
                                len = db_amount_a.length();
                                tmp = db_amount_a.substring(0, len - 1);
                                db_amount_a = tmp;
                                break;
            case CR_AMOUNT_B:
                                if (db_amount_b.isEmpty())   return;
                                len = db_amount_b.length();
                                tmp = db_amount_b.substring(0, len - 1);
                                db_amount_b = tmp;
                                break;
            case CR_SET_A:
                                if (db_set_a.isEmpty())   return;
                                len = db_set_a.length();
                                tmp = db_set_a.substring(0, len - 1);
                                db_set_a = tmp;
                                break;
            case CR_SET_B:
                                if (db_set_b.isEmpty())   return;
                                len = db_set_b.length();
                                tmp = db_set_b.substring(0, len - 1);
                                db_set_b = tmp;
                                break;
            case CR_POINT_A:
                                if (db_point_a.isEmpty())   return;
                                len = db_point_a.length();
                                tmp = db_point_a.substring(0, len - 1);
                                db_point_a = tmp;
                                break;
            case CR_POINT_B:
                                if (db_point_b.isEmpty())   return;
                                len = db_point_b.length();
                                tmp = db_point_b.substring(0, len - 1);
                                db_point_b = tmp;
                                break;
        }
//        StringUtils.chop(str);
        DisplayScreen();
        DisplayCalculateResult();
    }

    /**************************************************
     * カーソル　ボタン処理
     *  type = 0:ダイレクト    1:次へ  2:前へ
     *************************************************/
    public void ChangeCursor(int type, int tmp_cursor) {

        switch (type){
            case 0:
                now_cursor = tmp_cursor;
                break;
            case 1:
                now_cursor++;
                break;
            case 2:
                now_cursor--;
                break;
        }
        //保険処理
        if (now_cursor > CR_POINT_B){
            now_cursor = CR_PRI_A;
        }
        if (now_cursor < CR_PRI_A){
            now_cursor = CR_POINT_B;
        }

        DisplayScreen();
    }


    /**************************************************
     * データ計算関連　ボタン処理
     *
     *************************************************/
    public void NumDataInput(int cursor) {
        //カーソルの決定

        //入力チェック（桁数）
        //小数点（２個以上の入力や、１桁目から小数点）
        if (true){

        }
        //入力値のデータセット
        //計算処理

    }

    /**************************************************
     * データ入力関連　ボタン処理
     *
     *************************************************/
    public void onPrice_a(View v) {
        ChangeCursor(0, CR_PRI_A);
    }

    public void onPrice_b(View v) {
        ChangeCursor(0, CR_PRI_B);
    }

    public void onAmount_a(View v) {
        ChangeCursor(0, CR_AMOUNT_A);
    }

    public void onAmount_b(View v) {
        ChangeCursor(0, CR_AMOUNT_B);
    }

    public void onSet_a(View v) {
        ChangeCursor(0, CR_SET_A);
    }

    public void onSet_b(View v) {
        ChangeCursor(0, CR_SET_B);
    }

    public void onPoint_a(View v) {
        ChangeCursor(0, CR_POINT_A);
    }

    public void onPoint_b(View v) {
        ChangeCursor(0, CR_POINT_B);
    }

    public void onReset_a(View v) {
        LinearLayout lay_item_a = (LinearLayout)findViewById(R.id.linearLayout_11_1);
        LinearLayout lay_item_b = (LinearLayout)findViewById(R.id.linearLayout_11_3);
        lay_item_a.setBackgroundResource(R.drawable.bak_noselect);
        lay_item_b.setBackgroundResource(R.drawable.bak_noselect);

        db_price_a = "";
        db_amount_a = "";
        db_set_a = "";
        db_point_a = "";
        txt_item_a.setText("商品Ａ");
        unit_A = 0;

        txt_item_title.setText("");
        DisplayScreen();
        DisplayCalculateResult();
    }

    public void onReset_b(View v) {
        LinearLayout lay_item_a = (LinearLayout)findViewById(R.id.linearLayout_11_1);
        LinearLayout lay_item_b = (LinearLayout)findViewById(R.id.linearLayout_11_3);
        lay_item_a.setBackgroundResource(R.drawable.bak_noselect);
        lay_item_b.setBackgroundResource(R.drawable.bak_noselect);

        db_price_b = "";
        db_amount_b = "";
        db_set_b = "";
        db_point_b = "";
        txt_item_b.setText("商品Ｂ");
        unit_B = 0;

        txt_item_title.setText("");
        DisplayScreen();
        DisplayCalculateResult();
    }

    public void onReset_All(View v) {
        LinearLayout lay_item_a = (LinearLayout)findViewById(R.id.linearLayout_11_1);
        LinearLayout lay_item_b = (LinearLayout)findViewById(R.id.linearLayout_11_3);
        lay_item_a.setBackgroundResource(R.drawable.bak_noselect);
        lay_item_b.setBackgroundResource(R.drawable.bak_noselect);

        db_price_a = "";
        db_amount_a = "";
        db_set_a = "";
        db_point_a = "";
        txt_item_a.setText("商品Ａ");
        unit_A = 0;

        db_price_b = "";
        db_amount_b = "";
        db_set_b = "";
        db_point_b = "";
        txt_item_b.setText("商品Ｂ");
        unit_B = 0;

        txt_item_title.setText("");

        now_cursor = CR_PRI_A;
        DisplayScreen();
        DisplayCalculateResult();
    }

    /**************************************************
     * 電卓　ボタン処理
     *
     *************************************************/
    public void onNum9(View v) {
        input_data = "9";
        NumDataInput();
    }

    public void onNum8(View v) {
        input_data = "8";
        NumDataInput();
    }

    public void onNum7(View v) {
        input_data = "7";
        NumDataInput();
    }

    public void onNum6(View v) {
        input_data = "6";
        NumDataInput();
    }

    public void onNum5(View v) {
        input_data = "5";
        NumDataInput();
    }
    public void onNum4(View v) {
        input_data = "4";
        NumDataInput();
    }

    public void onNum3(View v) {
        input_data = "3";
        NumDataInput();
    }

    public void onNum2(View v) {
        input_data = "2";
        NumDataInput();
    }

    public void onNum1(View v) {
        input_data = "1";
        NumDataInput();
    }

    public void onNum00(View v) {

        input_data = "00";
        NumDataInput();
    }

    public void onNum0(View v) {
        input_data = "0";
        NumDataInput();
    }

    public void onNumDot(View v) {
        input_data = ".";
        NumDataInput();
    }

    public void onQuestion(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("〜 POINTについて 〜");
        builder.setMessage("\n\n店舗独自で運用されている[POINT]を計算に含めることができます。\n\n例えば、100円に付き [1] POINT が付与される場合、価格248円の商品は [2] POINT になりますので、POINT欄に [2] を入力して下さい。\n[-2円] を差引して単価計算を行い、どちらがお得を求めます。\n\n\n");

        builder.setPositiveButton("閉じる", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void onNumNext(View v) {
        ChangeCursor(1, 0);
    }

    public void onNumBefore(View v) {
        ChangeCursor(2, 0);
    }

    public void onNumDelete(View v) {
        NumDataDelete();
    }

    public void onTrash(View v){
        Subscription();
    }

    /********************************
        設定
     ********************************/
    public void onSetting(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("設定メニュー");
        builder.setMessage("\n\n【 短縮 】\n履歴入力で素早く簡単入力ができます\n\n\n\n【広告非表示】\nサブスクリプションで広告を非表示にすることができます\n\n\n\n\n\n");

        builder.setPositiveButton("短縮", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                //ダイアログ処理
                History();
            }
        });

        builder.setNegativeButton("広告非表示", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                Subscription();
            }
        });

        builder.setNeutralButton("戻る", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                /*
                 *   処理なし（戻るだけ）
                 * */
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public void Subscription() {
        Intent intent = new Intent(MainActivity.this, SubscriptionActivity.class);
        startActivity(intent);
    }

    public void History() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("〜 短縮入力 機能 〜");
        builder.setMessage("\n\n短縮入力とは「価格、容量、数量、ポイント」を保存&読込する事が可能です。日常的に使用する条件を保存すると便利です。\n\n操作に応じてボタンを選択して下さい。\n\n\n\n [戻る] 短縮入力画面を閉じる\n [読込] 保存した値を読み込む\n [保存] 入力した値を保存する\n\n\n");

        builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                //ダイアログ処理
                AppDbSetHistoryData(1);
                AppDBUpdated();
            }
        });

        builder.setNegativeButton("読込", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                AppDbGetHistoryData(1);
                DisplayScreen();
                DisplayCalculateResult();
            }
        });

        builder.setNeutralButton("戻る", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                /*
                 *   処理なし（戻るだけ）
                 * */
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * OS関連処理
     */
    @Override
    public void onStart() {
        super.onStart();
        //DBのロード
        /* データベース */
        helper = new MyOpenHelper(this);
        AppDBInitRoad();

        //評価ポップアップ処理
        if (db_system1 <= REVIEW_POP){
            if (ReviewCount != 0){
                db_system1++;
                ReviewCount = 0;
            }
        }
        ShowRatingPopup();
    }

    @Override
    public void onResume() {
        super.onResume();
        //サブスク
        //BillingClientを初期化
        billingClient = BillingClient.newBuilder(this)
                .setListener(this)
                .enablePendingPurchases()
                .build();

        // Google Playへの接続
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "@@@@@@ Billing Client connected ");
                    checkSubscriptionStatus();
                } else {
                    Log.e(TAG, "@@@@@@ Billing connection failed: " + billingResult.getDebugMessage());
//                    Log.e(TAG, "Billing Client connection failed");
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.e(TAG, "@@@@@@ Billing Service disconnected");
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        //  DB更新
        AppDBUpdated();
    }

    @Override
    public void onStop() {
        super.onStop();
        //  DB更新
        AppDBUpdated();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //  DB更新
        AppDBUpdated();
        if (billingClient != null){
            billingClient.endConnection();
        }
    }
    /*
        計算条件をDBデータに反映
     */
    public void AppDbSetHistoryData(int index) {
        switch (index){
            default:
            case 1:
                db_history1_a = db_price_a + "," + db_amount_a + "," + db_set_a + "," + db_point_a + ","+"EOF";
                db_history1_b = db_price_b + "," + db_amount_b + "," + db_set_b + "," + db_point_b + ","+"EOF";
                break;

            case 2:
                db_history2_a = db_price_a + "," + db_amount_a + "," + db_set_a + "," + db_point_a + ","+"EOF";
                db_history2_b = db_price_b + "," + db_amount_b + "," + db_set_b + "," + db_point_b + ","+"EOF";
                break;

            case 3:
                db_history3_a = db_price_a + "," + db_amount_a + "," + db_set_a + "," + db_point_a + ","+"EOF";
                db_history3_b = db_price_b + "," + db_amount_b + "," + db_set_b + "," + db_point_b + ","+"EOF";
                break;

            case 4:
                db_history4_a = db_price_a + "," + db_amount_a + "," + db_set_a + "," + db_point_a + ","+"EOF";
                db_history4_b = db_price_b + "," + db_amount_b + "," + db_set_b + "," + db_point_b + ","+"EOF";
                break;

            case 5:
                db_history5_a = db_price_a + "," + db_amount_a + "," + db_set_a + "," + db_point_a + ","+"EOF";
                db_history5_b = db_price_b + "," + db_amount_b + "," + db_set_b + "," + db_point_b + ","+"EOF";
                break;
        }
    }
    public void AppDbGetHistoryData(int index) {
        String[] temp_a;
        String[] temp_b;

        switch (index) {
            default:
            case 1:
                temp_a = db_history1_a.split(",");
                temp_b = db_history1_b.split(",");
                break;

            case 2:
                temp_a = db_history2_a.split(",");
                temp_b = db_history2_b.split(",");
                break;

            case 3:
                temp_a = db_history3_a.split(",");
                temp_b = db_history3_b.split(",");
                break;

            case 4:
                temp_a = db_history4_a.split(",");
                temp_b = db_history4_b.split(",");
                break;

            case 5:
                temp_a = db_history5_a.split(",");
                temp_b = db_history5_b.split(",");
                break;
        }

        if (!temp_a[0].isEmpty()){
            db_price_a = temp_a[0];
        }
        else{
            db_price_a = "";
        }
        if (!temp_a[1].isEmpty()){
            db_amount_a = temp_a[1];
        }
        else{
            db_amount_a = "";
        }
        if (!temp_a[2].isEmpty()){
            db_set_a = temp_a[2];
        }
        else{
            db_set_a = "";
        }
        if (!temp_a[3].isEmpty()){
            db_point_a = temp_a[3];
        }
        else{
            db_point_a = "";
        }

        if (!temp_b[0].isEmpty()){
            db_price_b = temp_b[0];
        }
        else{
            db_price_b = "";
        }
        if (!temp_b[1].isEmpty()){
            db_amount_b = temp_b[1];
        }
        else{
            db_amount_b = "";
        }
        if (!temp_b[2].isEmpty()){
            db_set_b = temp_b[2];
        }
        else{
            db_set_b = "";
        }
        if (!temp_b[3].isEmpty()){
            db_point_b = temp_b[3];
        }
        else{
            db_point_b = "";
        }
    }

    /***************************************************
        DB初期ロードおよび設定
    ****************************************************/
    public void AppDBInitRoad() {
        SQLiteDatabase db = helper.getReadableDatabase();
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT");
        sql.append(" isopen");
        sql.append(" ,history1_a,history1_b");
        sql.append(" ,history2_a,history2_b");
        sql.append(" ,history3_a,history3_b");
        sql.append(" ,history4_a,history4_b");
        sql.append(" ,history5_a,history5_b");
        sql.append(" ,system1,system2");
        sql.append(" ,system3,system4");
        sql.append(" ,system5");
        sql.append(" FROM appinfo;");
        try {
            Cursor cursor = db.rawQuery(sql.toString(), null);
            //TextViewに表示
            StringBuilder text = new StringBuilder();
            if (cursor.moveToNext()) {
                db_isopen = cursor.getInt(0);
                db_history1_a = cursor.getString(1);
                db_history1_b = cursor.getString(2);
                db_history2_a = cursor.getString(3);
                db_history2_b = cursor.getString(4);
                db_history3_a = cursor.getString(5);
                db_history3_b = cursor.getString(6);
                db_history4_a = cursor.getString(7);
                db_history4_b = cursor.getString(8);
                db_history5_a = cursor.getString(9);
                db_history5_b = cursor.getString(10);
                db_system1 = cursor.getInt(11);
                db_system2 = cursor.getInt(12);
                db_system3 = cursor.getInt(13);
                db_system4 = cursor.getInt(14);
                db_system5 = cursor.getInt(15);
            }
        } finally {
            db.close();
        }

        db = helper.getWritableDatabase();
        if (db_isopen == 0) {
            long ret;
            /* 新規レコード追加 */
            ContentValues insertValues = new ContentValues();
            insertValues.put("isopen", 1);
            insertValues.put("history1_a", "");
            insertValues.put("history1_b", "");
            insertValues.put("history2_a", "");
            insertValues.put("history2_b", "");
            insertValues.put("history3_a", "");
            insertValues.put("history3_b", "");
            insertValues.put("history4_a", "");
            insertValues.put("history4_b", "");
            insertValues.put("history5_a", "");
            insertValues.put("history5_b", "");
            insertValues.put("system1", 0);
            insertValues.put("system2", 0);
            insertValues.put("system3", 0);
            insertValues.put("system4", 0);
            insertValues.put("system5", 0);
            try {
                ret = db.insert("appinfo", null, insertValues);
            } finally {
                db.close();
            }
            /*
            if (ret == -1) {
                Toast.makeText(this, "DataBase Create.... ERROR", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "DataBase Create.... OK", Toast.LENGTH_SHORT).show();
            }
             */

        } else {
            /*
            Toast.makeText(this, "Data Loading...  isopen:" + db_isopen, Toast.LENGTH_SHORT).show();
            */
        }
    }
    /***************************************************
        DB更新
    ****************************************************/
    public void AppDBUpdated() {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues insertValues = new ContentValues();
        insertValues.put("isopen", db_isopen);
        insertValues.put("history1_a", db_history1_a);
        insertValues.put("history1_b", db_history1_b);
        insertValues.put("history2_a", db_history2_a);
        insertValues.put("history2_b", db_history2_b);
        insertValues.put("history3_a", db_history3_a);
        insertValues.put("history3_b", db_history3_b);
        insertValues.put("history4_a", db_history4_a);
        insertValues.put("history4_b", db_history4_b);
        insertValues.put("history5_a", db_history5_a);
        insertValues.put("history5_b", db_history5_b);
        insertValues.put("system1", db_system1);
        insertValues.put("system2", db_system2);
        insertValues.put("system3", db_system3);
        insertValues.put("system4", db_system4);
        insertValues.put("system5", db_system5);
        int ret;
        try {
            ret = db.update("appinfo", insertValues, null, null);
        } finally {
            db.close();
        }
        /*
        if (ret != -1){
            Context context = getApplicationContext();
            Toast.makeText(context, "セーブ中...", Toast.LENGTH_SHORT).show();
//            Toast.makeText(context, "セーブ中...("+db_data1+")...", Toast.LENGTH_SHORT).show();
        }
        */

        /*
        if (ret == -1) {
            Toast.makeText(this, "Saving.... ERROR ", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Saving.... OK ", Toast.LENGTH_SHORT).show();
        }
         */
    }

    /*-----------------------------------------------------------------
        サブスク処理
     -----------------------------------------------------------------*/
    private void checkSubscriptionStatus(){
        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(),
                (billingResult, purchasesList) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchasesList != null){
                        boolean isSubscribed = false;
                        for (Purchase purchase : purchasesList) {
                            if (purchase.getProducts().contains(SUBSCRIPTION_ID)) {
                                isSubscribed = true;
                                break;
                            }
                        }
                        if (isSubscribed){
                            Log.e(TAG, "@@@@@@ サブスク有効");
                            TrashActive(false);
                            AdViewActive(false);
                            isInterAdd = false;
                        }
                        else{
                            Log.e(TAG, "@@@@@@ サブスク無効");
                            TrashActive(true);
                            AdViewActive(true);
                            isInterAdd = true;
                        }
                    }
                    else{
                        Log.e(TAG, "@@@@@@ サブスク有効／無効情報の取得エラー");
                    }
                }
        );
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, List<Purchase>purchases){

    }

}
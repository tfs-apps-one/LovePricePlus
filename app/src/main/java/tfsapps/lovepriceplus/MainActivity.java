package tfsapps.lovepriceplus;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayScreenLoad();
        DisplayScreen();
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
            temp_a = String.format("%.4f", unit_A);
            temp_b = String.format("%.4f", unit_B);

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
                txt_item_title.setText("お得▶︎");
                txt_item_title.setTextColor(Color.rgb(255,100,100));

                txt_item_b.setTextColor(Color.rgb(255,100,100));
                lay_item_b.setBackgroundResource(R.drawable.bak_select_box);
            }
            else if (unit_A < unit_B){
                txt_item_title.setText("◀︎お得");
                txt_item_title.setTextColor(Color.rgb(255,100,100));

                txt_item_a.setTextColor(Color.rgb(255,100,100));
                lay_item_a.setBackgroundResource(R.drawable.bak_select_box);
            }
            else{
                txt_item_title.setTextColor(Color.DKGRAY);
                txt_item_title.setText("同じ");
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
        ttl_item.setText("単価");
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
        db_price_b = "";
        db_amount_b = "";
        db_set_b = "";
        db_point_b = "";
        txt_item_b.setText("商品Ｂ");

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

    public void onHistory(View v) {

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

    /**
     * OS関連処理
     */
    @Override
    public void onStart() {
        super.onStart();
        //DBのロード
        /* データベース */
        //helper = new MyOpenHelper(this);
        AppDBInitRoad();
    }

    @Override
    public void onResume() {
        super.onResume();
        //動画
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
    }

    /***************************************************
     DB初期ロードおよび設定
     ****************************************************/
    public void AppDBInitRoad() {

    }

    /***************************************************
     DB更新
     ****************************************************/
    public void AppDBUpdated() {

    }
}
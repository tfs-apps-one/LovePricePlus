package tfsapps.lovepriceplus;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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
    private int now_cursor;

    final int CR_PRI_A = 0;
    final int CR_PRI_B = 1;
    final int CR_AMOUNT_A = 2;
    final int CR_AMOUNT_B = 3;
    final int CR_SET_A = 4;
    final int CR_SET_B = 5;
    final int CR_POINT_A = 6;
    final int CR_POINT_B = 7;

    private Button btn_pri_a;
    private Button btn_pri_b;
    private Button btn_amount_a;
    private Button btn_amount_b;
    private Button btn_set_a;
    private Button btn_set_b;
    private Button btn_point_a;
    private Button btn_point_b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayScreenLoad();
    }

    /**************************************************
     * 画面表示更新
     *
     *************************************************/
    public void DisplayScreenLoad() {
        btn_pri_a = findViewById(R.id.btn_price_a);
        btn_pri_b = findViewById(R.id.btn_price_b);
        btn_amount_a = findViewById(R.id.btn_amount_a);
        btn_amount_b = findViewById(R.id.btn_amount_b);
        btn_set_a = findViewById(R.id.btn_set_a);
        btn_set_b = findViewById(R.id.btn_set_b);
        btn_point_a = findViewById(R.id.btn_point_a);
        btn_point_b = findViewById(R.id.btn_point_b);
    }

    public void DisplayScreen() {
        int select_color = R.drawable.bak_btn_2;
        int no_select_color = R.drawable.bak_btn_1;
        String input_tmp = "";

        //カーソル表示
        btn_pri_a.setBackgroundTintList(null);
        btn_pri_a.setBackgroundResource(no_select_color);
        btn_pri_b.setBackgroundTintList(null);
        btn_pri_b.setBackgroundResource(no_select_color);

        btn_amount_a.setBackgroundTintList(null);
        btn_amount_a.setBackgroundResource(no_select_color);
        btn_amount_b.setBackgroundTintList(null);
        btn_amount_b.setBackgroundResource(no_select_color);

        btn_set_a.setBackgroundTintList(null);
        btn_set_a.setBackgroundResource(no_select_color);
        btn_set_b.setBackgroundTintList(null);
        btn_set_b.setBackgroundResource(no_select_color);

        btn_point_a.setBackgroundTintList(null);
        btn_point_a.setBackgroundResource(no_select_color);
        btn_point_b.setBackgroundTintList(null);
        btn_point_b.setBackgroundResource(no_select_color);

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
    public void NumDataInput() {

        switch (now_cursor){
            case CR_PRI_A:      db_price_a += input_data;
                break;
            case CR_PRI_B:      db_price_b += input_data;
                break;
            case CR_AMOUNT_A:   db_amount_a += input_data;
                break;
            case CR_AMOUNT_B:   db_amount_b += input_data;
                break;
            case CR_SET_A:      db_set_a += input_data;
                break;
            case CR_SET_B:      db_set_b += input_data;
                break;
            case CR_POINT_A:    db_point_a += input_data;
                break;
            case CR_POINT_B:    db_point_b += input_data;
                break;
        }
//        StringUtils.chop(str);
        DisplayScreen();
    }
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
    public void Calculate() {

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

    }

    public void onReset_b(View v) {

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
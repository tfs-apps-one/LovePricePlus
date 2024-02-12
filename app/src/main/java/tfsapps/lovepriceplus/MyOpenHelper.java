package tfsapps.lovepriceplus;

import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyOpenHelper extends SQLiteOpenHelper
{
    private static final String TABLE = "appinfo";
    public MyOpenHelper(Context context) {
        super(context, "AppDB", null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE + "("
                + "isopen integer,"             // DBオープン
                + "history1_a char,"            //
                + "history1_b char,"            //
                + "history2_a char,"            //
                + "history2_b char,"            //
                + "history3_a char,"            //
                + "history3_b char,"            //
                + "history4_a char,"            //
                + "history4_b char,"            //
                + "history5_a char,"            //
                + "history5_b char,"            //
                + "system1 integer,"            // 予備１～１０
                + "system2 integer,"
                + "system3 integer,"
                + "system4 integer,"
                + "system5 integer);");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
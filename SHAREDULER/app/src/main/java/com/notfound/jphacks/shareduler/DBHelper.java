package com.notfound.jphacks.shareduler;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DBHelper extends SQLiteOpenHelper {
    static final String DB = "schedule.db";
    static final int DB_VERSION = 1;
    // DBのテーブル
    // ２つに分けてもよかったかもしれない
    static final String CREATE_TABLE =
            "create table calendarData"
                    + "(id         integer primary key autoincrement,"
                    + " schedule   text not null,"
                    + " dateMillis integer not null,"
                    + " placeName text not null,"
                    + " latitude   real not null,"
                    + " longitude  real not null,"
                    + " url text,"
                    + " creatorName text not null,"
                    + " mode integer not null,"
                    + " alertTime integer not null,"
                    + " noticeFlag integer not null"
                    + ")";

    public DBHelper(Context context) {
        super(context, DB, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // versionが上がった時のため
    }
}
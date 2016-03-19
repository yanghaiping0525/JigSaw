package com.yang.jigsaw.dao.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2016/3/6.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "download.db";
    private static final int VERSION = 1;
    private static final String TABLE_NAME = "thread_info";
    private static final String SQL_CREATE = "create table " + TABLE_NAME + " (_id integer primary key autoincrement," +
            "thread_id integer not null,url text not null,start integer not null,end integer not null,finished not null)";
    private static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP);
        db.execSQL(SQL_CREATE);
    }
}

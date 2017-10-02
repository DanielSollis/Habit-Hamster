package com.example.jonathan.myapplication.db;

/**
 * Created by Jonathan on 9/27/2017.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HabitHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "HabitHamster";
    public static final int DB_Version = 2;

    public HabitHelper(Context context){
        super(context, DB_NAME, null, DB_Version);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + HabitContract.HabitEntry.TABLE + " ( " +
                HabitContract.HabitEntry._ID + " INTEGER PRIMARY KEY, " +
                HabitContract.HabitEntry.COLUMN_NAME_TITLE + " TEXT NOT NULL);";

        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + HabitContract.HabitEntry.TABLE);
        onCreate(db);
    }
}

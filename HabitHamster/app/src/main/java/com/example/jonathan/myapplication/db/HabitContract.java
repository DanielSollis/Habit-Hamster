package com.example.jonathan.myapplication.db;

import android.provider.BaseColumns;

/**
 * Created by Jonathan on 9/27/2017.
 */

import android.provider.BaseColumns;

public class HabitContract {

    private HabitContract() {}

    public class HabitEntry implements BaseColumns{
        public static final String TABLE = "habits";
        public static final String COLUMN_NAME_TITLE = "title";
    }

}

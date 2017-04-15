package com.velychko.kyrylo.faiflycities.data.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "CitiesDatabase.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_DICTIONARIES_TABLE =
                "CREATE TABLE " + DatabaseDescription.Cities.TABLE_NAME + "(" +
                        DatabaseDescription.Cities._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        DatabaseDescription.Cities.COLUMN_COUNTRY + " TEXT, " +
                        DatabaseDescription.Cities.COLUMN_CITY + " TEXT);";
        db.execSQL(CREATE_DICTIONARIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}

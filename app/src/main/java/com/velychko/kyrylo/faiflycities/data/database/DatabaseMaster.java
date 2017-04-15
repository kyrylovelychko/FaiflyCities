package com.velychko.kyrylo.faiflycities.data.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import static com.velychko.kyrylo.faiflycities.data.database.DatabaseDescription.Cities.COLUMN_CITY;
import static com.velychko.kyrylo.faiflycities.data.database.DatabaseDescription.Cities.COLUMN_COUNTRY;
import static com.velychko.kyrylo.faiflycities.data.database.DatabaseDescription.Cities.TABLE_NAME;

public class DatabaseMaster {

    private SQLiteDatabase database;
    private DatabaseHelper helper;
    private static DatabaseMaster instance;

    public DatabaseMaster(Context context) {
        helper = new DatabaseHelper(context);
        if (database == null || !database.isOpen()) {
            database = helper.getWritableDatabase();
        }
    }

    public static DatabaseMaster getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseMaster(context);
        }
        return instance;
    }

    public boolean fillDatabase(CitiesResponse response) {
        boolean result = false;
        String sqlInsert = "INSERT INTO " + TABLE_NAME +
                "(" + COLUMN_COUNTRY + "," + COLUMN_CITY + ")" +
                " VALUES(?,?);";
        SQLiteStatement statement = database.compileStatement(sqlInsert);

        database.beginTransaction();
        try {
            for (CitiesResponse.Country country : response.countries) {
                statement.clearBindings();
                statement.bindString(1, country.name);
                for (String city : country.cities) {
                    statement.bindString(2, city);
                    statement.executeInsert();
                }
            }
            database.setTransactionSuccessful();
            result = true;
        } finally {
            database.endTransaction();
            return result;
        }
    }
}

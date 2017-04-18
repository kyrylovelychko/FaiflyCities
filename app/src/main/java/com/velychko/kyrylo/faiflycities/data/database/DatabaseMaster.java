package com.velychko.kyrylo.faiflycities.data.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.velychko.kyrylo.faiflycities.data.network.countriestocities.CitiesResponse;
import com.velychko.kyrylo.faiflycities.data.network.countriestocities.Country;
import com.velychko.kyrylo.faiflycities.utils.Constants;

import static android.provider.BaseColumns._ID;
import static com.velychko.kyrylo.faiflycities.data.database.DatabaseDescription.Cities.*;

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

        database.delete(TABLE_NAME, null, null);

        String sqlInsert = "INSERT INTO " + TABLE_NAME +
                "(" + COLUMN_COUNTRY + "," + COLUMN_CITY + ")" +
                " VALUES(?,?);";
        SQLiteStatement statement = database.compileStatement(sqlInsert);

        database.beginTransaction();
        try {
            for (Country country : response.countries) {
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

    public Cursor getCountriesList (){
        return database.query(TABLE_NAME,
                new String[]{_ID + ", " + COLUMN_COUNTRY + ", COUNT(" + COLUMN_CITY + ") AS "
                        + Constants.SQL_ALIAS_COUNT_OF_CITIES},
                null,
                null,
                COLUMN_COUNTRY,
                null,
                _ID);
    }

    public Cursor getCitiesListByCountry (String countryName){
        return database.query(TABLE_NAME,
                new String[]{_ID + ", " + COLUMN_CITY},
                COLUMN_COUNTRY + "=?",
                new String[]{countryName},
                null,
                null,
                _ID);
    }
}

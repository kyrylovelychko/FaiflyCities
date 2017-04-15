package com.velychko.kyrylo.faiflycities.data.database;

import android.provider.BaseColumns;

public class DatabaseDescription {

    public static final class Cities implements BaseColumns {
        public static final String TABLE_NAME = "cities";

        public static final String COLUMN_COUNTRY = "country";
        public static final String COLUMN_CITY = "city";
    }

}

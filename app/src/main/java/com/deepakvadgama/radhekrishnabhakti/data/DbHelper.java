/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.deepakvadgama.radhekrishnabhakti.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract.ContentEntry;
import static com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract.FavoritesEntry;

/**
 * Manages a local database for weather data.
 */
public class DbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "bhakti.db";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        // No foreign key constraint. This will help when user gets favorites (ids) on fresh device with same
        // email id. In that case, the content might not be present but we still need to store favorites.
        final String SQL_CREATE_FAVORITES_TABLE = "CREATE TABLE " + FavoritesEntry.TABLE_NAME + " ("
                + FavoritesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + FavoritesEntry.COLUMN_CONTENT_ID + " INTEGER NOT NULL )";

        final String SQL_CREATE_CONTENT_TABLE = "CREATE TABLE " + ContentEntry.TABLE_NAME + " (" +
                ContentEntry._ID + " INTEGER PRIMARY KEY," +
                ContentEntry.COLUMN_TYPE + " TEXT NOT NULL, " +
                ContentEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                ContentEntry.COLUMN_AUTHOR + " TEXT NOT NULL, " +
                ContentEntry.COLUMN_URL + " TEXT NOT NULL, " +
                ContentEntry.COLUMN_TEXT + " TEXT NOT NULL);";

        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITES_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_CONTENT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ContentEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}

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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    void deleteTheDatabase() {
        mContext.deleteDatabase(DbHelper.DATABASE_NAME);
    }

    public void setUp() {
        deleteTheDatabase();
    }

    public void testCreateDb() throws Throwable {

        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(DatabaseContract.FavoritesEntry.TABLE_NAME);
        tableNameHashSet.add(DatabaseContract.ContentEntry.TABLE_NAME);

        mContext.deleteDatabase(DbHelper.DATABASE_NAME);
        SQLiteDatabase db = new DbHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly", c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while (c.moveToNext());

        // if this fails, it means that your database doesn't contain both the favorites entry
        // and weather entry tables
        assertTrue("Error: Your database was created without both the favorites entry and content entry tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + DatabaseContract.FavoritesEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> favoritesColumnHashSet = new HashSet<String>();
        favoritesColumnHashSet.add(DatabaseContract.FavoritesEntry.COLUMN_CONTENT_ID);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            favoritesColumnHashSet.remove(columnName);
        } while (c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required favorites
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required favorites entry columns",
                favoritesColumnHashSet.isEmpty());
        db.close();
    }

    public void testfavoritesTable() {
        insertfavorites();
    }

    public void testContentTable() {

        long favoritesRowId = insertfavorites();
        assertFalse("Error: favorites Not Inserted Correctly", favoritesRowId == -1L);

        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues contentValues = TestUtilities.createContentValues(favoritesRowId);

        long contentRowId = db.insert(DatabaseContract.ContentEntry.TABLE_NAME, null, contentValues);
        assertTrue(contentRowId != -1);

        Cursor contentCursor = db.query(
                DatabaseContract.ContentEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        assertTrue("Error: No Records returned from content query", contentCursor.moveToFirst());

        TestUtilities.validateCurrentRecord("testInsertReadDb contentEntry failed to validate",
                contentCursor, contentValues);

        assertFalse("Error: More than one record returned from weather query",
                contentCursor.moveToNext());

        contentCursor.close();
        dbHelper.close();
    }


    public long insertfavorites() {

        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createFavoritesEntry();

        long favoritesRowId;
        favoritesRowId = db.insert(DatabaseContract.FavoritesEntry.TABLE_NAME, null, testValues);

        assertTrue(favoritesRowId != -1);

        Cursor cursor = db.query(
                DatabaseContract.FavoritesEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        assertTrue("Error: No Records returned from favorites query", cursor.moveToFirst());

        TestUtilities.validateCurrentRecord("Error: favorites Query Validation Failed",
                cursor, testValues);

        assertFalse("Error: More than one record returned from favorites query",
                cursor.moveToNext());

        cursor.close();
        db.close();
        return favoritesRowId;
    }
}

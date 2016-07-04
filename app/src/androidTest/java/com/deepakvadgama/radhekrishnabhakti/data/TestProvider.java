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

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;

import com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract.ContentEntry;
import com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract.FavoritesEntry;

import static com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract.CONTENT_AUTHORITY;
import static com.deepakvadgama.radhekrishnabhakti.data.TestUtilities.TEST_DATA_AUTHOR;
import static com.deepakvadgama.radhekrishnabhakti.data.TestUtilities.TEST_DATA_RANDOM;
import static com.deepakvadgama.radhekrishnabhakti.data.TestUtilities.TEST_DATA_TITLE;
import static com.deepakvadgama.radhekrishnabhakti.data.TestUtilities.TEST_DATA_TYPE;
import static com.deepakvadgama.radhekrishnabhakti.data.TestUtilities.TEST_ID;
import static com.deepakvadgama.radhekrishnabhakti.data.TestUtilities.TestContentObserver;
import static com.deepakvadgama.radhekrishnabhakti.data.TestUtilities.createContentValues;
import static com.deepakvadgama.radhekrishnabhakti.data.TestUtilities.createFavoritesEntry;
import static com.deepakvadgama.radhekrishnabhakti.data.TestUtilities.getTestContentObserver;
import static com.deepakvadgama.radhekrishnabhakti.data.TestUtilities.insertContentEntry;
import static com.deepakvadgama.radhekrishnabhakti.data.TestUtilities.validateCurrentRecord;
import static com.deepakvadgama.radhekrishnabhakti.data.TestUtilities.validateCursor;

public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(ContentEntry.CONTENT_URI, null, null);
        mContext.getContentResolver().delete(FavoritesEntry.CONTENT_URI, null, null);

        Cursor cursor = mContext.getContentResolver().query(ContentEntry.CONTENT_URI, null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Content table during delete", 0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                FavoritesEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Favorites table during delete", 0, cursor.getCount());
        cursor.close();
    }

    public void deleteAllRecords() {
        deleteAllRecordsFromProvider();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        ComponentName componentName = new ComponentName(mContext.getPackageName(), ContentProvider.class.getName());
        try {
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            assertEquals("Error: ContentProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + CONTENT_AUTHORITY,
                    providerInfo.authority, CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            assertTrue("Error: ContentProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    public void testGetType() {

        String type = mContext.getContentResolver().getType(ContentEntry.CONTENT_URI);
        assertEquals("Error: the ContentEntry CONTENT_URI should return ContentEntry.CONTENT_TYPE",
                ContentEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(ContentEntry.buildContentSearchWithType(TEST_DATA_TYPE));
        assertEquals("Error: the ContentEntry CONTENT_URI with Type should return ContentEntry.CONTENT_TYPE",
                ContentEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(ContentEntry.buildContentSearchWithAuthor(TEST_DATA_AUTHOR));
        assertEquals("Error: the ContentEntry CONTENT_URI with Author should return ContentEntry.CONTENT_TYPE",
                ContentEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(ContentEntry.buildContentSearchWithTitle(TEST_DATA_TITLE));
        assertEquals("Error: the ContentEntry CONTENT_URI with Title should return ContentEntry.CONTENT_TYPE",
                ContentEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(ContentEntry.buildContentSearchWithAny(TEST_DATA_RANDOM));
        assertEquals("Error: the ContentEntry CONTENT_URI with Any should return ContentEntry.CONTENT_TYPE",
                ContentEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(FavoritesEntry.CONTENT_URI);
        assertEquals("Error: the FavoritesEntry CONTENT_URI should return FavoritesEntry.CONTENT_TYPE",
                FavoritesEntry.CONTENT_TYPE, type);
    }

    public void testBasicContentQuery() {

        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues contentValues = createContentValues(TEST_ID);
        long contentRowId = insertContentEntry(mContext, contentValues);


        assertTrue("Unable to Insert ContentEntry into the Database", contentRowId != -1);


        db.close();

        Cursor weatherCursor = mContext.getContentResolver().query(
                ContentEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        validateCursor("testBasicContentQuery", weatherCursor, contentValues);
    }

    public void testFavoritesQueries() {

        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues favoritesEntry = createFavoritesEntry();
        long favoritesRowId = db.insert(FavoritesEntry.TABLE_NAME, null, favoritesEntry);
        assertTrue("Unable to Insert FavoritesEntry into the Database", favoritesRowId != -1);

        Cursor cursor = mContext.getContentResolver().query(
                FavoritesEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        validateCursor("testFavoritesQueries", cursor, favoritesEntry);

        if (Build.VERSION.SDK_INT >= 19) {
            assertEquals("Error: Favorites Query did not properly set NotificationUri",
                    cursor.getNotificationUri(), FavoritesEntry.CONTENT_URI);
        }
    }

//    public void testUpdateLocation() {
//
//        ContentValues values = TestUtilities.createContentValues(TestUtilities.TEST_ID);
//
//        Uri uri = mContext.getContentResolver().insert(ContentEntry.CONTENT_URI, values);
//        long locationRowId = ContentUris.parseId(uri);
//
//        // Verify we got a row back.
//        assertTrue(locationRowId != -1);
//        Log.d(LOG_TAG, "New row id: " + locationRowId);
//
//        ContentValues updatedValues = new ContentValues(values);
//        updatedValues.put(ContentEntry._ID, locationRowId);
//        updatedValues.put(ContentEntry.COLUMN_CITY_NAME, "Santa's Village");
//
//        // Create a cursor with observer to make sure that the content provider is notifying
//        // the observers as expected
//        Cursor locationCursor = mContext.getContentResolver().query(FavoritesEntry.CONTENT_URI, null, null, null, null);
//
//        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
//        locationCursor.registerContentObserver(tco);
//
//        int count = mContext.getContentResolver().update(
//                FavoritesEntry.CONTENT_URI, updatedValues, FavoritesEntry._ID + "= ?",
//                new String[]{Long.toString(locationRowId)});
//        assertEquals(count, 1);
//
//        // Test to make sure our observer is called.  If not, we throw an assertion.
//        //
//        // Students: If your code is failing here, it means that your content provider
//        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
//        tco.waitForNotificationOrFail();
//
//        locationCursor.unregisterContentObserver(tco);
//        locationCursor.close();
//
//        // A cursor is your primary interface to the query results.
//        Cursor cursor = mContext.getContentResolver().query(
//                FavoritesEntry.CONTENT_URI,
//                null,   // projection
//                FavoritesEntry._ID + " = " + locationRowId,
//                null,   // Values for the "where" clause
//                null    // sort order
//        );
//
//        TestUtilities.validateCursor("testUpdateLocation.  Error validating location entry update.",
//                cursor, updatedValues);
//
//        cursor.close();
//    }

    public void testInsertReadProvider() {

        ContentValues favoritesValues = createFavoritesEntry();

        // Register a content observer for our insert.  This time, directly with the content resolver
        TestContentObserver tco = getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(FavoritesEntry.CONTENT_URI, true, tco);
        Uri locationUri = mContext.getContentResolver().insert(FavoritesEntry.CONTENT_URI, favoritesValues);

        // Did our content observer get called?  Students:  If this fails, your insert location
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long locationRowId = ContentUris.parseId(locationUri);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                FavoritesEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        validateCursor("testInsertReadProvider. Error validating FavoritesEntry.", cursor, favoritesValues);

        ContentValues contentValues = createContentValues(TEST_ID);
        tco = getTestContentObserver();

        mContext.getContentResolver().registerContentObserver(ContentEntry.CONTENT_URI, true, tco);
        Uri contentInsertUri = mContext.getContentResolver().insert(ContentEntry.CONTENT_URI, contentValues);
        assertTrue(contentInsertUri != null);

        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        // A cursor is your primary interface to the query results.
        Cursor contentCursor = mContext.getContentResolver().query(
                ContentEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );

        validateCursor("testInsertReadProvider. Error validating ContentEntry insert.", contentCursor, contentValues);

        contentCursor = mContext.getContentResolver().query(
                ContentEntry.buildContentSearchWithAuthor(TEST_DATA_AUTHOR),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        validateCursor("testInsertReadProvider. Error validating Content with author.", contentCursor, contentValues);

        // Get the joined Content and Favorites data with a start date
        contentCursor = mContext.getContentResolver().query(
                ContentEntry.buildContentSearchWithTitle(TEST_DATA_TITLE),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        validateCursor("testInsertReadProvider. Error validating Content with title.",
                contentCursor, contentValues);

        // Get the joined Content data for a specific date
        contentCursor = mContext.getContentResolver().query(
                ContentEntry.buildContentSearchWithType(TEST_DATA_TYPE),
                null,
                null,
                null,
                null
        );
        validateCursor("testInsertReadProvider. Error validating Content with type.", contentCursor, contentValues);

        // Get the joined Content data for a specific date
        contentCursor = mContext.getContentResolver().query(
                ContentEntry.buildContentSearchWithAny(TEST_DATA_RANDOM),
                null,
                null,
                null,
                null
        );
        validateCursor("testInsertReadProvider. Error validating Content with search.", contentCursor, contentValues);
    }

    public void testDeleteRecords() {
        testInsertReadProvider();

        // Register a content observer for our location delete.
        TestContentObserver favObserver = getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(FavoritesEntry.CONTENT_URI, true, favObserver);

        // Register a content observer for our weather delete.
        TestContentObserver contentObserver = getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(ContentEntry.CONTENT_URI, true, contentObserver);

        deleteAllRecordsFromProvider();

        // Students: If either of these fail, you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in the ContentProvider
        // delete.  (only if the insertReadProvider is succeeding)
        favObserver.waitForNotificationOrFail();
        contentObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(favObserver);
        mContext.getContentResolver().unregisterContentObserver(contentObserver);
    }


    static private final int BULK_INSERT_RECORDS_TO_INSERT = 5;

    public void testBulkInsert() {

        ContentValues testValues = createFavoritesEntry();
        Uri locationUri = mContext.getContentResolver().insert(FavoritesEntry.CONTENT_URI, testValues);
        long locationRowId = ContentUris.parseId(locationUri);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                FavoritesEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        validateCursor("testBulkInsert. Error validating FavoritesEntry.", cursor, testValues);

        ContentValues[] bulkInsertContentValues = TestUtilities.createContentValuesBulk();

        // Register a content observer for our bulk insert.
        TestContentObserver contentObserver = getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(ContentEntry.CONTENT_URI, true, contentObserver);

        int insertCount = mContext.getContentResolver().bulkInsert(ContentEntry.CONTENT_URI, bulkInsertContentValues);

        contentObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(contentObserver);

        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);

        cursor = mContext.getContentResolver().query(
                ContentEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order == by DATE ASCENDING
        );

        // we should have as many records in the database as we've inserted
        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        // and let's make sure they match the ones we created
        cursor.moveToFirst();
        for (int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext()) {
            validateCurrentRecord("testBulkInsert.  Error validating ContentEntry " + i,
                    cursor, bulkInsertContentValues[i]);
        }
        cursor.close();
    }
}

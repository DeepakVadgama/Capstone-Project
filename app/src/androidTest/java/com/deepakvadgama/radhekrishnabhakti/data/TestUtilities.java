package com.deepakvadgama.radhekrishnabhakti.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.deepakvadgama.radhekrishnabhakti.utils.PollingCheck;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract.ContentEntry;
import static com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract.FavoritesEntry;

public class TestUtilities extends AndroidTestCase {

    static final int TEST_ID = 23;
    public static final String TEST_DATA_TYPE = "PICTURE";
    public static final String TEST_DATA_AUTHOR = "Swami Nikhilanand";
    public static final String TEST_DATA_TITLE = "Radhe Krishna";
    public static final String TEST_DATA_RANDOM = "Radhe";

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error,
                                      Cursor valueCursor,
                                      ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    static ContentValues createContentValues(long locationRowId) {
        ContentValues content = new ContentValues();
        content.put(ContentEntry._ID, TEST_ID);
        content.put(ContentEntry.COLUMN_TYPE, TEST_DATA_TYPE);
        content.put(ContentEntry.COLUMN_AUTHOR, TEST_DATA_AUTHOR);
        content.put(ContentEntry.COLUMN_TITLE, TEST_DATA_TITLE);
        content.put(ContentEntry.COLUMN_URL, "https://storage.googleapis.com/radhekrishna/8.jpg");
        content.put(ContentEntry.COLUMN_TEXT, "Temporary dummy text");
        return content;
    }

    static ContentValues[] createContentValuesBulk() {

        List<ContentValues> records = new ArrayList<>();

        ContentValues content = new ContentValues();
        content.put(ContentEntry._ID, TEST_ID);
        content.put(ContentEntry.COLUMN_TYPE, "PICTURE");
        content.put(ContentEntry.COLUMN_AUTHOR, TEST_DATA_AUTHOR);
        content.put(ContentEntry.COLUMN_TITLE, TEST_DATA_TITLE);
        content.put(ContentEntry.COLUMN_URL, "https://storage.googleapis.com/radhekrishna/8.jpg");
        content.put(ContentEntry.COLUMN_TEXT, "Temporary dummy text");
        records.add(content);

        ContentValues content2 = new ContentValues();
        content2.put(ContentEntry._ID, TEST_ID + 1);
        content2.put(ContentEntry.COLUMN_TYPE, "QUOTE");
        content2.put(ContentEntry.COLUMN_AUTHOR, "Swami Vivekanand");
        content2.put(ContentEntry.COLUMN_TITLE, "Inspiration");
        content2.put(ContentEntry.COLUMN_URL, "https://storage.googleapis.com/radhekrishna/8.jpg");
        content2.put(ContentEntry.COLUMN_TEXT, "Awake, Arise and Stop not until the goa is achieved.");
        records.add(content2);

        ContentValues content3 = new ContentValues();
        content3.put(ContentEntry._ID, TEST_ID + 2);
        content3.put(ContentEntry.COLUMN_TYPE, "STORY");
        content3.put(ContentEntry.COLUMN_AUTHOR, "Kripalu Maharaj");
        content3.put(ContentEntry.COLUMN_TITLE, "Prahlad's Life");
        content3.put(ContentEntry.COLUMN_URL, "https://storage.googleapis.com/radhekrishna/8.jpg");
        content3.put(ContentEntry.COLUMN_TEXT, "Temporary story text. Temporary story text. Temporary story text. " +
                "Temporary story text. Temporary story text. ");
        records.add(content3);

        ContentValues content4 = new ContentValues();
        content4.put(ContentEntry._ID, TEST_ID + 3);
        content4.put(ContentEntry.COLUMN_TYPE, "LECTURE");
        content4.put(ContentEntry.COLUMN_AUTHOR, "Parikari Didi");
        content4.put(ContentEntry.COLUMN_TITLE, "Living Life");
        content4.put(ContentEntry.COLUMN_URL, "https://storage.googleapis.com/radhekrishna/8.jpg");
        content4.put(ContentEntry.COLUMN_TEXT, "Temporary lecture text .......................................... ");
        records.add(content4);

        ContentValues content5 = new ContentValues();
        content5.put(ContentEntry._ID, TEST_ID + 4);
        content5.put(ContentEntry.COLUMN_TYPE, "LECTURE");
        content5.put(ContentEntry.COLUMN_AUTHOR, "Kripalu Maharaj");
        content5.put(ContentEntry.COLUMN_TITLE, "Main Kaun Mera Kaun");
        content5.put(ContentEntry.COLUMN_URL, "https://www.youtube.com/watch?v=lx9H0etPlGQ");
        content5.put(ContentEntry.COLUMN_TEXT, "");
        records.add(content5);

        return records.toArray(new ContentValues[5]);
    }

    static ContentValues createFavoritesEntry() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(FavoritesEntry.COLUMN_CONTENT_ID, TEST_ID);
        return testValues;
    }

    static long insertContentEntry(Context context, ContentValues testValues) {

        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long locationRowId;
        locationRowId = db.insert(ContentEntry.TABLE_NAME, null, testValues);

        assertTrue("Error: Failure to insert Content Values", locationRowId != -1);

        return locationRowId;
    }

//    static long insertFavoritesEntry(Context context) {
//        // insert our test records into the database
//        DbHelper dbHelper = new DbHelper(context);
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//        ContentValues testValues = TestUtilities.createFavoritesEntry();
//
//        long locationRowId;
//        locationRowId = db.insert(DatabaseContract.FavoritesEntry.TABLE_NAME, null, testValues);
//
//        // Verify we got a row back.
//        assertTrue("Error: Failure to insert North Pole Location Values", locationRowId != -1);
//
//        return locationRowId;
//    }

    /*
        Students: The functions we provide inside of TestProvider use this utility class to test
        the ContentObserver callbacks using the PollingCheck class that we grabbed from the Android
        CTS tests.

        Note that this only tests that the onChange function is called; it does not test that the
        correct Uri is returned.
     */
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}

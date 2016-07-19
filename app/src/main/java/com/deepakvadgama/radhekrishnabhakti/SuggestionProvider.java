package com.deepakvadgama.radhekrishnabhakti;

import android.content.SearchRecentSuggestionsProvider;
import android.database.Cursor;
import android.net.Uri;

public class SuggestionProvider extends SearchRecentSuggestionsProvider {

    public final static String AUTHORITY = "com.deepakvadgama.radhekrishnabhakti.app.SuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;
    public static final String TAG = "SuggestionProvider";


    public SuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return super.query(uri, projection, selection, selectionArgs, sortOrder);
    }
}

// For custom options
//public class SuggestionProvider extends ContentProvider {
//
//    List<String> options = new ArrayList<>();
//
//    @Override
//    public boolean onCreate() {
//
//        options.add("Quotes");
//        options.add("Kirtan");
//        options.add("Lectures");
//        options.add("Pictures");
//
//        return false;
//    }
//
//    @Override
//    public Cursor query(Uri uri, String[] projection, String selection,
//                        String[] selectionArgs, String sortOrder) {
//
//        MatrixCursor cursor = new MatrixCursor(
//                new String[] {
//                        BaseColumns._ID,
//                        SearchManager.SUGGEST_COLUMN_TEXT_1,
//                        SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID,
//                        SearchManager.SUGGEST_COLUMN_ICON_1
//                }
//        );
//
//        if (options != null) {
//            String query = uri.getLastPathSegment().toUpperCase();
//            int limit = Integer.parseInt(uri.getQueryParameter(SearchManager.SUGGEST_PARAMETER_LIMIT));
//
//            int lenght = options.size();
//            for (int i = 0; i < lenght && cursor.getCount() < limit; i++) {
//                String option = options.get(i);
////                if (option.toUpperCase().contains(query)){
//                    cursor.addRow(new Object[]{ i, option, i });
////                }
//            }

//    Uri uri = Uri.parse("android.resource://com.deepakvadgama.radhekrishnabhakti/drawable/ic_restore_white_24dp");
//cursor.addRow(new Object[]{0, "Quotes", 0, uri});
//        cursor.addRow(new Object[]{1, "Stories", 1, uri});
//        cursor.addRow(new Object[]{2, "Pictures", 2, uri});
//        }
//        return cursor;
//    }
//
//    @Nullable
//    @Override
//    public String getType(Uri uri) {
//        return null;
//    }
//
//    @Nullable
//    @Override
//    public Uri insert(Uri uri, ContentValues values) {
//        return null;
//    }
//
//    @Override
//    public int delete(Uri uri, String selection, String[] selectionArgs) {
//        return 0;
//    }
//
//    @Override
//    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
//        return 0;
//    }
//
//}
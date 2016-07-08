package com.deepakvadgama.radhekrishnabhakti.data;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import static com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract.CONTENT_AUTHORITY;
import static com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract.ContentEntry;
import static com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract.FavoritesEntry;
import static com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract.PATH_CONTENT;
import static com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract.PATH_FAVORITE;

public class ContentProvider extends android.content.ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private DbHelper mOpenHelper;

    static final int CONTENT = 100;
    static final int CONTENT_WITH_TYPE = 101;
    static final int CONTENT_WITH_AUTHOR = 102;
    static final int CONTENT_WITH_TITLE = 103;
    static final int CONTENT_WITH_SEARCH = 104;
    static final int FAVORITES = 300;

    private static final SQLiteQueryBuilder queryBuilder;

    static {
        queryBuilder = new SQLiteQueryBuilder();

        // We need this join for favorite (left outer join)
        queryBuilder.setTables(
                ContentEntry.TABLE_NAME + " LEFT OUTER JOIN " +
                        FavoritesEntry.TABLE_NAME +
                        " ON (" + ContentEntry.TABLE_NAME +
                        "." + ContentEntry._ID +
                        " = " + FavoritesEntry.TABLE_NAME +
                        "." + FavoritesEntry.COLUMN_CONTENT_ID + ")");
    }

    private static final String sTitleSelection = ContentEntry.TABLE_NAME + "." + ContentEntry.COLUMN_TITLE + " like ? ";
    private static final String sTypeSelection = ContentEntry.TABLE_NAME + "." + ContentEntry.COLUMN_TYPE + " = ? ";
    private static final String sAuthorSelection = ContentEntry.TABLE_NAME + "." + ContentEntry.COLUMN_AUTHOR + " like ? ";
    private static final String sSearchSelection = ContentEntry.TABLE_NAME + "." + ContentEntry.COLUMN_AUTHOR + " = ? OR " +
            ContentEntry.TABLE_NAME + "." + ContentEntry.COLUMN_TITLE + " like ? OR " +
            ContentEntry.TABLE_NAME + "." + ContentEntry.COLUMN_TYPE + " = ? OR " +
            ContentEntry.TABLE_NAME + "." + ContentEntry.COLUMN_TEXT + " like ? ";

    private Cursor getContentBySearch(Uri uri, String[] projection, String sortOrder) {

        String searchText = ContentEntry.getSearchFromUri(uri);
        String[] selectionArgs = new String[]{searchText, "%" + searchText + "%", searchText, "%" + searchText + "%"};
        String selection = sSearchSelection;

        return queryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getContentByTitle(Uri uri, String[] projection, String sortOrder) {

        String title = ContentEntry.getTitleFromUri(uri);
        String[] selectionArgs = new String[]{"%" + title + "%"};
        String selection = sTitleSelection;

        return queryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getContentByType(Uri uri, String[] projection, String sortOrder) {

        String type = ContentEntry.getTypeFromUri(uri);
        String[] selectionArgs = new String[]{type};
        String selection = sTypeSelection;

        return queryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getContentByAuthor(Uri uri, String[] projection, String sortOrder) {

        String author = ContentEntry.getAuthorFromUri(uri);
        String[] selectionArgs = new String[]{"%" + author + "%"};
        String selection = sAuthorSelection;

        return queryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }


    static UriMatcher buildUriMatcher() {

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = CONTENT_AUTHORITY;

        matcher.addURI(authority, PATH_CONTENT, CONTENT);
        matcher.addURI(authority, PATH_CONTENT + "/" + ContentEntry.COLUMN_TYPE, CONTENT_WITH_TYPE);
        matcher.addURI(authority, PATH_CONTENT + "/" + ContentEntry.COLUMN_TITLE, CONTENT_WITH_TITLE);
        matcher.addURI(authority, PATH_CONTENT + "/" + ContentEntry.COLUMN_AUTHOR, CONTENT_WITH_AUTHOR);
        matcher.addURI(authority, PATH_CONTENT + "/" + ContentEntry.SEARCH, CONTENT_WITH_SEARCH);
        matcher.addURI(authority, PATH_FAVORITE, FAVORITES);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case CONTENT_WITH_SEARCH:
                return ContentEntry.CONTENT_TYPE;
            case CONTENT_WITH_AUTHOR:
                return ContentEntry.CONTENT_TYPE;
            case CONTENT_WITH_TITLE:
                return ContentEntry.CONTENT_TYPE;
            case CONTENT_WITH_TYPE:
                return ContentEntry.CONTENT_TYPE;
            case CONTENT:
                return ContentEntry.CONTENT_TYPE;
            case FAVORITES:
                return FavoritesEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {

            // "content/search/*"
            case CONTENT_WITH_SEARCH: {
                retCursor = getContentBySearch(uri, projection, sortOrder);
                break;
            }
            // "content/title/*"
            case CONTENT_WITH_TITLE: {
                retCursor = getContentByTitle(uri, projection, sortOrder);
                break;
            }
            // "content/type/*"
            case CONTENT_WITH_TYPE: {
                retCursor = getContentByType(uri, projection, sortOrder);
                break;
            }
            // "content/author/*"
            case CONTENT_WITH_AUTHOR: {
                retCursor = getContentByAuthor(uri, projection, sortOrder);
                break;
            }

            // "content"
            case CONTENT: {
                retCursor = queryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            // "favorites"
            case FAVORITES: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        FavoritesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case CONTENT: {
                long _id = db.insert(ContentEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ContentEntry.buildContentUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case FAVORITES: {
                long _id = db.insert(FavoritesEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = FavoritesEntry.buildFavoriteUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if (null == selection) selection = "1";
        switch (match) {
            case CONTENT:
                rowsDeleted = db.delete(
                        ContentEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case FAVORITES:
                rowsDeleted = db.delete(
                        FavoritesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case CONTENT:
                rowsUpdated = db.update(ContentEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case FAVORITES:
                rowsUpdated = db.update(FavoritesEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CONTENT:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(ContentEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
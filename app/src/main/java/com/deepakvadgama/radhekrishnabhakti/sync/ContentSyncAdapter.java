package com.deepakvadgama.radhekrishnabhakti.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.deepakvadgama.radhekrishnabhakti.BuildConfig;
import com.deepakvadgama.radhekrishnabhakti.R;
import com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract;
import com.deepakvadgama.radhekrishnabhakti.pojo.Content;
import com.deepakvadgama.radhekrishnabhakti.util.NotificationUtil;
import com.deepakvadgama.radhekrishnabhakti.util.Utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract.ContentEntry;

public class ContentSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = ContentSyncAdapter.class.getSimpleName();

    // Interval at which to sync with the weather, in milliseconds.
    // 60 seconds (1 minute)  120 = 2 hours
    public static final int SYNC_INTERVAL = 60 * 120;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    private RetrofitService mService;

    public ContentSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        initRetrofit();
    }

    public ContentSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        initRetrofit();
    }

    private void initRetrofit() {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        // set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        // add your other interceptors …

        // add logging as last interceptor
        httpClient.addInterceptor(logging);  // <-- this is the important line!


        Retrofit mRetrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.SERVER_ADDRESS)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        mService = mRetrofit.create(RetrofitService.class);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        // get latest id from DB
        int latestId = getLatestId();
        populateContent(latestId);

        // Get favorites for first time sync
        if (latestId == -1) {
            populateFavorites();
        }
    }

    private void populateContent(int latestId) {
        try {
            final List<Content> list = mService.getContent(latestId).execute().body();
            if (!list.isEmpty()) {
                insertContentIntoDB(list);
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            // This is first time sync so tell UI
            // TODO: Analytics
        }
    }

    private void insertContentIntoDB(List<Content> list) {
        List<ContentValues> contentList = convertToValues(list);
        final ContentValues[] array = contentList.toArray(new ContentValues[contentList.size()]);
        getContext().getContentResolver().bulkInsert(ContentEntry.CONTENT_URI, array);
        NotificationUtil.notify(getContext(), getContentToNotify(list));
    }

    private Content getContentToNotify(List<Content> list) {

        // Preference for Quote or Picture
        for (Content record : list) {
            if ("QUOTE".equalsIgnoreCase(record.type)) {
                return record;
            } else if ("PICTURE".equalsIgnoreCase(record.type)) {
                return record;
            }
        }
        // If no quotes or pics in data set. Send any.
        return list.get(0);
    }

    private List<ContentValues> convertToValues(List<Content> list) {
        List<ContentValues> contentList = new ArrayList<>();
        for (Content record : list) {
            ContentValues content = new ContentValues();
            content.put(ContentEntry._ID, record.id);
            content.put(ContentEntry.COLUMN_TEXT, record.text);
            content.put(ContentEntry.COLUMN_URL, record.url);
            content.put(ContentEntry.COLUMN_AUTHOR, record.author);
            content.put(ContentEntry.COLUMN_TITLE, record.title);
            content.put(ContentEntry.COLUMN_TYPE, record.type);
            contentList.add(content);
        }
        return contentList;
    }

    private void populateFavorites() {
        try {
            final String email = Utility.getEmail(getContext());
            if (email != null) {
                final List<Content> list = mService.getFavorites(email).execute().body();
                if (!list.isEmpty()) {
                    insertFavoritesIntoDB(list);
                }
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }

    private void insertFavoritesIntoDB(List<Content> list) {
        List<ContentValues> contentList = convertToFavorites(list);
        final ContentValues[] array = contentList.toArray(new ContentValues[contentList.size()]);
        getContext().getContentResolver().bulkInsert(DatabaseContract.FavoritesEntry.CONTENT_URI, array);
    }

    private List<ContentValues> convertToFavorites(List<Content> list) {
        List<ContentValues> favorites = new ArrayList<>();
        for (Content record : list) {
            ContentValues content = new ContentValues();
            content.put(DatabaseContract.FavoritesEntry._ID, record.id);
            content.put(DatabaseContract.FavoritesEntry.COLUMN_CONTENT_ID, record.id);
            favorites.add(content);
        }
        return favorites;
    }

    private int getLatestId() {

        int latestId = -1;
        Cursor cursor = getContext().getContentResolver().query(
                ContentEntry.CONTENT_URI,
                new String[]{ContentEntry.TABLE_NAME + "." + ContentEntry._ID},
                null,
                null,
                ContentEntry._ID + " DESC"
        );
        if (cursor.moveToFirst()) {
            latestId = cursor.getInt(0);
        }
        cursor.close();
        return latestId;
    }

    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account, authority, new Bundle(), syncInterval);
        }
    }

    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.content_authority), bundle);
    }


    public static Account getSyncAccount(Context context) {

        // Get an instance of the Android account manager
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        ContentSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}

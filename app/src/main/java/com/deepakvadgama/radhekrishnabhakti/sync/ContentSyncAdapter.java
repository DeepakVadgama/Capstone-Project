package com.deepakvadgama.radhekrishnabhakti.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.deepakvadgama.radhekrishnabhakti.BuildConfig;
import com.deepakvadgama.radhekrishnabhakti.R;
import com.deepakvadgama.radhekrishnabhakti.pojo.Content;
import com.deepakvadgama.radhekrishnabhakti.util.AnalyticsUtil;
import com.deepakvadgama.radhekrishnabhakti.util.NotificationUtil;
import com.deepakvadgama.radhekrishnabhakti.util.PreferenceUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract.ContentEntry;
import static com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract.FavoritesEntry;

public class ContentSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = ContentSyncAdapter.class.getSimpleName();

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

//        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
//        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
//        httpClient.addInterceptor(logging);  // For testing

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

        // Synchronize favorites with server
        if (isFavoritesUpdated()) {
            syncFavoritesWithServer();
        }

        // Set timestamp of sync
        saveTimestampOfSync();
    }

    private void syncFavoritesWithServer() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        final String email = prefs.getString(getContext().getString(R.string.pref_user_email), null);

        if (email != null) {

            // Sync newly added favorites
            final Set<String> addedFavorites = prefs.getStringSet(getContext().getString(R.string.pref_favorites_added), null);
            if (addedFavorites != null) {
                mService.addFavorites(email, addedFavorites);
            }

            // Sync recently removed favorites
            final Set<String> removedFavorites = prefs.getStringSet(getContext().getString(R.string.pref_favorites_removed), null);
            if (removedFavorites != null) {
                mService.removeFavorites(email, removedFavorites);
            }

            // Reset flag
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(getContext().getString(R.string.pref_favorites_updated), false);
            editor.remove(getContext().getString(R.string.pref_favorites_added));
            editor.remove(getContext().getString(R.string.pref_favorites_removed));
            editor.apply();
        }

    }

    private boolean isFavoritesUpdated() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        return prefs.getBoolean(getContext().getString(R.string.pref_favorites_updated), false);
    }

    private void saveTimestampOfSync() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(getContext().getString(R.string.pref_last_notification), System.currentTimeMillis());
        editor.apply();
    }

    private void populateContent(int latestId) {
        try {
            final List<Content> list = mService.getContent(latestId + 1).execute().body();
            if (!list.isEmpty()) {
                insertContentIntoDB(list);
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            AnalyticsUtil.trackSyncError();
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
            final String email = PreferenceUtil.getEmail(getContext());
            if (email != null) {
                final List<Content> list = mService.getFavorites(email).execute().body();
                if (list != null && !list.isEmpty()) {
                    insertFavoritesIntoDB(list);
                }
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            AnalyticsUtil.trackSyncError();
        }
    }

    private void insertFavoritesIntoDB(List<Content> list) {
        List<ContentValues> contentList = convertToFavorites(list);
        final ContentValues[] array = contentList.toArray(new ContentValues[contentList.size()]);
        getContext().getContentResolver().bulkInsert(FavoritesEntry.CONTENT_URI, array);
    }

    private List<ContentValues> convertToFavorites(List<Content> list) {
        List<ContentValues> favorites = new ArrayList<>();
        for (Content record : list) {
            ContentValues content = new ContentValues();
            content.put(FavoritesEntry.COLUMN_CONTENT_ID, record.id);
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
                ContentEntry.TABLE_NAME + "." + ContentEntry._ID + " DESC"
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

    public static void removePeriodicSync(Context context) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        ContentResolver.removePeriodicSync(account, authority, new Bundle());
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
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        final String frequencyStr = preferences.getString(context.getString(R.string.sync_frequency), "120");
        final int syncInterval = Integer.parseInt(frequencyStr) * 60;
        final int syncFlextime = syncInterval / 3;
        ContentSyncAdapter.configurePeriodicSync(context, syncInterval, syncFlextime);

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

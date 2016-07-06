package com.deepakvadgama.radhekrishnabhakti.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class ContentSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static ContentSyncAdapter sContentSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("ContentSyncService", "onCreate - ContentSyncService");
        synchronized (sSyncAdapterLock) {
            if (sContentSyncAdapter == null) {
                sContentSyncAdapter = new ContentSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    /**
     * Return an object that allows the system to invoke
     * the sync adapter.
     */
    @Override
    public IBinder onBind(Intent intent) {
         /*
         * Get the object that allows external processes to call onPerformSync().
         * The object is created
         * in the base class code when the SyncAdapter constructors call super()
         */
        return sContentSyncAdapter.getSyncAdapterBinder();
    }
}
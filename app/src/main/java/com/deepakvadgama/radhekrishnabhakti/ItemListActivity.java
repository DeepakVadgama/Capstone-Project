package com.deepakvadgama.radhekrishnabhakti;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

import com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract;
import com.deepakvadgama.radhekrishnabhakti.sync.ContentSyncAdapter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ItemListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, GoogleApiClient.OnConnectionFailedListener {

    private static final int CONTENT_LOADER = 0;
    private static final int REQUEST_CODE = 10;
    private boolean mTwoPane;
    private ContentAdapter mContentAdapter;
    private LoaderManager.LoaderCallbacks<Cursor> mCallbacks;
    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        // TODO: Replace later with Recycler View (though its complicated due lack of native support).
        // Initialize the adapter. Note that we pass a 'null' Cursor as the third argument. We will pass the adapter a Cursor only when the
        // data has finished loading for the first time (i.e. when the LoaderManager delivers the data to onLoadFinished). Also note
        // that we have passed the '0' flag as the last argument. This prevents the adapter from registering a ContentObserver for the
        // Cursor (the CursorLoader will do this for us!).
        mContentAdapter = new ContentAdapter(this, null, 0);

        // Tablet
        if (findViewById(R.id.item_detail_container) != null) {
            mTwoPane = true;
            mContentAdapter.setTwoPane(true);
        }

        ListView listView = (ListView) findViewById(R.id.item_list);
        listView.setAdapter(mContentAdapter);

        // Main Sauce - Here loader is created if not present, or already created loader is reused.
        getSupportLoaderManager().initLoader(CONTENT_LOADER, null, this);

        ContentSyncAdapter.initializeSyncAdapter(this);

        storeGoogleAccount();
    }

    private void storeGoogleAccount() {
        // Configure sign-in to request the user's ID, email address, and basic profile. ID and
        // basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to GoogleSignIn.API and the options above.
        mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();

        Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"},
                false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
//            mFullName = acct.getDisplayName();
//            mEmail = acct.getEmail();
//            mPhotoUrl = acct.getPhotoUrl();
            // Store in settings.
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // Since activity uses only 1 loader, we are not using id/LOADER_ID
        // TODO: Check projection and sort order
        return new CursorLoader(this,
                DatabaseContract.ContentEntry.CONTENT_URI,
                DatabaseContract.CONTENT_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mContentAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mContentAdapter.swapCursor(null);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // TODO: Failed to connect to Google.
    }
}

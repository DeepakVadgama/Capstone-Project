package com.deepakvadgama.radhekrishnabhakti;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract;
import com.deepakvadgama.radhekrishnabhakti.pojo.Content;
import com.deepakvadgama.radhekrishnabhakti.sync.ContentSyncAdapter;
import com.deepakvadgama.radhekrishnabhakti.util.PreferenceUtil;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link DetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class FavoritesActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, GoogleApiClient.OnConnectionFailedListener {

    public final String LOG_TAG = FavoritesActivity.class.getSimpleName();

    private static final String SELECTED_KEY = "selected_position";
    private static final int CONTENT_LOADER = 0;
    private static final int REQUEST_CODE = 10;
    public static final String ARG_ITEM = "CONTENT_ITEM";
    private boolean mTwoPane;
    private ContentAdapter mContentAdapter;
    private LoaderManager.LoaderCallbacks<Cursor> mCallbacks;
    private GoogleApiClient mGoogleApiClient;
    private int mPosition = ListView.INVALID_POSITION;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        // TODO: Replace later with Recycler View (complicated due lack of native support w/ CursorAdapter).
        // Initialize the adapter. Note that we pass a 'null' Cursor as the third argument. We will pass the adapter a Cursor only when the
        // data has finished loading for the first time (i.e. when the LoaderManager delivers the data to onLoadFinished). Also note
        // that we have passed the '0' flag as the last argument. This prevents the adapter from registering a ContentObserver for the
        // Cursor (the CursorLoader will do this for us!).
        mContentAdapter = new ContentAdapter(this, null, 0);

        // Tablet
        if (findViewById(R.id.item_detail_container) != null) {
            mTwoPane = true;
            mContentAdapter.setTwoPane(true);
            // At this point there is no data, so load fragment after cursor load finishes.
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        mListView = (ListView) findViewById(R.id.item_list);
        mListView.setAdapter(mContentAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                if (mTwoPane) {
                    final Bundle args = new Bundle();
                    args.putParcelable(DetailFragment.ARG_ITEM, (Parcelable) view.getTag(R.id.contentTag));

                    final DetailFragment fragment = new DetailFragment();
                    fragment.setArguments(args);

                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.item_detail_container, fragment)
                            .commit();

                } else {
                    Intent intent = new Intent(view.getContext(), DetailActivity.class);
                    intent.putExtra(DetailFragment.ARG_ITEM, (Parcelable) view.getTag(R.id.contentTag));
                    startActivity(intent);
                }

                mPosition = position;
            }
        });

        // Main Sauce - Here loader is created if not present, or already created loader is reused.
        getSupportLoaderManager().initLoader(CONTENT_LOADER, null, this);

        ContentSyncAdapter.initializeSyncAdapter(this);

        if (!PreferenceUtil.isAccountSelected.get()) {
            selectGoogleAccount();
        }
    }

    private void selectGoogleAccount() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.server_client_id))
                .requestProfile()
                .build();

        // Build a GoogleApiClient with access to GoogleSignIn.API and the options above.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        } else if (requestCode == REQUEST_CODE && resultCode == RESULT_CANCELED) {
            // TODO: If account is not chosen.
//            Snackbar.make(this, R.string.pick_account, Toast.LENGTH_LONG).show();
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            String name = acct.getDisplayName();
            String email = acct.getEmail();
            Uri photoUrl = acct.getPhotoUrl();
            PreferenceUtil.storeUserProfile(this, email, name, photoUrl.toString());

            // To be used for phase 2.
            // String idToken = acct.getIdToken();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // Since activity uses only 1 loader, we are not using id/LOADER_ID
        return new CursorLoader(this,
                DatabaseContract.ContentEntry.CONTENT_URI,
                DatabaseContract.CONTENT_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mContentAdapter.swapCursor(cursor);
        if (mPosition != ListView.INVALID_POSITION) {
            mListView.smoothScrollToPosition(mPosition);
        }

        // If Activity is triggered from Notification or Widget, display appropriate content
        if (getIntent().getParcelableExtra(ARG_ITEM) != null) {

            Content content = getIntent().getParcelableExtra(ARG_ITEM);
            if (mTwoPane) {

                final Bundle args = new Bundle();
                args.putParcelable(DetailFragment.ARG_ITEM, content);

                final DetailFragment fragment = new DetailFragment();
                fragment.setArguments(args);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.item_detail_container, fragment)
                        .commit();

            } else {
                Intent intent = new Intent(this, DetailActivity.class);
                intent.putExtra(DetailFragment.ARG_ITEM, content);
                startActivity(intent);
            }

            return;
        }

        // Tablet, on data load, open details view
        if (mTwoPane) {

            if (mPosition != ListView.INVALID_POSITION) {
                cursor.moveToPosition(mPosition);
            } else {
                cursor.moveToFirst();
            }

            final Bundle args = new Bundle();
            args.putParcelable(DetailFragment.ARG_ITEM, ContentAdapter.converToContent(cursor));

            final DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.item_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mContentAdapter.swapCursor(null);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // TODO: Failed to connect to Google.
        Log.e(LOG_TAG, "Failed to connect to Google");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

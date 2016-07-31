package com.deepakvadgama.radhekrishnabhakti;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.deepakvadgama.radhekrishnabhakti.sync.ContentSyncAdapter;
import com.deepakvadgama.radhekrishnabhakti.util.AnalyticsUtil;
import com.deepakvadgama.radhekrishnabhakti.util.ConnectionUtil;
import com.deepakvadgama.radhekrishnabhakti.util.PreferenceUtil;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

public class BaseActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        SwipeRefreshLayout.OnRefreshListener,
        AdapterView.OnItemClickListener {

    public final String LOG_TAG = BaseActivity.class.getSimpleName();

    protected static final String SELECTED_KEY = "selected_position";
    private GoogleApiClient mGoogleApiClient;
    private static final int REQUEST_CODE = 10;
    private SwipeRefreshLayout mySwipeRefreshLayout;
    protected ContentAdapter mContentAdapter;
    protected int mPosition = ListView.INVALID_POSITION;
    protected ListView mListView;
    protected Tracker mTracker;

    protected void setupTracker() {
        if (!AnalyticsTrackers.isInitialized()) {
            AnalyticsTrackers.initialize(this);
            mTracker = AnalyticsTrackers.getInstance().get(AnalyticsTrackers.Target.APP);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mPosition = position;

        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailFragment.ARG_ITEM, (Parcelable) view.getTag(R.id.contentTag));
        startActivity(intent);
        overridePendingTransition(R.transition.right_in, R.transition.left_out);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
    }

    protected void setToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    protected void setupPullToRefresh() {
        mySwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        if (mySwipeRefreshLayout != null) {
            mySwipeRefreshLayout.setOnRefreshListener(this);
        }
    }

    protected void selectGoogleAccount() {
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
            Log.i(LOG_TAG, "Account selection cancelled");
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            String name = acct.getDisplayName();
            String email = acct.getEmail();
            Uri photoUrl = acct.getPhotoUrl();
            // String idToken = acct.getIdToken(); // Phase 2
            PreferenceUtil.storeUserProfile(this, email, name, photoUrl.toString());
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "Failed to connect to Google");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_feedback:
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse(getString(R.string.mail_to)));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
                startActivity(Intent.createChooser(emailIntent, "Send feedback"));
                AnalyticsUtil.trackFeedback();
                return true;
            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                overridePendingTransition(R.transition.right_in, R.transition.left_out);
                return true;
            case R.id.menu_refresh:
                mySwipeRefreshLayout.setRefreshing(true);
                syncData();
                AnalyticsUtil.manualRefresh();
                return true;
            case android.R.id.home:
                finish();
                overridePendingTransition(R.transition.left_in, R.transition.right_out);
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                overridePendingTransition(R.transition.right_in, R.transition.left_out);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void syncData() {
        ContentSyncAdapter.syncImmediately(this);
        mySwipeRefreshLayout.setRefreshing(false);
        if (!ConnectionUtil.isNetworkAvailable(this)) {
            Snackbar.make(mListView, R.string.internet_not_working_string, Snackbar.LENGTH_LONG).show();
        } else {
            updateEmptyView();
        }
    }

    @Override
    public void onRefresh() {
        syncData();
    }

    protected void updateEmptyView() {
        TextView tv = (TextView) findViewById(R.id.empty_list_view_text);
        View view = findViewById(R.id.empty_list_view);
        if (mContentAdapter.getCount() == 0) {
            if (this instanceof FavoritesActivity) {
                tv.setText(R.string.no_favorites_string);
            } else if (this instanceof SearchActivity) {
                tv.setText(R.string.no_search_results_string);
            } else if (this instanceof MainActivity && !ConnectionUtil.isNetworkAvailable(this)) {
                tv.setText(R.string.internet_not_working_string);
            } else if (this instanceof MainActivity) {
                tv.setText(R.string.connection_to_server_failed_string);
            }
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    protected void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        stopProgressBar();
        mContentAdapter.swapCursor(cursor);
        if (mPosition != ListView.INVALID_POSITION) {
            mListView.smoothScrollToPosition(mPosition);
        }
        updateEmptyView();
    }

    protected void showProgressBar() {
        ProgressBar pb = (ProgressBar) findViewById(R.id.progress_bar);
        pb.setVisibility(ProgressBar.VISIBLE);
    }

    protected void stopProgressBar() {
        ProgressBar pb = (ProgressBar) findViewById(R.id.progress_bar);
        pb.setVisibility(ProgressBar.GONE);
    }
}

package com.deepakvadgama.radhekrishnabhakti;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract;
import com.deepakvadgama.radhekrishnabhakti.pojo.Content;
import com.deepakvadgama.radhekrishnabhakti.sync.ContentSyncAdapter;
import com.deepakvadgama.radhekrishnabhakti.util.PreferenceUtil;
import com.deepakvadgama.radhekrishnabhakti.util.TypefaceSpan;

/**
 * Main Activity which displays list of items
 */
public class MainActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener {

    public final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String SELECTED_KEY = "selected_position";
    private static final int CONTENT_LOADER = 0;
    private static final int REQUEST_CODE = 10;
    public static final String ARG_ITEM = "CONTENT_ITEM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextAppearance(this, R.style.Toolbar_TitleText);
        setSupportActionBar(toolbar);

        SpannableString s = new SpannableString(getTitle());
        s.setSpan(new TypefaceSpan(this, "Philosopher-Regular.ttf"), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setTitle(s);


        // TODO: Replace later with Recycler View (complicated due lack of native support w/ CursorAdapter).
        // Initialize the adapter. Note that we pass a 'null' Cursor as the third argument. We will pass the adapter a Cursor only when the
        // data has finished loading for the first time (i.e. when the LoaderManager delivers the data to onLoadFinished). Also note
        // that we have passed the '0' flag as the last argument. This prevents the adapter from registering a ContentObserver for the
        // Cursor (the CursorLoader will do this for us!).
        mContentAdapter = new ContentAdapter(this, null, 0);

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        mListView = (ListView) findViewById(R.id.item_list);
        mListView.setAdapter(mContentAdapter);
        mListView.setOnItemClickListener(this);

        // Main Sauce - Here loader is created if not present, or already created loader is reused.
        getSupportLoaderManager().initLoader(CONTENT_LOADER, null, this);

        ContentSyncAdapter.initializeSyncAdapter(this);

        if (!PreferenceUtil.isAccountSelected.get()) {
            selectGoogleAccount();
        }

        setupPullToRefresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search_view).getActionView();

        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(getApplicationContext(), SearchActivity.class)));
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(false);
        searchView.setQueryRefinementEnabled(false);
        searchView.setIconifiedByDefault(true); // Do not iconify the widget; expand it by default

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_go_to_favorites) {
            startActivity(new Intent(this, FavoritesActivity.class));
            return true;
        }
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mPosition = position;

        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailFragment.ARG_ITEM, (Parcelable) view.getTag(R.id.contentTag));
        startActivity(intent);
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        showProgressBar();

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
        super.onLoadFinished(loader, cursor);

        // If Activity is triggered from Notification or Widget, open details screen.
        if (getIntent().getParcelableExtra(ARG_ITEM) != null) {
            Content content = getIntent().getParcelableExtra(ARG_ITEM);
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(DetailFragment.ARG_ITEM, content);
            startActivity(intent);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mContentAdapter.swapCursor(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}

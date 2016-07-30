package com.deepakvadgama.radhekrishnabhakti;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.deepakvadgama.radhekrishnabhakti.data.ContentProvider;
import com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract;

/**
 * Activity to display list of favorites
 */
public class FavoritesActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {

    public final String LOG_TAG = FavoritesActivity.class.getSimpleName();

    private static final String SELECTED_KEY = "selected_position";
    private static final int CONTENT_LOADER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        setToolbar();

        mContentAdapter = new ContentAdapter(this, null, 0);

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        mListView = (ListView) findViewById(R.id.item_list);
        mListView.setAdapter(mContentAdapter);
        mListView.setOnItemClickListener(this);

        // Main Sauce - Here loader is created if not present, or already created loader is reused.
        getSupportLoaderManager().initLoader(CONTENT_LOADER, null, this);

        setupPullToRefresh();
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
                ContentProvider.sFavoriteSelection,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        super.onLoadFinished(loader, cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mContentAdapter.swapCursor(null);
    }
}

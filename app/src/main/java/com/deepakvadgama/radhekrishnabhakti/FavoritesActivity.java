package com.deepakvadgama.radhekrishnabhakti;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.deepakvadgama.radhekrishnabhakti.data.ContentProvider;
import com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link DetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class FavoritesActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {

    public final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String SELECTED_KEY = "selected_position";
    private static final int CONTENT_LOADER = 1;
    private boolean mTwoPane;
    private ContentAdapter mContentAdapter;
    private int mPosition = ListView.INVALID_POSITION;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mContentAdapter = new ContentAdapter(this, null, 0);

        // Tablet
        if (findViewById(R.id.item_detail_container) != null) {
            mTwoPane = true;
            mContentAdapter.setTwoPane(true);
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        mListView = (ListView) findViewById(R.id.item_list);
        mListView.setAdapter(mContentAdapter);
        mListView.setOnItemClickListener(this);

        // Main Sauce - Here loader is created if not present, or already created loader is reused.
        getSupportLoaderManager().initLoader(CONTENT_LOADER, null, this);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mTwoPane) {
            final Bundle args = new Bundle();
            args.putParcelable(DetailFragment.ARG_ITEM, (Parcelable) view.getTag(R.id.contentTag));

            final DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.item_detail_container, fragment)
                    .commit();

        } else {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(DetailFragment.ARG_ITEM, (Parcelable) view.getTag(R.id.contentTag));
            startActivity(intent);
        }

        mPosition = position;
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
        mContentAdapter.swapCursor(cursor);
        if (mPosition != ListView.INVALID_POSITION) {
            mListView.smoothScrollToPosition(mPosition);
        }

        // Tablet, on data load, open details view
        if (mTwoPane) {

            // TODO: Is this even required?
            if (mPosition != ListView.INVALID_POSITION) {
                cursor.moveToPosition(mPosition);
            } else {
                cursor.moveToFirst();
            }

            // Consider clicking the itemm instead to avoid this duplication
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
}

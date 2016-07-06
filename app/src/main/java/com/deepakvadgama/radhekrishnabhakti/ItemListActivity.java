package com.deepakvadgama.radhekrishnabhakti;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

import com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ItemListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CONTENT_LOADER = 0;
    private boolean mTwoPane;
    private ContentAdapter mContentAdapter;
    private LoaderManager.LoaderCallbacks<Cursor> mCallbacks;

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
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // Since activity uses only 1 loader, we are not using id/LOADER_ID
        // TODO: Check projection and sort order
        return new CursorLoader(this,
                DatabaseContract.ContentEntry.CONTENT_URI,
                null,
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
}

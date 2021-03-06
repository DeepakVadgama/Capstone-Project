package com.deepakvadgama.radhekrishnabhakti;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

/**
 * An activity representing a single Item detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link MainActivity}.
 */
public class DetailActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        setToolbar();

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity using a fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.ARG_ITEM,
                    getIntent().getParcelableExtra(DetailFragment.ARG_ITEM));

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.item_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if (id == android.R.id.home) {
//            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
//            finish();
//            overridePendingTransition(R.transition.left_out, R.transition.right_in);
//            return true;
//        }
//        if (id == R.id.action_settings) {
//            startActivity(new Intent(this, SettingsActivity.class));
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }
}

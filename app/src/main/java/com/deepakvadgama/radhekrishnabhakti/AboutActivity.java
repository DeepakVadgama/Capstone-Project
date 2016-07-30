package com.deepakvadgama.radhekrishnabhakti;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

/**
 * Activity to display static content about the app
 */
public class AboutActivity extends BaseActivity {

    public final String LOG_TAG = AboutActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        setToolbar();

        final ImageView image = (ImageView) findViewById(R.id.about_image);
        Glide.with(this)
                .load(Uri.parse(getString(R.string.about_page_image_url)))
                .into(image);
    }

}

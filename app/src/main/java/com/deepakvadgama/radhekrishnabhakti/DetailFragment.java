package com.deepakvadgama.radhekrishnabhakti;

import android.app.Activity;
import android.content.ContentValues;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract;
import com.deepakvadgama.radhekrishnabhakti.pojo.Content;
import com.deepakvadgama.radhekrishnabhakti.util.PreferenceUtil;
import com.deepakvadgama.radhekrishnabhakti.util.ShareUtil;
import com.deepakvadgama.radhekrishnabhakti.util.YouTubeUtil;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import static com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract.ContentType.valueOf;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link MainActivity}
 * in two-pane mode (on tablets) or a {@link DetailActivity}
 * on handsets.
 */
public class DetailFragment extends Fragment implements YouTubePlayer.OnInitializedListener {

    public static final String TAG = "DetailFragment";
    public static final String ARG_ITEM = "item";

    // Accept Content directly instead of URI, then load data from Cursor
    // Cursor is not needed here since data is not expected to change (unlike Sunshine app)
    private Content content;

    // Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon screen orientation changes).
    public DetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM)) {
            content = getArguments().getParcelable(ARG_ITEM);
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(ARG_ITEM)) {
            content = savedInstanceState.getParcelable(ARG_ITEM);
        }

        Activity activity = this.getActivity();
        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle(content.getTypeInTitleCase());
        }

        setHasOptionsMenu(true);
    }

    private Drawable getDrawable() {
        if (content.isFavorite) {
            return ContextCompat.getDrawable(getActivity(), R.drawable.ic_favorite_white_24dp);
        } else {
            return ContextCompat.getDrawable(getActivity(), R.drawable.ic_favorite_border_white_24dp);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.item_detail, container, false);

        ImageView imageView = ((ImageView) rootView.findViewById(R.id.image));
        View youTubeView = rootView.findViewById(R.id.youtube_fragment);
        TextView titleView = (TextView) rootView.findViewById(R.id.title);
        TextView quoteView = (TextView) rootView.findViewById(R.id.quote);
        TextView authorView = (TextView) rootView.findViewById(R.id.author);
        TextView textView = (TextView) rootView.findViewById(R.id.story);

        final DatabaseContract.ContentType type = valueOf(content.type);
        switch (type) {
            case QUOTE:
                titleView.setText(content.author);
                quoteView.setText(content.text);
                break;
            case STORY:
                titleView.setText(content.title);
                textView.setText(content.text);
                break;
            case PICTURE:
                titleView.setText(content.title);
                Glide.with(getActivity())
                        .load(Uri.parse(content.url))
                        .into(imageView);
                break;
            case KIRTAN:
            case LECTURE:
                titleView.setText(content.author);

                YouTubePlayerSupportFragment youTubePlayerFragment
                        = (YouTubePlayerSupportFragment) getChildFragmentManager().findFragmentById(R.id.youtube_fragment);
                youTubePlayerFragment.initialize(YouTubeUtil.DEVELOPER_KEY, this);

                break;
        }

        // Set all view invisible
        titleView.setVisibility(View.GONE);
        authorView.setVisibility(View.GONE);
        quoteView.setVisibility(View.GONE);
        youTubeView.setVisibility(View.GONE);
        textView.setVisibility(View.GONE);
        imageView.setVisibility(View.GONE);

        // Set visibility based on type
        switch (type) {
            case QUOTE:
                titleView.setVisibility(View.VISIBLE);
                quoteView.setVisibility(View.VISIBLE);
                break;
            case STORY:
                titleView.setVisibility(View.VISIBLE);
                textView.setVisibility(View.VISIBLE);
                break;
            case PICTURE:
                imageView.setVisibility(View.VISIBLE);
                titleView.setVisibility(View.VISIBLE);
                break;
            case KIRTAN:
            case LECTURE:
                titleView.setVisibility(View.VISIBLE);
                authorView.setVisibility(View.VISIBLE);
                youTubeView.setVisibility(View.VISIBLE);
                break;
        }


        View fabView = getActivity().findViewById(R.id.fab);
        if (fabView != null) {
            FloatingActionButton fab = (FloatingActionButton) fabView;
            fab.setImageDrawable(getDrawable());
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String snackBarText = null;
                    if (!content.isFavorite) {
                        // Update in preferences
                        if (!PreferenceUtil.isAccountSelected.get()) {
                            ((GoogleSignInActivity) getActivity()).selectGoogleAccount();
                        }
                        PreferenceUtil.addToFavorites(getActivity(), content.id);

                        // Update favorites table
                        ContentValues values = new ContentValues();
                        values.put(DatabaseContract.FavoritesEntry.COLUMN_CONTENT_ID, content.id);
                        getActivity().getContentResolver().insert(DatabaseContract.FavoritesEntry.CONTENT_URI, values);

                        content.isFavorite = true;
                        snackBarText = content.getTypeInTitleCase() + " added to favorites";
                        ((FloatingActionButton) view).setImageDrawable(getDrawable());

                    } else {
                        // Update in preferences
                        PreferenceUtil.removeFromFavorites(getActivity(), content.id);

                        // Update favorites table
                        getActivity().getContentResolver().delete(DatabaseContract.FavoritesEntry.CONTENT_URI,
                                DatabaseContract.FavoritesEntry.COLUMN_CONTENT_ID + " = ? ",
                                new String[]{String.valueOf(content.id)});

                        content.isFavorite = false;
                        snackBarText = content.getTypeInTitleCase() + " removed from favorites";
                        ((FloatingActionButton) view).setImageDrawable(getDrawable());
                    }

                    Snackbar.make(view, snackBarText, Snackbar.LENGTH_LONG).show();
                }
            });
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(ARG_ITEM, content);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
        if (!wasRestored) {
//                            youTubePlayer.setFullscreen(true);
            youTubePlayer.loadVideo(YouTubeUtil.getVideoKey(content.url));
            youTubePlayer.play();
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        Log.w(TAG, "Error initializing YouTube");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.detail_fragment, menu);
//
//        // Retrieve the share menu item
//        MenuItem menuItem = menu.findItem(R.id.action_share);
//
//        // Get the provider and hold onto it to set/change the share intent.
//        ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
//
//        // Later: If using cursor, if onLoadFinished happens before this, we can go ahead and set the share intent now.
//        if (content != null) {
//            mShareActionProvider.setShareIntent(ShareUtil.getShareIntent(content));
//        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_share) {
            ShareUtil.share(getContext(), content);
        }
        return true;
    }
}

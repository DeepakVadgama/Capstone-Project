package com.deepakvadgama.radhekrishnabhakti;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.deepakvadgama.radhekrishnabhakti.pojo.Content;
import com.deepakvadgama.radhekrishnabhakti.util.PreferenceUtil;
import com.deepakvadgama.radhekrishnabhakti.util.ShareUtil;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.like.LikeButton;
import com.like.OnLikeListener;

import java.util.HashMap;
import java.util.Map;

import static com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract.ContentType;
import static com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract.ContentType.valueOf;
import static com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract.FavoritesEntry;

public class ContentAdapter extends CursorAdapter {

    private static final int COLUMN_ID = 0;
    private static final int COLUMN_TYPE = 1;
    private static final int COLUMN_TITLE = 2;
    private static final int COLUMN_AUTHOR = 3;
    private static final int COLUMN_URL = 4;
    private static final int COLUMN_TEXT = 5;
    private static final int COLUMN_FAVORITE = 6;
    private static final int RECOVERY_REQUEST = 1;
    private boolean mTwoPane = false;
    private final ThumbnailListener thumbnailListener;
    private final Map<YouTubeThumbnailView, YouTubeThumbnailLoader> thumbnailViewToLoaderMap;


    public static class ViewHolder {

        public final ImageView iconView;

        public final TextView titleView;
        public final TextView textView;
        public final LikeButton saveView;
        public final ImageButton shareView;
        public final YouTubeThumbnailView youtubeView;
        public boolean youtubeInit;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.image);
            titleView = (TextView) view.findViewById(R.id.title);
            textView = (TextView) view.findViewById(R.id.text);
            saveView = (LikeButton) view.findViewById(R.id.save);
            shareView = (ImageButton) view.findViewById(R.id.share);
            youtubeView = (YouTubeThumbnailView) view.findViewById(R.id.youtube_view);
            youtubeInit = false;
        }
    }

    public ContentAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        thumbnailListener = new ThumbnailListener();
        thumbnailViewToLoaderMap = new HashMap<YouTubeThumbnailView, YouTubeThumbnailLoader>();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = LayoutInflater.from(context).inflate(R.layout.item_list_content, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        final Content content = converToContent(cursor);
        ViewHolder h = (ViewHolder) view.getTag();

        final ContentType type = valueOf(content.type);
        switch (type) {
            case QUOTE:
                h.titleView.setText(content.author);
                h.textView.setText(content.text);
                h.textView.setVisibility(View.VISIBLE);
                h.iconView.setVisibility(View.GONE);
                h.youtubeView.setVisibility(View.GONE);
                break;
            case STORY:
                h.titleView.setText(content.title);
                h.textView.setText(content.text);
                h.textView.setVisibility(View.VISIBLE);
                h.iconView.setVisibility(View.GONE);
                h.youtubeView.setVisibility(View.GONE);
                break;
            case PICTURE:
                h.titleView.setText(content.title);
                Glide.with(context)
                        .load(Uri.parse(content.url))
                        .into(h.iconView);
                h.textView.setVisibility(View.GONE);
                h.iconView.setVisibility(View.VISIBLE);
                h.youtubeView.setVisibility(View.GONE);
                break;
            case KIRTAN:
                h.titleView.setText(content.author);
                h.youtubeView.setTag(content.url);
                if (!h.youtubeInit) {
                    h.youtubeView.initialize(DeveloperKey.DEVELOPER_KEY, thumbnailListener);
                }
                h.textView.setVisibility(View.GONE);
                h.iconView.setVisibility(View.GONE);
                h.youtubeView.setVisibility(View.VISIBLE);
                break;
            case LECTURE:
                h.titleView.setText(content.title);
                h.youtubeView.setTag(content.url);
                if (!h.youtubeInit) {
                    h.youtubeView.initialize(DeveloperKey.DEVELOPER_KEY, thumbnailListener);
                }
                h.textView.setVisibility(View.GONE);
                h.iconView.setVisibility(View.GONE);
                h.youtubeView.setVisibility(View.VISIBLE);
                break;
        }

        // Set click listener on save
        h.saveView.setTag(content.id);
        h.saveView.setLiked(content.isFavorite);
        h.saveView.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                int id = (Integer) likeButton.getTag();

                // Update in preferences
                PreferenceUtil.addToFavorites(context, id);

                // Update favorites table
                // TODO: Later consider doing this in background
                ContentValues values = new ContentValues();
                values.put(FavoritesEntry.COLUMN_CONTENT_ID, content.id);
                context.getContentResolver().insert(FavoritesEntry.CONTENT_URI, values);
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                int id = (Integer) likeButton.getTag();

                // Update in preferences
                PreferenceUtil.removeFromFavorites(context, id);

                // Update favorites table
                // TODO: Later consider doing this in background
                context.getContentResolver().delete(FavoritesEntry.CONTENT_URI,
                        FavoritesEntry.COLUMN_CONTENT_ID + " = ? ",
                        new String[]{String.valueOf(content.id)});
            }
        });

        // Set click listener on share
        h.shareView.setTag(content);
        h.shareView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent shareIntent = ShareUtil.getShareIntent((Content) v.getTag());
                context.startActivity(shareIntent);
            }
        });
    }


    @NonNull
    private Content converToContent(Cursor cursor) {
        Content content = new Content();
        content.id = cursor.getInt(COLUMN_ID);
        content.title = cursor.getString(COLUMN_TITLE);
        content.text = cursor.getString(COLUMN_TEXT);
        content.type = cursor.getString(COLUMN_TYPE);
        content.url = cursor.getString(COLUMN_URL);
        content.author = cursor.getString(COLUMN_AUTHOR);
        content.isFavorite = cursor.getString(COLUMN_FAVORITE) != null;
        return content;
    }

    public boolean ismTwoPane() {
        return mTwoPane;
    }

    public void setTwoPane(boolean mTwoPane) {
        this.mTwoPane = mTwoPane;
    }

//    private class MyYouTubePlayerInitializer implements YouTubePlayer.OnInitializedListener {
//
//        private String videoUrl;
//
//        public MyYouTubePlayerInitializer(String videoUrl) {
//            this.videoUrl = videoUrl;
//        }
//
//        @Override
//        public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
//            if (!wasRestored) {
//                player.cueVideo(videoUrl);
//            }
//        }
//
//        @Override
//        public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult errorReason) {
//            // TODO: Take care of this.
////            if (errorReason.isUserRecoverableError()) {
////                errorReason.getErrorDialog(this, RECOVERY_REQUEST).show();
////            } else {
////                String error = String.format(getString(R.string.player_error), errorReason.toString());
////                Toast.makeText(, error, Toast.LENGTH_LONG).show();
////            }
//        }
//    }

    private final class ThumbnailListener implements
            YouTubeThumbnailView.OnInitializedListener,
            YouTubeThumbnailLoader.OnThumbnailLoadedListener {

        @Override
        public void onInitializationSuccess(
                YouTubeThumbnailView view, YouTubeThumbnailLoader loader) {
            loader.setOnThumbnailLoadedListener(this);
            thumbnailViewToLoaderMap.put(view, loader);
            String videoUrl = (String) view.getTag();
            final String videoKey = videoUrl.split("v=")[1];
            loader.setVideo(videoKey);
        }

        @Override
        public void onInitializationFailure(
                YouTubeThumbnailView view, YouTubeInitializationResult loader) {
            view.setImageResource(R.mipmap.no_thumbnail);
        }

        @Override
        public void onThumbnailLoaded(YouTubeThumbnailView view, String videoId) {
        }

        @Override
        public void onThumbnailError(YouTubeThumbnailView view, YouTubeThumbnailLoader.ErrorReason errorReason) {
            view.setImageResource(R.mipmap.no_thumbnail);
        }
    }

    public void releaseLoaders() {
        for (YouTubeThumbnailLoader loader : thumbnailViewToLoaderMap.values()) {
            loader.release();
        }
    }
}
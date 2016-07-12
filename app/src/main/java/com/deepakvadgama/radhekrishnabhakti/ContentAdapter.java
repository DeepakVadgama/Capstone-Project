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
import com.like.LikeButton;
import com.like.OnLikeListener;

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

    private boolean mTwoPane = false;

    public static class ViewHolder {

        public final ImageView iconView;

        public final TextView titleView;
        public final TextView textView;
        public final LikeButton saveView;
        public final ImageButton shareView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.image);
            titleView = (TextView) view.findViewById(R.id.title);
            textView = (TextView) view.findViewById(R.id.text);
            saveView = (LikeButton) view.findViewById(R.id.save);
            shareView = (ImageButton) view.findViewById(R.id.share);
        }
    }

    public ContentAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
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
                break;
            case STORY:
                h.titleView.setText(content.title);
                h.textView.setText(content.text);
                h.textView.setVisibility(View.VISIBLE);
                h.iconView.setVisibility(View.GONE);
                break;
            case PICTURE:
                h.titleView.setText(content.title);
                Glide.with(context)
                        .load(Uri.parse(content.url))
                        .into(h.iconView);
                h.textView.setVisibility(View.GONE);
                h.iconView.setVisibility(View.VISIBLE);
                break;
            case KIRTAN:
                h.titleView.setText(content.author);
                h.textView.setVisibility(View.GONE);
                h.iconView.setVisibility(View.VISIBLE);
                break;
            case LECTURE:
                h.titleView.setText(content.title);
                h.textView.setVisibility(View.GONE);
                h.iconView.setVisibility(View.VISIBLE);
                break;
        }

        // Set click listener on save
        h.saveView.setTag(COLUMN_ID, content.id);
        h.saveView.setLiked(content.isFavorite);
        h.saveView.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                int id = (Integer) likeButton.getTag(COLUMN_ID);

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
                int id = (Integer) likeButton.getTag(COLUMN_ID);

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
}
package com.deepakvadgama.radhekrishnabhakti;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.like.LikeButton;
import com.like.OnLikeListener;

import static com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract.ContentType;
import static com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract.ContentType.valueOf;

public class ContentAdapter extends CursorAdapter {

    private static final int COLUMN_ID = 0;
    private static final int COLUMN_TYPE = 1;
    private static final int COLUMN_TITLE = 2;
    private static final int COLUMN_AUTHOR = 3;
    private static final int COLUMN_URL = 4;
    private static final int COLUMN_TEXT = 5;
    private static final int COLUMN_IS_FAVORITE = 6;

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
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder h = (ViewHolder) view.getTag();

        final ContentType type = valueOf(cursor.getString(COLUMN_TYPE));
        switch (type) {
            case QUOTE:
                h.titleView.setText(cursor.getString(COLUMN_AUTHOR));
                h.textView.setText(cursor.getString(COLUMN_TEXT));
                h.textView.setVisibility(View.VISIBLE);
                h.iconView.setVisibility(View.GONE);
            case STORY:
                h.titleView.setText(cursor.getString(COLUMN_TITLE));
                h.textView.setText(cursor.getString(COLUMN_TEXT));
                h.textView.setVisibility(View.VISIBLE);
                h.iconView.setVisibility(View.GONE);
            case PICTURE:
                h.titleView.setText(cursor.getString(COLUMN_TITLE));
                Glide.with(context)
                        .load(Uri.parse(cursor.getString(COLUMN_URL)))
                        .into(h.iconView);
                h.textView.setVisibility(View.GONE);
                h.iconView.setVisibility(View.VISIBLE);
            case KIRTAN:
                h.titleView.setText(cursor.getString(COLUMN_AUTHOR));
                h.textView.setVisibility(View.GONE);
                h.iconView.setVisibility(View.VISIBLE);
            case LECTURE:
                h.titleView.setText(cursor.getString(COLUMN_TITLE));
                h.textView.setVisibility(View.GONE);
                h.iconView.setVisibility(View.VISIBLE);
        }

        // Set click listener on save
        h.saveView.setOnLikeListener(new CustomLikeListener(cursor.getInt(COLUMN_ID)));

        // Set click listener on share
        h.shareView.setOnClickListener(new CustomClickListener(cursor.getInt(COLUMN_ID)));
        h.shareView.setTag();
    }

    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, );
        return shareIntent;
    }

    private class CustomClickListener implements View.OnClickListener {

        private int contentId;

        public CustomClickListener(int contentId) {
            this.contentId = contentId;
        }

        @Override
        public void onClick(View v) {
            final View parent = v.getTag()
        }
    }

    private class CustomLikeListener implements OnLikeListener {

        private int contentId;

        public CustomLikeListener(int contentId) {
            this.contentId = contentId;
        }

        @Override
        public void liked(LikeButton likeButton) {

        }

        @Override
        public void unLiked(LikeButton likeButton) {

        }
    }

    ;

    public boolean ismTwoPane() {
        return mTwoPane;
    }

    public void setTwoPane(boolean mTwoPane) {
        this.mTwoPane = mTwoPane;
    }
}
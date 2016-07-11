package com.deepakvadgama.radhekrishnabhakti;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class ContentAdapter extends CursorAdapter {

    private boolean mTwoPane = false;

    public static class ViewHolder {

        public final ImageView iconView;
        public final TextView titleView;
        public final TextView textView;
        public final ImageButton saveView;
        public final ImageButton shareView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.image);
            titleView = (TextView) view.findViewById(R.id.title);
            textView = (TextView) view.findViewById(R.id.text);
            saveView = (ImageButton) view.findViewById(R.id.save);
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

        ViewHolder holder = (ViewHolder) view.getTag();

        // Based on type set title or author
        // Based on type set set image or text
        // Set click listener on share and save
    }

    public boolean ismTwoPane() {
        return mTwoPane;
    }

    public void setTwoPane(boolean mTwoPane) {
        this.mTwoPane = mTwoPane;
    }
}
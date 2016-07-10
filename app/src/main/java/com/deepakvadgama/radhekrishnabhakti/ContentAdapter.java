package com.deepakvadgama.radhekrishnabhakti;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * {@link ContentAdapter} exposes a list of content items
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ContentAdapter extends CursorAdapter {

    private boolean mTwoPane = false;

    public ContentAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        // TODO: Is there a way to optimize this infating for each view card?
        View view = LayoutInflater.from(context).inflate(R.layout.item_list_content, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
//        TextView tv = (TextView) view;
//        tv.setText(convertCursorRowToUXFormat(cursor));
    }

    public boolean ismTwoPane() {
        return mTwoPane;
    }

    public void setTwoPane(boolean mTwoPane) {
        this.mTwoPane = mTwoPane;
    }
}
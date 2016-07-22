/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.deepakvadgama.radhekrishnabhakti.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import com.deepakvadgama.radhekrishnabhakti.ContentAdapter;
import com.deepakvadgama.radhekrishnabhakti.MainActivity;
import com.deepakvadgama.radhekrishnabhakti.R;
import com.deepakvadgama.radhekrishnabhakti.pojo.Content;

import static com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract.CONTENT_COLUMNS;
import static com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract.ContentEntry;
import static com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract.ContentType;

/**
 * IntentService which handles updating all widgets with the latest data
 */
public class QuoteWidgetIntentService extends IntentService {

    public QuoteWidgetIntentService() {
        super("QuoteWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // Retrieve all of the widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, QuotesWidget.class));

        // get last quote shown
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final String lastQuoteId = getString(R.string.last_quote_id);
        final int content_id = prefs.getInt(lastQuoteId, -1);

        // get next quote
        Cursor data = getContentResolver().query(
                ContentEntry.CONTENT_URI,
                CONTENT_COLUMNS,
                ContentEntry.TABLE_NAME + "." + ContentEntry._ID + " > ? AND " + ContentEntry.TABLE_NAME + "." + ContentEntry.COLUMN_TYPE + " = ?",
                new String[]{String.valueOf(content_id), ContentType.QUOTE.toString()},
                ContentEntry.TABLE_NAME + "." + ContentEntry._ID + " ASC");

        if (data == null) {
            return;
        }
        if (!data.moveToFirst()) {
            data.close();
            return;
        }

        // Extract the content data from the Cursor
        final Content content = ContentAdapter.converToContent(data);
        data.close();

        // update the id in preferences
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(lastQuoteId, content.id);
        editor.apply();

        // Perform this loop procedure for each widget
        for (int appWidgetId : appWidgetIds) {

            RemoteViews views = new RemoteViews(getPackageName(), R.layout.quotes_widget);

            // Add the data to the RemoteViews
//            views.setImageViewResource(R.id.widget_icon, weatherArtResourceId);

            // Content Descriptions for RemoteViews were only added in ICS MR1
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                setRemoteContentDescription(views, content.text);
            }
            views.setTextViewText(R.id.widget_description, "\"" + content.text + "\"");
            views.setTextViewText(R.id.widget_author, "- " + content.author);

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, String description) {
//        views.setContentDescription(R.id.widget_icon, description);
    }
}

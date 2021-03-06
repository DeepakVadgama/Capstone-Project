package com.deepakvadgama.radhekrishnabhakti.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.deepakvadgama.radhekrishnabhakti.util.AnalyticsUtil;

/**
 * Implementation of App Widget functionality.
 */
public class QuotesWidget extends AppWidgetProvider {


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        context.startService(new Intent(context, QuoteWidgetIntentService.class));
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        context.startService(new Intent(context, QuoteWidgetIntentService.class));
    }

    @Override
    public void onEnabled(Context context) {
        AnalyticsUtil.trackQuoteWidget("Added");
    }

    @Override
    public void onDisabled(Context context) {
        AnalyticsUtil.trackQuoteWidget("Added");
    }
}


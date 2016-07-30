package com.deepakvadgama.radhekrishnabhakti.util;

import com.deepakvadgama.radhekrishnabhakti.AnalyticsTrackers;
import com.google.android.gms.analytics.HitBuilders;

import static com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract.ContentType;

public class AnalyticsUtil {

    public static void trackShare(ContentType type) {
        AnalyticsTrackers.getDefaultTracker().send(new HitBuilders.EventBuilder()
                .setCategory("Actions")
                .setAction("Share")
                .setLabel(type.toString())
                .build());
    }

    public static void trackFavorite(String contentType) {
        AnalyticsTrackers.getDefaultTracker().send(new HitBuilders.EventBuilder()
                .setCategory("Actions")
                .setAction("Favorite")
                .setLabel(contentType)
                .build());
    }

    public static void trackSearch(String query) {
        AnalyticsTrackers.getDefaultTracker().send(new HitBuilders.EventBuilder()
                .setCategory("Actions")
                .setAction("Search")
                .setLabel(query)
                .build());
    }

    public static void trackQuoteWidget(String action) {
        AnalyticsTrackers.getDefaultTracker().send(new HitBuilders.EventBuilder()
                .setCategory("Actions")
                .setAction("Widget")
                .setLabel(action)
                .build());
    }

    public static void trackSyncError() {
        AnalyticsTrackers.getDefaultTracker().send(new HitBuilders.EventBuilder()
                .setCategory("Data")
                .setAction("Sync")
                .setLabel("Error")
                .build());
    }

    public static void manualRefresh() {
        AnalyticsTrackers.getDefaultTracker().send(new HitBuilders.EventBuilder()
                .setCategory("Data")
                .setAction("Sync")
                .setLabel("Manual")
                .build());
    }

    public static void trackFeedback() {
        AnalyticsTrackers.getDefaultTracker().send(new HitBuilders.EventBuilder()
                .setCategory("Actions")
                .setAction("Feedback")
                .setLabel("Sent")
                .build());

    }
}

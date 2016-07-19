package com.deepakvadgama.radhekrishnabhakti.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v4.app.TaskStackBuilder;

import com.deepakvadgama.radhekrishnabhakti.MainActivity;
import com.deepakvadgama.radhekrishnabhakti.R;
import com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract;
import com.deepakvadgama.radhekrishnabhakti.pojo.Content;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NotificationUtil {

    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int NOTIFICATION_ID = 0;

    public static void notify(Context context, Content content) {

        //checking the last update and notify if it' the first of the day
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key);
        boolean displayNotifications = prefs.getBoolean(displayNotificationsKey, true);

        if (displayNotifications) {

            String lastNotificationKey = context.getString(R.string.pref_last_notification);
            long lastSync = prefs.getLong(lastNotificationKey, 0);

            if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {

                String title = "";
                String contentText = "";
                Bitmap largeIcon = null;

                // Define the text of the forecast.
                final DatabaseContract.ContentType type = DatabaseContract.ContentType.valueOf(content.type);
                switch (type) {
                    case QUOTE:
                        title = "Quote - " + content.author;
                        contentText = "\" " + content.text + "\"";
                        break;
                    case STORY:
                        title = "Story - " + content.title;
                        contentText = content.text.substring(0, 140) + "...";
                        break;
                    case PICTURE:
                        title = "Picture - " + content.title;
                        largeIcon = getBitmapFromURL(content.url);
                        break;
                    case KIRTAN:
                        title = "Kirtan - " + content.title;
                        largeIcon = getBitmapFromURL(content.url);
                        break;
                    case LECTURE:
                        title = "Lecture - " + content.title;
                        largeIcon = getBitmapFromURL(YouTubeUtil.getThumbnailUrlString(content.url));
                        break;
                }

                // NotificationCompatBuilder is a very convenient way to build backward-compatible
                // notifications.  Just throw in some data.
                Notification.Builder mBuilder =
                        new Notification.Builder(context)
//                                .setColor(resources.getColor(R.color.sunshine_light_blue))
                                .setSmallIcon(R.drawable.ic_favorite_white_24dp) // Change later
                                .setContentTitle(title)
                                .setContentText(contentText);

                if (largeIcon != null) {
                    mBuilder.setLargeIcon(largeIcon);
                    mBuilder.setStyle(new Notification.BigPictureStyle()
                            .bigPicture(largeIcon)
                            .setBigContentTitle(title)
                            .setSummaryText(title));
                }

                Intent resultIntent = new Intent(context, MainActivity.class);
                resultIntent.putExtra(MainActivity.ARG_ITEM, content);

                // The stack builder object will contain an artificial back stack for the started Activity.
                // This ensures that navigating backward from the Activity leads out of
                // your application to the Home screen.
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                mBuilder.setContentIntent(resultPendingIntent);

                NotificationManager mNotificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

                //refreshing last sync
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong(lastNotificationKey, System.currentTimeMillis());
                editor.commit();

            }
        }
    }

    public static Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            return null;
        }
    }
}

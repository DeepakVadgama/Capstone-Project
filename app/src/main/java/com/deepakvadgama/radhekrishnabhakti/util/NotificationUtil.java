package com.deepakvadgama.radhekrishnabhakti.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.deepakvadgama.radhekrishnabhakti.MainActivity;
import com.deepakvadgama.radhekrishnabhakti.R;
import com.deepakvadgama.radhekrishnabhakti.pojo.Content;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract.ContentType;

public class NotificationUtil {

    private static final long FOUR_HOURS = 4 * 60 * 1000;
    private static final int NOTIFICATION_ID = 0;

    public static void notify(Context context, Content content) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        //checking if notifications are enabled.
        if (isNotificationEnabled(context, prefs)) {

            long lastSync = prefs.getLong(context.getString(R.string.pref_last_notification), 0);

            //checking the last update and notify if it' the first of the day
            if (System.currentTimeMillis() - lastSync >= 0) {

                String title = "";
                String contentText = "";

                final ContentType type = ContentType.valueOf(content.type);
                switch (type) {
                    case QUOTE:
                        title = "Quote - " + content.author;
                        contentText = "\" " + content.text + "\"";
                        break;
                    case STORY:
                        title = "Story - " + content.title;
                        contentText = content.text.substring(0, 140) + "...";
                        break;
                    default:
                        // Images cannot be loaded in main thread, thus this async task & duplication of code
                        new LoadPictureAndNotifyTask(context, content).execute();
                        return;
                }

                Notification.Builder mBuilder = new Notification.Builder(context);
                mBuilder.setSmallIcon(R.drawable.feather_white)
                        .setContentTitle(title)
                        .setContentText(contentText);

                String ringTone = prefs.getString(context.getString(R.string.notifications_ringtone), null);
                if (ringTone != null) {
                    mBuilder.setSound(Uri.parse(ringTone));
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
                editor.putLong(context.getString(R.string.pref_last_notification), System.currentTimeMillis());
                editor.apply();

            }
        }
    }

    private static boolean isNotificationEnabled(Context context, SharedPreferences prefs) {
        String displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key);
        return prefs.getBoolean(displayNotificationsKey, true);
    }

    static class LoadPictureAndNotifyTask extends AsyncTask<Void, Void, Bitmap> {

        private final Context context;
        private final Content content;

        LoadPictureAndNotifyTask(Context context, Content content) {
            this.context = context;
            this.content = content;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {

                String urlString = content.url;
                final ContentType type = ContentType.valueOf(content.type);
                if (type == ContentType.LECTURE || type == ContentType.KIRTAN) {
                    urlString = YouTubeUtil.getThumbnailUrlString(content.url);
                }

                return getBitmapFromURL(urlString);

            } catch (Exception ex) {
                Log.w("NOTIFICATION", "Download image for notification failed - ", ex);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result == null) {
                return;
            }


            final ContentType type = ContentType.valueOf(content.type);
            String title = null;
            switch (type) {
                case PICTURE:
                    title = "Picture - " + content.title;
                    break;
                case KIRTAN:
                    title = "Kirtan - " + content.title;
                    break;
                case LECTURE:
                    title = "Lecture - " + content.title;
                    break;
            }

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            Notification.Builder mBuilder = new Notification.Builder(context);
            mBuilder.setSmallIcon(R.drawable.feather_white)
                    .setContentTitle(title);

            String ringTone = prefs.getString(context.getString(R.string.notifications_ringtone), null);
            if (ringTone != null) {
                mBuilder.setSound(Uri.parse(ringTone));
            }

            mBuilder.setLargeIcon(result);
            mBuilder.setStyle(new Notification.BigPictureStyle()
                    .bigPicture(result)
                    .setBigContentTitle(title));

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
            editor.putLong(context.getString(R.string.pref_last_notification), System.currentTimeMillis());
            editor.apply();
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
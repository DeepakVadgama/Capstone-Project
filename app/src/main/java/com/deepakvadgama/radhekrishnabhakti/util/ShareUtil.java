package com.deepakvadgama.radhekrishnabhakti.util;

import android.content.Intent;
import android.net.Uri;

import com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract;
import com.deepakvadgama.radhekrishnabhakti.pojo.Content;

import static com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract.ContentType.valueOf;

public class ShareUtil {

    public static Intent getShareIntent(Content content) {

        final Intent shareIntent = createShareIntent();
        final DatabaseContract.ContentType type = valueOf(content.type);
        switch (type) {
            case QUOTE:
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, content.text + " - " + content.author);
                break;
            case STORY:
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, content.title + " \n\n " + content.text);
                break;
            case PICTURE:
                shareIntent.setType("image/jpeg");
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(content.url));
                break;
            case KIRTAN:
            case LECTURE:
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, content.url);
                break;
        }

        return shareIntent;
    }

    private static Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        return shareIntent;
    }
}

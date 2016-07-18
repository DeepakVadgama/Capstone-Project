package com.deepakvadgama.radhekrishnabhakti.util;

import android.content.Context;
import android.content.Intent;

import com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract;
import com.deepakvadgama.radhekrishnabhakti.pojo.Content;

import static com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract.ContentType.PICTURE;
import static com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract.ContentType.valueOf;

public class ShareUtil {

    public static void share(Context context, Content content) {
        final DatabaseContract.ContentType type = valueOf(content.type);
        if (type == PICTURE) {
            SharePicturetask sharePicturetask = new SharePicturetask(context, content);
            sharePicturetask.execute();
        } else {
            Intent shareIntent = getShareIntent(content);
            context.startActivity(Intent.createChooser(shareIntent, "Share " + content.getTypeInTitleCase()));
        }
    }

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
//            case PICTURE:
//                final Uri pictureStream = getFilePictureStream(context, content.url);
//                shareIntent.setType("image/*");
//                shareIntent.putExtra(Intent.EXTRA_STREAM, pictureStream);
//                break;
            case KIRTAN:
            case LECTURE:
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, content.url);
                break;
        }

        return shareIntent;
    }

    public static Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        return shareIntent;
    }

}

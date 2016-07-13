package com.deepakvadgama.radhekrishnabhakti.util;

import android.net.Uri;

public class YouTubeUtil {


    public static final String YOUTUBE_IMAGE_BASE_URL = "http://img.youtube.com/vi/";
    public static final String IMAGE_FILE_NAME = "0.jpg"; // or hqdefault.jpg

    public static Uri getThumbnailUrl(String videoUrl) {
        String videoKey = videoUrl.split("v=")[1];

        Uri imageUri = Uri.parse(YOUTUBE_IMAGE_BASE_URL).buildUpon()
                .appendPath(videoKey)
                .appendPath(IMAGE_FILE_NAME)
                .build();

        return imageUri;
    }
}

package com.deepakvadgama.radhekrishnabhakti.util;

import static com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract.ContentType;

public class ContentUtil {

    public static boolean isTextType(String string) {
        final ContentType type = ContentType.valueOf(string);
        return ContentType.QUOTE.equals(type) || ContentType.STORY.equals(type);
    }

    public static boolean isAuthor(String string) {
        final ContentType type = ContentType.valueOf(string);
        return ContentType.QUOTE.equals(type);
    }


}

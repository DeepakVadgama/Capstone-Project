package com.deepakvadgama.radhekrishnabhakti.util;

import java.util.HashMap;
import java.util.Map;

import static com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract.ContentType;

public class SearchUtil {

    public static Map<String, String> searchMap = new HashMap<>();

    static {
        searchMap.put("quotes", "QUOTE");
        searchMap.put("kirtans", "KIRTAN");
        searchMap.put("lectures", "LECTURE");
        searchMap.put("pictures", "PICTURE");
        searchMap.put("stories", "STORY");
    }

    public static String mapQuery(String query) {

        if (query == null || query.isEmpty()) {
            return "";
        }

        try {
            ContentType.valueOf(query.toUpperCase());
            query = query.toUpperCase();
            return query;
        } catch (IllegalArgumentException e) {
        }

        if (searchMap.containsKey(query.toLowerCase())) {
            return searchMap.get(query);
        }

        return query;
    }
}

package com.deepakvadgama.radhekrishnabhakti.util;

import com.deepakvadgama.radhekrishnabhakti.data.DatabaseContract;

import java.util.HashMap;
import java.util.Map;

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
            DatabaseContract.ContentType.valueOf(query.toUpperCase());
            query = query.toUpperCase();
            return query;
        } catch (IllegalArgumentException e) {
        }

        if (searchMap.containsKey(query)) {
            return searchMap.get(query);
        }

        return query;
    }
}

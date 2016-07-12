/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.deepakvadgama.radhekrishnabhakti.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.deepakvadgama.radhekrishnabhakti.R;

import java.util.HashSet;
import java.util.Set;

public class PreferenceUtil {

    public static String getEmail(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_user_email), null);
    }

    public static String getName(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_user_name), null);
    }

    public static String getProfileUrl(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_user_url), null);
    }

    public static void storeUserProfile(Context context, String email, String name, String url) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(context.getString(R.string.pref_user_email), email);
        editor.putString(context.getString(R.string.pref_user_name), name);
        editor.putString(context.getString(R.string.pref_user_url), url);
        editor.commit();
    }

    public static void addToFavorites(Context context, int id) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        final Set<String> stringSet = prefs.getStringSet(context.getString(R.string.pref_favorites_added), new HashSet<String>());
        stringSet.add(String.valueOf(id));
        editor.putStringSet(context.getString(R.string.pref_favorites_added), stringSet);
    }


    public static void removeFromFavorites(Context context, int id) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        final Set<String> stringSet = prefs.getStringSet(context.getString(R.string.pref_favorites_added), new HashSet<String>());
        stringSet.remove(String.valueOf(id));
        editor.putStringSet(context.getString(R.string.pref_favorites_added), stringSet);
    }
}
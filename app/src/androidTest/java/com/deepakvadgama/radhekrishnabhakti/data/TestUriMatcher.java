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
package com.deepakvadgama.radhekrishnabhakti.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

public class TestUriMatcher extends AndroidTestCase {

    private static final String TEST_TYPE_DATA = "Quote";
    private static final String TEST_SEARCH_DATA = "random";
    private static final String TEST_AUTHOR_DATA = "Swami Vivekanand";
    private static final String TEST_TITLE_DATA = "Life and Rebirth";

    private static final Uri TEST_CONTENT_DIR = DatabaseContract.ContentEntry.CONTENT_URI;
    private static final Uri TEST_FAVORITES_DIR = DatabaseContract.FavoritesEntry.CONTENT_URI;
    private static final Uri TEST_CONTENT_WITH_TYPE = DatabaseContract.ContentEntry.buildContentSearchWithType(TEST_TYPE_DATA);
    private static final Uri TEST_CONTENT_WITH_TITLE = DatabaseContract.ContentEntry.buildContentSearchWithTitle(TEST_TITLE_DATA);
    private static final Uri TEST_CONTENT_WITH_AUTHOR = DatabaseContract.ContentEntry.buildContentSearchWithAuthor(TEST_AUTHOR_DATA);
    private static final Uri TEST_CONTENT_WITH_SEARCH = DatabaseContract.ContentEntry.buildContentSearchWithAny(TEST_SEARCH_DATA);

    public void testUriMatcher() {
        UriMatcher testMatcher = ContentProvider.buildUriMatcher();

        assertEquals("Error: The Content URL was matched incorrectly.",
                testMatcher.match(TEST_CONTENT_DIR), ContentProvider.CONTENT);
        assertEquals("Error: The Favorites URL was matched incorrectly.",
                testMatcher.match(TEST_FAVORITES_DIR), ContentProvider.FAVORITES);

        assertEquals("Error: The Content With Type was matched incorrectly.",
                testMatcher.match(TEST_CONTENT_WITH_TYPE), ContentProvider.CONTENT_WITH_TYPE);
        assertEquals("Error: The Content With Title was matched incorrectly.",
                testMatcher.match(TEST_CONTENT_WITH_TITLE), ContentProvider.CONTENT_WITH_TITLE);
        assertEquals("Error: The Content With Author was matched incorrectly.",
                testMatcher.match(TEST_CONTENT_WITH_AUTHOR), ContentProvider.CONTENT_WITH_AUTHOR);
        assertEquals("Error: The Content With Search was matched incorrectly.",
                testMatcher.match(TEST_CONTENT_WITH_SEARCH), ContentProvider.CONTENT_WITH_SEARCH);
    }
}

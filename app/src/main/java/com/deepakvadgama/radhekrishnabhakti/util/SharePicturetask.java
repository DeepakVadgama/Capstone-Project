/*
 * Copyright 2015 Google Inc.
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
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.deepakvadgama.radhekrishnabhakti.R;
import com.deepakvadgama.radhekrishnabhakti.pojo.Content;

import java.io.File;

/**
 * An AsyncTask which retrieves a File from the Glide cache then shares it.
 */
class SharePicturetask extends AsyncTask<Void, Void, File> {

    private final Context context;
    private final Content content;

    SharePicturetask(Context context, Content content) {
        this.context = context;
        this.content = content;
    }

    @Override
    protected File doInBackground(Void... params) {
        try {
            return Glide
                    .with(context)
                    .load(content.url)
                    .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .get();
        } catch (Exception ex) {
            Log.w("SHARE", "Sharing " + content.url + " failed", ex);
            return null;
        }
    }

    @Override
    protected void onPostExecute(File result) {
        if (result == null) {
            return;
        }

        // glide cache uses an unfriendly & extension-less name, massage it based on the original
        String fileName = content.url.substring(content.url.lastIndexOf('/') + 1);
        File renamed = new File(result.getParent(), fileName);
        result.renameTo(renamed);
        Uri uri = FileProvider.getUriForFile(context, context.getString(R.string.share_authority), renamed);

        final Intent shareIntent = ShareUtil.createShareIntent();
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);

        context.startActivity(Intent.createChooser(shareIntent, "Share Picture"));
    }
}

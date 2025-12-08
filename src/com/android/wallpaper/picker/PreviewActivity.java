/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.android.wallpaper.picker;

import android.content.Context;
import android.content.Intent;

import com.android.wallpaper.model.InlinePreviewIntentFactory;
import com.android.wallpaper.model.WallpaperInfo;
import com.android.wallpaper.module.LargeScreenMultiPanesChecker;
import com.android.wallpaper.picker.preview.ui.WallpaperPreviewActivity;

/**
 * Activity that displays a preview of a specific wallpaper and provides the ability to set the
 * wallpaper as the user's current wallpaper.
 *
 * <p>TODO (b/442864280): Remove when cleaning up new picker UI flag
 */
public class PreviewActivity {

    /**
     * Implementation that provides an intent to start a PreviewActivity.
     *
     * <p>Get singleton instance from [Injector] instead of creating new instance directly.
     */
    public static class PreviewActivityIntentFactory implements InlinePreviewIntentFactory {
        private boolean mIsViewAsHome = false;

        @Override
        public Intent newIntent(Context context, WallpaperInfo wallpaper,
                boolean isAssetIdPresent, boolean shouldRefreshCategory) {
            Context appContext = context.getApplicationContext();
            LargeScreenMultiPanesChecker multiPanesChecker = new LargeScreenMultiPanesChecker();
            final boolean isMultiPanel = multiPanesChecker.isMultiPanesEnabled(appContext);
            return WallpaperPreviewActivity.intentBuilder(appContext, isAssetIdPresent)
                    .wallpaperInfo(wallpaper)
                    .viewAsHome(mIsViewAsHome)
                    .newTask(isMultiPanel)
                    .refreshCategory(shouldRefreshCategory)
                    .build();
        }

        @Override
        public void setViewAsHome(boolean isViewAsHome) {
            mIsViewAsHome = isViewAsHome;
        }
    }
}

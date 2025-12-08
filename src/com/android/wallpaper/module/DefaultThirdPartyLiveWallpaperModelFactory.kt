/*
 * Copyright (C) 2025 The Android Open Source Project
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

package com.android.wallpaper.module

import android.app.WallpaperInfo
import android.content.Context
import com.android.wallpaper.model.LiveWallpaperInfo
import com.android.wallpaper.picker.data.WallpaperModel
import com.android.wallpaper.util.converter.WallpaperModelFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** This class provides the default implementation for [ThirdPartyLiveWallpaperModelFactory] */
@Singleton
class DefaultThirdPartyLiveWallpaperModelFactory
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val wallpaperModelFactory: WallpaperModelFactory,
) : ThirdPartyLiveWallpaperModelFactory {

    override fun getLiveWallpaperModel(info: WallpaperInfo): WallpaperModel.LiveWallpaperModel? {
        // TODO(b/439671071): replace deprecated WallpaperInfo creation with direct WallpaperModel
        // creation
        return wallpaperModelFactory.getWallpaperModel(context, LiveWallpaperInfo(info))
            as? WallpaperModel.LiveWallpaperModel
    }
}

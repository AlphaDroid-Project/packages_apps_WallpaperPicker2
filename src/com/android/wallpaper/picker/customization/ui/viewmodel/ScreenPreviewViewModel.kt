/*
 * Copyright (C) 2022 The Android Open Source Project
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
 *
 */

package com.android.wallpaper.picker.customization.ui.viewmodel

import android.app.WallpaperColors
import android.os.Bundle
import com.android.wallpaper.model.WallpaperInfo
import com.android.wallpaper.util.PreviewUtils

/** Models the UI state for a preview of the home screen or lock screen. */
class ScreenPreviewViewModel(
    val previewUtils: PreviewUtils,
    private val initialExtrasProvider: () -> Bundle? = { null },
    private val wallpaperInfoProvider: suspend () -> WallpaperInfo?,
    private val onWallpaperColorChanged: (WallpaperColors?) -> Unit = {},
) {
    fun getInitialExtras(): Bundle? {
        return initialExtrasProvider.invoke()
    }

    suspend fun getWallpaperInfo(): WallpaperInfo? {
        return wallpaperInfoProvider.invoke()
    }

    fun onWallpaperColorsChanged(colors: WallpaperColors?) {
        onWallpaperColorChanged.invoke(colors)
    }
}

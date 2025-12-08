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

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import com.android.wallpaper.picker.customization.shared.model.WallpaperDestination

/** Methods for working with extended effect wallpapers. */
interface ExtendedEffectsHelper {
    /** Package name of the extended effect wallpaper, or empty string if none */
    val effectsPackage: String
    /** Package name of the extended effect activity, or empty string if none */
    val effectsActivity: String

    /** Returns whether the given component represents an extended effect wallpaper. */
    fun isExtendedEffectWallpaper(component: ComponentName): Boolean {
        return component.packageName == effectsPackage
    }

    /** Returns an Intent suitable for launching the extended effect wallpaper Activity. */
    fun getExtendedEffectIntent(): Intent {
        return Intent().apply { component = ComponentName(effectsPackage, effectsActivity) }
    }

    fun getCurrentThumbnail(destination: WallpaperDestination): Uri? = null
}

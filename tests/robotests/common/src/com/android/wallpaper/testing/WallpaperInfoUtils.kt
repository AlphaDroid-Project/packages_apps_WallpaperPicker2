/*
 * Copyright (C) 2024 The Android Open Source Project
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
package com.android.wallpaper.testing

import android.app.WallpaperInfo
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.pm.ServiceInfo
import android.service.wallpaper.WallpaperService
import org.robolectric.Shadows.shadowOf

/** Utility methods for writing Robolectric tests using [android.app.WallpaperInfo]. */
class WallpaperInfoUtils {
    companion object {
        const val STUB_PACKAGE = "com.google.android.apps.wallpaper.nexus"
        const val WALLPAPER_SPLIT = "wallpaper_cities_ny"
        const val WALLPAPER_CLASS = "NewYorkWallpaper"

        /**
         * Creates an instance of [android.app.WallpaperInfo], and optionally registers the
         * associated service so that it will resolve if necessary.
         *
         * This method must be called from a test that uses
         * [com.android.wallpaper.testing.ShadowWallpaperInfo].
         */
        fun createWallpaperInfo(
            context: Context,
            stubPackage: String = STUB_PACKAGE,
            wallpaperSplit: String = WALLPAPER_SPLIT,
            wallpaperClass: String = WALLPAPER_CLASS,
            configureService: Boolean = true,
        ): WallpaperInfo {
            val resolveInfo =
                ResolveInfo().apply {
                    serviceInfo = ServiceInfo()
                    serviceInfo.packageName = stubPackage
                    serviceInfo.splitName = wallpaperSplit
                    serviceInfo.name = wallpaperClass
                    serviceInfo.flags = PackageManager.GET_META_DATA
                }
            // ShadowWallpaperInfo allows the creation of this object
            val wallpaperInfo = WallpaperInfo(context, resolveInfo)
            if (configureService) {
                // For live wallpapers, we need the call to PackageManager#resolveService in
                // RecentWallpaperUtils#cleanUpRecentsArray to return non-null so that our test
                // entry isn't removed from the recents list.
                val pm = shadowOf(context.packageManager)
                val intent =
                    Intent(WallpaperService.SERVICE_INTERFACE)
                        .setClassName(stubPackage, wallpaperClass)
                pm.addResolveInfoForIntent(intent, resolveInfo)
            }
            return wallpaperInfo
        }
    }
}

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

import android.app.wallpaper.WallpaperDescription
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.pm.ServiceInfo
import android.net.Uri
import com.android.wallpaper.model.WallpaperInfo
import com.android.wallpaper.picker.category.client.LiveWallpapersClient
import com.android.wallpaper.picker.data.WallpaperModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeLiveWallpaperClientImpl @Inject constructor() : LiveWallpapersClient {
    @Inject @ApplicationContext lateinit var appContext: Context

    override fun getAll(excludedPackageNames: Set<String?>?): List<WallpaperInfo> {
        val attributions: MutableList<String> = ArrayList()
        attributions.add("Title")
        attributions.add("Subtitle 1")
        attributions.add("Subtitle 2")

        val mTestLiveWallpaper = TestLiveWallpaperInfo(TestStaticWallpaperInfo.COLOR_DEFAULT)
        mTestLiveWallpaper.setAttributions(attributions)
        mTestLiveWallpaper.collectionId = "collectionLive"
        mTestLiveWallpaper.wallpaperId = "wallpaperLive"
        return listOf(mTestLiveWallpaper)
    }

    override fun getAllWallpapers(excludedPackageNames: Set<String?>?): List<WallpaperModel> {
        val componentName = ComponentName("package", "class")
        val contextUri = Uri.parse("uri://context")
        val sourceDescription =
            WallpaperDescription.Builder().setComponent(componentName).setId("id").build()

        val wallpaperInfo =
            android.app.WallpaperInfo(
                appContext,
                ResolveInfo().apply {
                    serviceInfo = ServiceInfo()
                    serviceInfo.packageName = "com.google.android.apps.wallpaper.nexus"
                    serviceInfo.splitName = "wallpaper_cities_ny"
                    serviceInfo.name = "NewYorkWallpaper"
                    serviceInfo.flags = PackageManager.GET_META_DATA
                },
            )
        val liveWallpaperModel =
            WallpaperModelUtils.getLiveWallpaperModel(
                wallpaperId = "unused",
                collectionId = "collectionId",
                systemWallpaperInfo = wallpaperInfo,
                description = sourceDescription,
                placeholderColor = 123,
                effectNames = "effects",
                attribution = listOf("title", "line1", "line2"),
                actionUrl = contextUri.toString(),
            )
        return listOf(liveWallpaperModel)
    }
}

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
import android.app.wallpaper.WallpaperDescription
import android.content.Context
import android.net.Uri
import android.os.Parcel
import android.util.Log
import android.util.Pair
import androidx.annotation.VisibleForTesting
import androidx.core.database.getBlobOrNull
import com.android.wallpaper.model.CreativeCategory
import com.android.wallpaper.model.WallpaperInfoContract
import com.android.wallpaper.picker.customization.shared.model.WallpaperDestination
import javax.inject.Inject

class DefaultCreativeHelper @Inject constructor() : CreativeHelper {
    override fun getCreativePreviewUri(
        context: Context,
        info: WallpaperInfo,
        destination: WallpaperDestination,
    ): Uri? {
        return getCurrentCreativeData(context, info, destination).first
    }

    override fun getCreativeDescription(
        context: Context,
        info: WallpaperInfo,
        destination: WallpaperDestination,
    ): WallpaperDescription? {
        return getCurrentCreativeData(context, info, destination).second
    }

    companion object {
        private const val TAG = "DefaultCreativeHelper"

        // Queries a live wallpaper for its preview Uri and description, and returns them if they
        // exist.
        @VisibleForTesting
        private fun getCurrentCreativeData(
            context: Context,
            info: WallpaperInfo,
            destination: WallpaperDestination,
        ): Pair<Uri?, WallpaperDescription?> {
            val metaData = info.serviceInfo.metaData
            val defaultValue: Pair<Uri?, WallpaperDescription?> = Pair(null, null)
            val uri =
                metaData.getString(CreativeCategory.KEY_WALLPAPER_SAVE_CREATIVE_WALLPAPER_CURRENT)
                    ?: return defaultValue

            val currentAssetsUri = Uri.parse(uri)
            if (currentAssetsUri.authority == null) return defaultValue
            context.contentResolver
                .acquireContentProviderClient(currentAssetsUri.authority!!)
                ?.use { client ->
                    try {
                        client.query(currentAssetsUri, null, null, null, null)?.use { cursor ->
                            if (!cursor.moveToFirst()) return defaultValue
                            do {
                                val dest =
                                    cursor.getString(
                                        cursor.getColumnIndex(
                                            WallpaperInfoContract.CURRENT_DESTINATION
                                        )
                                    )
                                val previewUri =
                                    Uri.parse(
                                        cursor.getString(
                                            cursor.getColumnIndex(
                                                WallpaperInfoContract.CURRENT_CONFIG_PREVIEW_URI
                                            )
                                        )
                                    )
                                val descriptionIndex =
                                    cursor.getColumnIndex(
                                        WallpaperInfoContract.CURRENT_DESCRIPITION
                                    )
                                val description =
                                    if (descriptionIndex >= 0) {
                                        cursor.getBlobOrNull(descriptionIndex)?.let {
                                            descriptionFromBytes(it).let { desc ->
                                                if (desc.component == null) {
                                                    desc
                                                        .toBuilder()
                                                        .setComponent(info.component)
                                                        .build()
                                                } else desc
                                            }
                                        }
                                    } else null
                                if (
                                    (dest == "home" && destination == WallpaperDestination.HOME) ||
                                        (dest == "lock" && destination == WallpaperDestination.LOCK)
                                ) {
                                    return Pair(previewUri, description)
                                }
                            } while (cursor.moveToNext())
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Error retrieving current creative asset id: ", e)
                    }
                }

            return defaultValue
        }

        private fun descriptionFromBytes(bytes: ByteArray): WallpaperDescription {
            val parcel = Parcel.obtain()
            parcel.unmarshall(bytes, 0, bytes.size)
            parcel.setDataPosition(0)
            val desc = WallpaperDescription.CREATOR.createFromParcel(parcel)
            parcel.recycle()
            return desc
        }
    }
}

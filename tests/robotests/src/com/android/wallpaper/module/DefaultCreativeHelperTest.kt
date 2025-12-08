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

import android.app.wallpaper.WallpaperDescription
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import com.android.wallpaper.model.CreativeCategory
import com.android.wallpaper.model.WallpaperInfoContract
import com.android.wallpaper.picker.customization.shared.model.WallpaperDestination
import com.android.wallpaper.testing.ShadowWallpaperInfo
import com.android.wallpaper.testing.WallpaperInfoUtils.Companion.createWallpaperInfo
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowContentResolver

@HiltAndroidTest
@Config(shadows = [ShadowWallpaperInfo::class, ShadowContentResolver::class])
@RunWith(RobolectricTestRunner::class)
class DefaultCreativeHelperTest {
    @get:Rule(order = 0) var hiltRule = HiltAndroidRule(this)
    @Inject @ApplicationContext lateinit var context: Context
    @Inject lateinit var creativeHelper: DefaultCreativeHelper

    @Before
    fun setUp() {
        hiltRule.inject()

        ShadowContentResolver.registerProviderInternal(
            "com.example.fake",
            CurrentWallpapersContentProvider(),
        )
        ShadowContentResolver.registerProviderInternal(
            "com.example.failing",
            ExceptionThrowingContentProvider(),
        )
    }

    @Test
    fun getCreativePreviewUri_homeScreen_succeeds() {
        val metaData =
            Bundle().apply {
                putString(
                    CreativeCategory.KEY_WALLPAPER_SAVE_CREATIVE_WALLPAPER_CURRENT,
                    "content://com.example.fake/currentwallpapers",
                )
            }
        val info = createWallpaperInfo(context, metaData = metaData)

        val uri = creativeHelper.getCreativePreviewUri(context, info, WallpaperDestination.HOME)

        assertThat(uri).isEqualTo(CurrentWallpapersContentProvider.PREVIEW_URI_HOME)
    }

    @Test
    fun getCreativePreviewUri_lockScreen_succeeds() {
        val metaData =
            Bundle().apply {
                putString(
                    CreativeCategory.KEY_WALLPAPER_SAVE_CREATIVE_WALLPAPER_CURRENT,
                    "content://com.example.fake/currentwallpapers",
                )
            }
        val info = createWallpaperInfo(context, metaData = metaData)

        val uri = creativeHelper.getCreativePreviewUri(context, info, WallpaperDestination.LOCK)

        assertThat(uri).isEqualTo(CurrentWallpapersContentProvider.PREVIEW_URI_LOCK)
    }

    @Test
    fun getCreativeDescription_homeScreen_succeeds() {
        val metaData =
            Bundle().apply {
                putString(
                    CreativeCategory.KEY_WALLPAPER_SAVE_CREATIVE_WALLPAPER_CURRENT,
                    "content://com.example.fake/currentwallpapers",
                )
            }
        val info = createWallpaperInfo(context, metaData = metaData)
        val expectedDescription =
            CurrentWallpapersContentProvider.DESCRIPTION_HOME.toBuilder()
                .setComponent(info.component)
                .build()

        val description =
            creativeHelper.getCreativeDescription(context, info, WallpaperDestination.HOME)

        assertThat(description).isEqualTo(expectedDescription)
    }

    @Test
    fun getCreativeDescription_lockScreen_succeeds() {
        val metaData =
            Bundle().apply {
                putString(
                    CreativeCategory.KEY_WALLPAPER_SAVE_CREATIVE_WALLPAPER_CURRENT,
                    "content://com.example.fake/currentwallpapers",
                )
            }
        val info = createWallpaperInfo(context, metaData = metaData)
        val expectedDescription =
            CurrentWallpapersContentProvider.DESCRIPTION_LOCK.toBuilder()
                .setComponent(info.component)
                .build()

        val description =
            creativeHelper.getCreativeDescription(context, info, WallpaperDestination.LOCK)

        assertThat(description).isEqualTo(expectedDescription)
    }

    @Test
    fun getCurrentCreativeData_providerThrowsException_returnsNull() {
        // Setup: Create metadata pointing to a content provider that will throw an exception.
        val metaData =
            Bundle().apply {
                putString(
                    CreativeCategory.KEY_WALLPAPER_SAVE_CREATIVE_WALLPAPER_CURRENT,
                    "content://com.example.failing/somepath",
                )
            }
        val info = createWallpaperInfo(context, metaData = metaData)

        // Action & Verification: Call the public methods and assert they return null.
        // This verifies that the try-catch block correctly handles the exception and returns the
        // default null values without crashing.
        val uri = creativeHelper.getCreativePreviewUri(context, info, WallpaperDestination.HOME)
        val description =
            creativeHelper.getCreativeDescription(context, info, WallpaperDestination.HOME)

        assertThat(uri).isNull()
        assertThat(description).isNull()
    }

    private class CurrentWallpapersContentProvider : ContentProvider() {
        override fun onCreate(): Boolean {
            return true
        }

        override fun query(
            uri: Uri,
            projection: Array<out String>?,
            selection: String?,
            selectionArgs: Array<out String>?,
            sortOrder: String?,
        ): Cursor {
            val cursor =
                MatrixCursor(
                    arrayOf(
                        WallpaperInfoContract.CURRENT_DESTINATION,
                        WallpaperInfoContract.CURRENT_CONFIG_PREVIEW_URI,
                        WallpaperInfoContract.CURRENT_DESCRIPITION,
                    )
                )
            cursor
                .newRow()
                .add(WallpaperInfoContract.CURRENT_DESTINATION, "home")
                .add(WallpaperInfoContract.CURRENT_CONFIG_PREVIEW_URI, PREVIEW_URI_HOME)
                .add(WallpaperInfoContract.CURRENT_DESCRIPITION, marshall(DESCRIPTION_HOME))
            cursor
                .newRow()
                .add(WallpaperInfoContract.CURRENT_DESTINATION, "lock")
                .add(WallpaperInfoContract.CURRENT_CONFIG_PREVIEW_URI, PREVIEW_URI_LOCK)
                .add(WallpaperInfoContract.CURRENT_DESCRIPITION, marshall(DESCRIPTION_LOCK))
            return cursor
        }

        override fun getType(uri: Uri): String? {
            return null
        }

        override fun insert(uri: Uri, values: ContentValues?): Uri? {
            return null
        }

        override fun update(
            uri: Uri,
            values: ContentValues?,
            selection: String?,
            selectionArgs: Array<out String>?,
        ): Int {
            return 0
        }

        override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
            return 0
        }

        companion object {
            val PREVIEW_URI_HOME = Uri.parse("www.bogus.com/reallybogus/home")
            val PREVIEW_URI_LOCK = Uri.parse("www.bogus.com/reallybogus/lock")
            val DESCRIPTION_HOME = WallpaperDescription.Builder().setId("id_1").build()
            val DESCRIPTION_LOCK = WallpaperDescription.Builder().setId("id_1").build()

            private fun marshall(parcelable: Parcelable): ByteArray {
                val parcel = Parcel.obtain()
                parcelable.writeToParcel(parcel, 0)
                val bytes = parcel.marshall()
                parcel.recycle()
                return bytes
            }
        }
    }

    private class ExceptionThrowingContentProvider : ContentProvider() {
        override fun onCreate(): Boolean = true

        override fun query(
            uri: Uri,
            projection: Array<out String>?,
            selection: String?,
            selectionArgs: Array<out String>?,
            sortOrder: String?,
        ): Cursor {
            throw RuntimeException("Test Exception")
        }

        override fun getType(uri: Uri): String? = null

        override fun insert(uri: Uri, values: ContentValues?): Uri? = null

        override fun update(
            uri: Uri,
            values: ContentValues?,
            selection: String?,
            selectionArgs: Array<out String>?,
        ): Int = 0

        override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int =
            0
    }
}

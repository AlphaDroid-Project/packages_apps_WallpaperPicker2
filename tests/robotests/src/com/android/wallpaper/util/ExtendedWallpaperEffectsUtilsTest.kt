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

package com.android.wallpaper.util

import android.app.Flags.FLAG_UPDATE_RECENTS_FROM_SYSTEM
import android.app.WallpaperManager.FLAG_LOCK
import android.app.WallpaperManager.FLAG_SYSTEM
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Point
import android.graphics.Rect
import android.net.Uri
import android.platform.test.annotations.DisableFlags
import android.platform.test.annotations.EnableFlags
import android.platform.test.flag.junit.SetFlagsRule
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import androidx.test.core.app.ActivityScenario
import com.android.systemui.shared.Flags.FLAG_PAN_AND_ZOOM_IN_EXTENDED_WALLPAPER_EFFECTS
import com.android.wallpaper.config.BaseFlags
import com.android.wallpaper.module.InjectorProvider
import com.android.wallpaper.picker.data.Destination
import com.android.wallpaper.picker.preview.PreviewTestActivity
import com.android.wallpaper.testing.TestInjector
import com.android.wallpaper.testing.WallpaperModelUtils
import com.android.wallpaper.util.ExtendedWallpaperEffectsUtils.PHOTO_CROPS
import com.android.wallpaper.util.ExtendedWallpaperEffectsUtils.PHOTO_URI
import com.android.wallpaper.util.ExtendedWallpaperEffectsUtils.SOURCE_BITMAP_SCREEN
import com.android.wallpaper.util.wallpaperconnection.WallpaperConnectionUtils
import com.google.common.truth.Truth.assertThat
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
class ExtendedWallpaperEffectsUtilsTest {
    @get:Rule(order = 0) var hiltRule = HiltAndroidRule(this)
    @get:Rule(order = 1) var setFlagsRule = SetFlagsRule()

    private lateinit var wallpaperConnectionUtils: WallpaperConnectionUtils

    @Inject @ApplicationContext lateinit var context: Context
    @Inject lateinit var testDispatcher: TestDispatcher
    @Inject lateinit var testScope: TestScope
    @Inject lateinit var testInjector: TestInjector

    object launcher : ActivityResultLauncher<Intent>() {
        var lastIntent: Intent? = null
        override val contract: ActivityResultContract<Intent, *>
            get() = TODO("Not yet implemented")

        override fun launch(input: Intent, options: ActivityOptionsCompat?) {
            lastIntent = input
        }

        override fun unregister() {
            TODO("Not yet implemented")
        }
    }

    @Before
    fun setUp() {
        hiltRule.inject()
        InjectorProvider.setInjector(testInjector)
        val activityInfo =
            ActivityInfo().apply {
                name = PreviewTestActivity::class.java.name
                packageName = context.packageName
            }
        Shadows.shadowOf(context.packageManager).addOrUpdateActivity(activityInfo)

        val scenario = ActivityScenario.launch(PreviewTestActivity::class.java)
        scenario.onActivity {
            val activityScopeEntryPoint =
                EntryPointAccessors.fromActivity(it, ActivityScopeEntryPoint::class.java)
            wallpaperConnectionUtils = activityScopeEntryPoint.wallpaperConnectionUtils()
        }
    }

    @EntryPoint
    @InstallIn(ActivityComponent::class)
    interface ActivityScopeEntryPoint {
        fun wallpaperConnectionUtils(): WallpaperConnectionUtils
    }

    @DisableFlags(FLAG_UPDATE_RECENTS_FROM_SYSTEM)
    @Test
    fun startEffects_noRecentsFromSystem_addsPhotoUri() =
        testScope.runTest {
            val photoUri = Uri.parse("content://bogus")
            val model =
                WallpaperModelUtils.getStaticWallpaperModel(
                    wallpaperId = "id",
                    collectionId = "collection",
                    imageWallpaperUri = photoUri,
                )

            ExtendedWallpaperEffectsUtils.startExtendedWallpaperEffects(
                model,
                launcher,
                context,
                wallpaperConnectionUtils,
                BaseFlags.get(),
            )

            assertThat(launcher.lastIntent).isNotNull()
            val intent = launcher.lastIntent!!
            assertThat(intent).isNotNull()
            assertThat(intent.hasExtra(PHOTO_URI)).isTrue()
            assertThat(intent.getParcelableExtra(PHOTO_URI, Uri::class.java)).isEqualTo(photoUri)
        }

    @EnableFlags(FLAG_UPDATE_RECENTS_FROM_SYSTEM)
    @DisableFlags(FLAG_PAN_AND_ZOOM_IN_EXTENDED_WALLPAPER_EFFECTS)
    @Test
    fun startEffects_recentsFromSystem_noPanAndZoom_notApplied_addsPhotoUri() =
        testScope.runTest {
            val photoUri = Uri.parse("content://bogus")
            val model =
                WallpaperModelUtils.getStaticWallpaperModel(
                    wallpaperId = "id",
                    collectionId = "collection",
                    destination = Destination.NOT_APPLIED,
                    imageWallpaperUri = photoUri,
                )

            ExtendedWallpaperEffectsUtils.startExtendedWallpaperEffects(
                model,
                launcher,
                context,
                wallpaperConnectionUtils,
                BaseFlags.get(),
            )

            assertThat(launcher.lastIntent).isNotNull()
            val intent = launcher.lastIntent!!
            assertThat(intent).isNotNull()
            assertThat(intent.hasExtra(PHOTO_URI)).isTrue()
            assertThat(intent.getParcelableExtra(PHOTO_URI, Uri::class.java)).isEqualTo(photoUri)
            assertThat(intent.hasExtra(SOURCE_BITMAP_SCREEN)).isFalse()
        }

    @EnableFlags(FLAG_UPDATE_RECENTS_FROM_SYSTEM, FLAG_PAN_AND_ZOOM_IN_EXTENDED_WALLPAPER_EFFECTS)
    @Test
    fun startEffects_notApplied_addsPhotoUriAndCrops() =
        testScope.runTest {
            val photoUri = Uri.parse("content://bogus")
            val crops = mapOf(Point(1, 2) to Rect(3, 4, 5, 6))
            val model =
                WallpaperModelUtils.getStaticWallpaperModel(
                    wallpaperId = "id",
                    collectionId = "collection",
                    destination = Destination.NOT_APPLIED,
                    imageWallpaperUri = photoUri,
                    cropHints = crops,
                )

            ExtendedWallpaperEffectsUtils.startExtendedWallpaperEffects(
                model,
                launcher,
                context,
                wallpaperConnectionUtils,
                BaseFlags.get(),
            )

            assertThat(launcher.lastIntent).isNotNull()
            val intent = launcher.lastIntent!!
            assertThat(intent).isNotNull()
            assertThat(intent.hasExtra(PHOTO_URI)).isTrue()
            assertThat(intent.getParcelableExtra(PHOTO_URI, Uri::class.java)).isEqualTo(photoUri)
            assertThat(intent.hasExtra(PHOTO_CROPS)).isTrue()
            assertThat(intent.getParcelableExtra(PHOTO_CROPS, Map::class.java))
                .containsExactlyEntriesIn(crops)
            assertThat(intent.hasExtra(SOURCE_BITMAP_SCREEN)).isFalse()
        }

    @EnableFlags(FLAG_UPDATE_RECENTS_FROM_SYSTEM)
    @Test
    fun startEffects_recentsFromSystem_appliedToSystem_setsSource() =
        testScope.runTest {
            val model =
                WallpaperModelUtils.getStaticWallpaperModel(
                    wallpaperId = "id",
                    collectionId = "collection",
                    destination = Destination.APPLIED_TO_SYSTEM,
                )

            ExtendedWallpaperEffectsUtils.startExtendedWallpaperEffects(
                model,
                launcher,
                context,
                wallpaperConnectionUtils,
                BaseFlags.get(),
            )

            assertThat(launcher.lastIntent).isNotNull()
            val intent = launcher.lastIntent!!
            assertThat(intent).isNotNull()
            assertThat(intent.hasExtra(PHOTO_URI)).isFalse()
            assertThat(intent.hasExtra(SOURCE_BITMAP_SCREEN)).isTrue()
            assertThat(intent.getIntExtra(SOURCE_BITMAP_SCREEN, 0)).isEqualTo(FLAG_SYSTEM)
        }

    @EnableFlags(FLAG_UPDATE_RECENTS_FROM_SYSTEM)
    @Test
    fun startEffects_recentsFromSystem_appliedToBoth_setsSource() =
        testScope.runTest {
            val model =
                WallpaperModelUtils.getStaticWallpaperModel(
                    wallpaperId = "id",
                    collectionId = "collection",
                    destination = Destination.APPLIED_TO_SYSTEM_LOCK,
                )

            ExtendedWallpaperEffectsUtils.startExtendedWallpaperEffects(
                model,
                launcher,
                context,
                wallpaperConnectionUtils,
                BaseFlags.get(),
            )

            assertThat(launcher.lastIntent).isNotNull()
            val intent = launcher.lastIntent!!
            assertThat(intent).isNotNull()
            assertThat(intent.hasExtra(PHOTO_URI)).isFalse()
            assertThat(intent.hasExtra(SOURCE_BITMAP_SCREEN)).isTrue()
            assertThat(intent.getIntExtra(SOURCE_BITMAP_SCREEN, 0)).isEqualTo(FLAG_SYSTEM)
        }

    @EnableFlags(FLAG_UPDATE_RECENTS_FROM_SYSTEM)
    @Test
    fun startEffects_recentsFromSystem_appliedToLock_setsSource() =
        testScope.runTest {
            val model =
                WallpaperModelUtils.getStaticWallpaperModel(
                    wallpaperId = "id",
                    collectionId = "collection",
                    destination = Destination.APPLIED_TO_LOCK,
                )

            ExtendedWallpaperEffectsUtils.startExtendedWallpaperEffects(
                model,
                launcher,
                context,
                wallpaperConnectionUtils,
                BaseFlags.get(),
            )

            assertThat(launcher.lastIntent).isNotNull()
            val intent = launcher.lastIntent!!
            assertThat(intent).isNotNull()
            assertThat(intent.hasExtra(PHOTO_URI)).isFalse()
            assertThat(intent.hasExtra(SOURCE_BITMAP_SCREEN)).isTrue()
            assertThat(intent.getIntExtra(SOURCE_BITMAP_SCREEN, 0)).isEqualTo(FLAG_LOCK)
        }
}

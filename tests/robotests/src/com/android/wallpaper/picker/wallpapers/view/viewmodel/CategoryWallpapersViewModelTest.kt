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

package com.android.wallpaper.picker.wallpapers.view.viewmodel

import android.app.WallpaperInfo
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.pm.ServiceInfo
import android.net.Uri
import android.platform.test.flag.junit.SetFlagsRule
import androidx.activity.viewModels
import androidx.test.core.app.ActivityScenario
import com.android.wallpaper.module.InjectorProvider
import com.android.wallpaper.picker.common.preview.data.repository.PersistentWallpaperModelRepository
import com.android.wallpaper.picker.data.CreativeWallpaperData
import com.android.wallpaper.picker.preview.PreviewTestActivity
import com.android.wallpaper.picker.wallpapers.ui.view.viewmodel.CategoryWallpapersItemViewModel
import com.android.wallpaper.picker.wallpapers.ui.view.viewmodel.CategoryWallpapersViewModel
import com.android.wallpaper.testing.FakeCategoryWallpapersInteractor
import com.android.wallpaper.testing.ShadowWallpaperInfo
import com.android.wallpaper.testing.TestInjector
import com.android.wallpaper.testing.WallpaperModelUtils
import com.android.wallpaper.testing.collectLastValue
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@HiltAndroidTest
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(shadows = [ShadowWallpaperInfo::class])
class CategoryWallpapersViewModelTest {
    @get:Rule(order = 0) var hiltRule = HiltAndroidRule(this)
    @get:Rule(order = 1) val setFlagsRule = SetFlagsRule()

    private lateinit var scenario: ActivityScenario<PreviewTestActivity>
    private lateinit var categoryWallpapersViewModel: CategoryWallpapersViewModel

    @Inject lateinit var testDispatcher: TestDispatcher
    @Inject @ApplicationContext lateinit var appContext: Context

    @Inject lateinit var testInjector: TestInjector

    @Inject lateinit var fakeCategoryWallpapersInteractor: FakeCategoryWallpapersInteractor
    @Inject lateinit var persistentWallpaperModelRepository: PersistentWallpaperModelRepository

    @Before
    fun setUp() {
        hiltRule.inject()

        InjectorProvider.setInjector(testInjector)
        Dispatchers.setMain(testDispatcher)

        val activityInfo =
            ActivityInfo().apply {
                name = PreviewTestActivity::class.java.name
                packageName = appContext.packageName
            }
        Shadows.shadowOf(appContext.packageManager).addOrUpdateActivity(activityInfo)
        scenario = ActivityScenario.launch(PreviewTestActivity::class.java)
        scenario.onActivity { setEverything(it) }
    }

    private fun setEverything(activity: PreviewTestActivity) {
        categoryWallpapersViewModel = activity.viewModels<CategoryWallpapersViewModel>().value
    }

    @Test
    fun sections_verifyNumberOfSectionsOfPlainWallpapersWithoutLabels() = runTest {
        val sections =
            collectLastValue(categoryWallpapersViewModel.categoryWallpapersContentViewModel)()
        assertThat(sections?.wallpaperItems?.size).isEqualTo(1)
        assertThat(
                (sections?.wallpaperItems?.get(0)
                        as? CategoryWallpapersItemViewModel.PlainThumbnailsViewModelCategory)
                    ?.thumbnailAssets
                    ?.size
            )
            .isEqualTo(24)
    }

    @Test
    fun sections_verifyNumberOfSectionsOfTemplateWallpapersWithPrimaryLabel() = runTest {
        val resolveInfo =
            ResolveInfo().apply {
                serviceInfo = ServiceInfo()
                serviceInfo.packageName = "com.google.android.apps.wallpaper.nexus"
                serviceInfo.splitName = "wallpaper_cities_ny"
                serviceInfo.name = "NewYorkWallpaper"
                serviceInfo.flags = PackageManager.GET_META_DATA
            }
        val creativeWallpaperData =
            CreativeWallpaperData(
                configPreviewUri = null,
                cleanPreviewUri = null,
                deleteUri = Uri.parse("https://www.hello.com"),
                thumbnailUri = null,
                shareUri = null,
                author = "fake",
                description = "fake",
                contentDescription = null,
                isCurrent = false,
                creativeWallpaperEffectsData = null,
                isNewCreativeWallpaper = false,
            )

        val wallpaperInfo = WallpaperInfo(appContext, resolveInfo)

        fakeCategoryWallpapersInteractor.setWallpapers(
            listOf(
                WallpaperModelUtils.getLiveWallpaperModel(
                    wallpaperId = "testWallpaperId",
                    collectionId = "testCollection",
                    systemWallpaperInfo = wallpaperInfo,
                    isApplied = false,
                    groupName = "hello",
                    creativeWallpaperData = creativeWallpaperData,
                ),
                WallpaperModelUtils.getLiveWallpaperModel(
                    wallpaperId = "testWallpaperId",
                    collectionId = "testCollection",
                    systemWallpaperInfo = wallpaperInfo,
                    isApplied = false,
                    groupName = "hello",
                    creativeWallpaperData = creativeWallpaperData,
                ),
                WallpaperModelUtils.getLiveWallpaperModel(
                    wallpaperId = "testWallpaperId",
                    collectionId = "testCollection",
                    systemWallpaperInfo = wallpaperInfo,
                    isApplied = false,
                    groupName = "hello",
                    creativeWallpaperData = creativeWallpaperData,
                ),
                WallpaperModelUtils.getLiveWallpaperModel(
                    wallpaperId = "testWallpaperId",
                    collectionId = "testCollection",
                    systemWallpaperInfo = wallpaperInfo,
                    isApplied = false,
                    groupName = "hello",
                    creativeWallpaperData = creativeWallpaperData,
                ),
            )
        )

        val sections =
            collectLastValue(categoryWallpapersViewModel.categoryWallpapersContentViewModel)()
        assertThat(sections?.wallpaperItems?.size).isEqualTo(2)

        // verify there is a primary header
        assertThat(
                (sections?.wallpaperItems?.get(0)
                        as? CategoryWallpapersItemViewModel.PrimaryHeaderViewModelCategory)
                    ?.title
            )
            .isEqualTo("hello")

        // verify that there are 4 template thumbnails
        assertThat(
                (sections?.wallpaperItems?.get(1)
                        as? CategoryWallpapersItemViewModel.TemplateThumbnailsViewModelCategory)
                    ?.thumbnailAssets
                    ?.size
            )
            .isEqualTo(4)
    }

    @Test
    fun sections_verifyMixOfPlainAndTemplateThumbnailsWithNoSecondaryLabel() = runTest {
        val resolveInfo =
            ResolveInfo().apply {
                serviceInfo = ServiceInfo()
                serviceInfo.packageName = "com.google.android.apps.wallpaper.nexus"
                serviceInfo.splitName = "wallpaper_cities_ny"
                serviceInfo.name = "NewYorkWallpaper"
                serviceInfo.flags = PackageManager.GET_META_DATA
            }
        val creativeWallpaperData =
            CreativeWallpaperData(
                configPreviewUri = null,
                cleanPreviewUri = null,
                deleteUri = Uri.parse("https://www.hello.com"),
                thumbnailUri = null,
                shareUri = null,
                author = "fake",
                description = "fake",
                contentDescription = null,
                isCurrent = false,
                creativeWallpaperEffectsData = null,
                isNewCreativeWallpaper = false,
            )

        val wallpaperInfo = WallpaperInfo(appContext, resolveInfo)

        fakeCategoryWallpapersInteractor.setWallpapers(
            listOf(
                WallpaperModelUtils.getLiveWallpaperModel(
                    wallpaperId = "testId1",
                    collectionId = "testCollection",
                    systemWallpaperInfo = wallpaperInfo,
                    isApplied = false,
                    groupName = "hello",
                    creativeWallpaperData = creativeWallpaperData,
                ),
                WallpaperModelUtils.getLiveWallpaperModel(
                    wallpaperId = "testId2",
                    collectionId = "testCollection",
                    systemWallpaperInfo = wallpaperInfo,
                    isApplied = false,
                    groupName = "hello",
                    creativeWallpaperData = creativeWallpaperData,
                ),
                WallpaperModelUtils.getStaticWallpaperModel(
                    wallpaperId = "testId3",
                    collectionId = "testCollection1",
                    title = "static wp 1",
                ),
                WallpaperModelUtils.getStaticWallpaperModel(
                    wallpaperId = "testId4",
                    collectionId = "testCollection2",
                    title = "static wp 2",
                ),
                WallpaperModelUtils.getStaticWallpaperModel(
                    wallpaperId = "testId5",
                    collectionId = "testCollection3",
                    title = "static wp 3",
                ),
                WallpaperModelUtils.getLiveWallpaperModel(
                    wallpaperId = "testId6",
                    collectionId = "testCollection",
                    title = "live walpaper no group 1",
                    groupName = "",
                    systemWallpaperInfo = wallpaperInfo,
                    isApplied = false,
                ),
                WallpaperModelUtils.getLiveWallpaperModel(
                    wallpaperId = "testId7",
                    collectionId = "testCollection",
                    title = "live walpaper no group 2",
                    groupName = "",
                    systemWallpaperInfo = wallpaperInfo,
                    isApplied = false,
                ),
            )
        )

        val sections =
            collectLastValue(categoryWallpapersViewModel.categoryWallpapersContentViewModel)()
        assertThat(sections?.wallpaperItems?.size).isEqualTo(3)

        // verify there is a primary header
        assertThat(
                (sections?.wallpaperItems?.get(0)
                        as? CategoryWallpapersItemViewModel.PrimaryHeaderViewModelCategory)
                    ?.title
            )
            .isEqualTo("hello")

        // verify that there are 2 template thumbnails
        assertThat(
                (sections?.wallpaperItems?.get(1)
                        as? CategoryWallpapersItemViewModel.TemplateThumbnailsViewModelCategory)
                    ?.thumbnailAssets
                    ?.size
            )
            .isEqualTo(2)

        // verify 5 ungrouped plain thumbnails
        assertThat(
                (sections?.wallpaperItems?.get(2)
                        as? CategoryWallpapersItemViewModel.PlainThumbnailsViewModelCategory)
                    ?.thumbnailAssets
                    ?.size
            )
            .isEqualTo(5)
    }

    @Test
    fun sections_verifyOnClickAction() = runTest {
        fakeCategoryWallpapersInteractor.setWallpapers(
            listOf(
                WallpaperModelUtils.getStaticWallpaperModel(
                    wallpaperId = "testId3",
                    collectionId = "testCollection1",
                    title = "static wp 1",
                ),
                WallpaperModelUtils.getStaticWallpaperModel(
                    wallpaperId = "testId4",
                    collectionId = "testCollection2",
                    title = "static wp 2",
                ),
            )
        )

        val categories =
            collectLastValue(categoryWallpapersViewModel.categoryWallpapersContentViewModel)()
                ?.wallpaperItems
        assertThat(categories).isNotNull()
        assertThat(categories).hasSize(1)

        val category =
            categories?.get(0) as? CategoryWallpapersItemViewModel.PlainThumbnailsViewModelCategory
        val thumbnail = category?.thumbnailAssets?.get(1)
        val onClick = thumbnail?.onSectionClicked

        assertThat(onClick).isNotNull()
        onClick?.invoke()
        val selectedWallpaperModel = persistentWallpaperModelRepository.wallpaperModel.value

        // verify that the lambda correctly set the selected WallpaperModel
        assertThat(selectedWallpaperModel?.commonWallpaperData?.title).isEqualTo("static wp 2")
    }

    @Test
    fun sections_verifyTitle() = runTest {
        val screenViewModel =
            collectLastValue(categoryWallpapersViewModel.categoryWallpapersContentViewModel)()
        assertThat(screenViewModel?.wallpaperItems?.size).isEqualTo(1)
        assertThat(screenViewModel?.title).isEqualTo("sample title")
    }
}

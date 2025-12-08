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

package com.android.wallpaper.picker.category.repository

import android.content.Context
import com.android.wallpaper.model.Category
import com.android.wallpaper.model.ImageCategory
import com.android.wallpaper.model.ThirdPartyLiveWallpaperCategory
import com.android.wallpaper.model.WallpaperInfo
import com.android.wallpaper.module.InjectorProvider
import com.android.wallpaper.picker.category.client.LiveWallpapersClient
import com.android.wallpaper.picker.category.data.repository.DefaultWallpaperCategoryRepository
import com.android.wallpaper.testing.FakeDefaultCategoryFactory
import com.android.wallpaper.testing.FakeDefaultWallpaperCategoryClient
import com.android.wallpaper.testing.TestInjector
import com.android.wallpaper.testing.TestStaticWallpaperInfo
import com.android.wallpaper.testing.TestWallpaperCategory
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@HiltAndroidTest
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class DefaultWallpaperCategoryRepositoryTest {

    @get:Rule var hiltRule = HiltAndroidRule(this)
    @Inject @ApplicationContext lateinit var context: Context
    @Inject lateinit var defaultCategoryFactory: FakeDefaultCategoryFactory
    @Inject lateinit var defaultWallpaperCategoryClient: FakeDefaultWallpaperCategoryClient
    @Inject lateinit var liveWallpapersClient: LiveWallpapersClient
    @Inject lateinit var testScope: TestScope
    @Inject lateinit var testInjector: TestInjector

    lateinit var repository: DefaultWallpaperCategoryRepository

    @Before
    fun setUp() {
        hiltRule.inject()
        InjectorProvider.setInjector(testInjector)
    }

    @Test
    fun `fetchAllCategories should update categories and set isAllCategoriesFetched to true`() =
        runTest {
            val category1: Category =
                ImageCategory(
                    /* title */ "My photos",
                    /* collection */ "image_wallpapers",
                    /* priority */ 0,
                )

            val wallpapers = ArrayList<WallpaperInfo>()
            val wallpaperInfo: WallpaperInfo = TestStaticWallpaperInfo(0)
            wallpapers.add(wallpaperInfo)
            val category2: Category =
                TestWallpaperCategory(
                    /* title */ "Test category",
                    /* collection */ "init_collection",
                    wallpapers,
                    /* priority */ 1,
                )

            val thirdPartyLiveWallpaperCategory: Category =
                ThirdPartyLiveWallpaperCategory(
                    /* title */ "Third_Party_Title",
                    /* collection */ "Third_Party_CollectionId",
                    wallpapers,
                    /* priority */ 1,
                    emptySet(),
                )

            val mCategories = ArrayList<Category>()
            mCategories.add(category1)
            mCategories.add(category2)

            defaultWallpaperCategoryClient.setSystemCategories(mCategories)
            defaultWallpaperCategoryClient.setThirdPartyLiveWallpaperCategories(
                listOf(thirdPartyLiveWallpaperCategory)
            )

            repository =
                DefaultWallpaperCategoryRepository(
                    context,
                    defaultWallpaperCategoryClient,
                    defaultCategoryFactory,
                    liveWallpapersClient,
                    testScope,
                )
            testScope.advanceUntilIdle()
            assertThat(repository.isDefaultCategoriesFetched.value).isTrue()
            assertThat(repository.systemCategories.value.size).isEqualTo(2)
            assertThat(repository.thirdPartyLiveWallpaperCategory.value.size).isEqualTo(1)
        }

    @Test
    fun refresh_localeChange_update() = runTest {
        val category1: Category =
            ImageCategory(
                /* title */ "My photos",
                /* collection */ "image_wallpapers",
                /* priority */ 0,
            )

        val mCategories = ArrayList<Category>()
        mCategories.add(category1)

        repository =
            DefaultWallpaperCategoryRepository(
                context,
                defaultWallpaperCategoryClient,
                defaultCategoryFactory,
                liveWallpapersClient,
                testScope,
            )
        testScope.advanceUntilIdle()
        assertThat(repository.isDefaultCategoriesFetched.value).isTrue()
        assertThat(repository.systemCategories.value.size).isEqualTo(0)

        defaultWallpaperCategoryClient.setSystemCategories(mCategories)
        repository.refreshDueToLocaleChange()
        testScope.advanceUntilIdle()

        assertThat(repository.isDefaultCategoriesFetched.value).isTrue()
        assertThat(repository.systemCategories.value.size).isEqualTo(1)
    }

    @Test
    fun initialStateShouldBeEmpty() = runTest {
        repository =
            DefaultWallpaperCategoryRepository(
                context,
                defaultWallpaperCategoryClient,
                defaultCategoryFactory,
                liveWallpapersClient,
                testScope,
            )
        assertThat(repository.systemCategories.value).isEmpty()
        assertThat(repository.isDefaultCategoriesFetched.value).isFalse()
    }

    @Test
    fun refreshThirdPartyLiveWallpaperCategoriesShouldUpdateStateCorrectly() = runTest {
        repository =
            DefaultWallpaperCategoryRepository(
                context,
                defaultWallpaperCategoryClient,
                defaultCategoryFactory,
                liveWallpapersClient,
                testScope,
            )

        val job = launch { repository.refreshThirdPartyLiveWallpaperCategories() }
        assertThat(repository.isDefaultCategoriesFetched.value).isFalse()
        testScope.advanceUntilIdle()
        job.join()
        assertThat(repository.isDefaultCategoriesFetched.value).isTrue()
    }

    @Test
    fun refreshThirdPartyAppCategoriesShouldUpdateStateCorrectly() = runTest {
        repository =
            DefaultWallpaperCategoryRepository(
                context,
                defaultWallpaperCategoryClient,
                defaultCategoryFactory,
                liveWallpapersClient,
                testScope,
            )

        val job = launch { repository.refreshThirdPartyAppCategories() }
        assertThat(repository.isDefaultCategoriesFetched.value).isFalse()
        testScope.advanceUntilIdle()
        job.join()
        assertThat(repository.isDefaultCategoriesFetched.value).isTrue()
    }
}

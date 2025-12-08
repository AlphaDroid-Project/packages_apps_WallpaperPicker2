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

package com.android.wallpaper.testing

import com.android.wallpaper.picker.data.WallpaperModel
import com.android.wallpaper.picker.wallpapers.domain.interactor.CategoryWallpapersInteractor
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * This is fake [CategoryWallpapersInteractor] implementation to enable testing lower level classes
 * that depend on it like [CategoryWallpapersFragment]
 */
@Singleton
class FakeCategoryWallpapersInteractor @Inject constructor() : CategoryWallpapersInteractor {
    private val fakeWallpapers: List<WallpaperModel> =
        listOf(
            WallpaperModelUtils.getStaticWallpaperModel(
                wallpaperId = "testId1",
                collectionId = "testCollection1",
                title = "onDeviceTitle1",
            ),
            WallpaperModelUtils.getStaticWallpaperModel(
                wallpaperId = "testId2",
                collectionId = "testCollection2",
                title = "onDeviceTitle2",
            ),
            WallpaperModelUtils.getStaticWallpaperModel(
                wallpaperId = "testId3",
                collectionId = "testCollection3",
                title = "onDeviceTitle3",
            ),
            WallpaperModelUtils.getStaticWallpaperModel(
                wallpaperId = "testId1",
                collectionId = "testCollection1",
                title = "onDeviceTitle1",
            ),
            WallpaperModelUtils.getStaticWallpaperModel(
                wallpaperId = "testId2",
                collectionId = "testCollection2",
                title = "onDeviceTitle2",
            ),
            WallpaperModelUtils.getStaticWallpaperModel(
                wallpaperId = "testId3",
                collectionId = "testCollection3",
                title = "onDeviceTitle3",
            ),
            WallpaperModelUtils.getStaticWallpaperModel(
                wallpaperId = "testId1",
                collectionId = "testCollection1",
                title = "onDeviceTitle1",
            ),
            WallpaperModelUtils.getStaticWallpaperModel(
                wallpaperId = "testId2",
                collectionId = "testCollection2",
                title = "onDeviceTitle2",
            ),
            WallpaperModelUtils.getStaticWallpaperModel(
                wallpaperId = "testId3",
                collectionId = "testCollection3",
                title = "onDeviceTitle3",
            ),
            WallpaperModelUtils.getStaticWallpaperModel(
                wallpaperId = "testId1",
                collectionId = "testCollection1",
                title = "onDeviceTitle1",
            ),
            WallpaperModelUtils.getStaticWallpaperModel(
                wallpaperId = "testId2",
                collectionId = "testCollection2",
                title = "onDeviceTitle2",
            ),
            WallpaperModelUtils.getStaticWallpaperModel(
                wallpaperId = "testId3",
                collectionId = "testCollection3",
                title = "onDeviceTitle3",
            ),
            WallpaperModelUtils.getStaticWallpaperModel(
                wallpaperId = "testId1",
                collectionId = "testCollection1",
                title = "onDeviceTitle1",
            ),
            WallpaperModelUtils.getStaticWallpaperModel(
                wallpaperId = "testId2",
                collectionId = "testCollection2",
                title = "onDeviceTitle2",
            ),
            WallpaperModelUtils.getStaticWallpaperModel(
                wallpaperId = "testId3",
                collectionId = "testCollection3",
                title = "onDeviceTitle3",
            ),
            WallpaperModelUtils.getStaticWallpaperModel(
                wallpaperId = "testId1",
                collectionId = "testCollection1",
                title = "onDeviceTitle1",
            ),
            WallpaperModelUtils.getStaticWallpaperModel(
                wallpaperId = "testId2",
                collectionId = "testCollection2",
                title = "onDeviceTitle2",
            ),
            WallpaperModelUtils.getStaticWallpaperModel(
                wallpaperId = "testId3",
                collectionId = "testCollection3",
                title = "onDeviceTitle3",
            ),
            WallpaperModelUtils.getStaticWallpaperModel(
                wallpaperId = "testId1",
                collectionId = "testCollection1",
                title = "onDeviceTitle1",
            ),
            WallpaperModelUtils.getStaticWallpaperModel(
                wallpaperId = "testId2",
                collectionId = "testCollection2",
                title = "onDeviceTitle2",
            ),
            WallpaperModelUtils.getStaticWallpaperModel(
                wallpaperId = "testId3",
                collectionId = "testCollection3",
                title = "onDeviceTitle3",
            ),
            WallpaperModelUtils.getStaticWallpaperModel(
                wallpaperId = "testId1",
                collectionId = "testCollection1",
                title = "onDeviceTitle1",
            ),
            WallpaperModelUtils.getStaticWallpaperModel(
                wallpaperId = "testId2",
                collectionId = "testCollection2",
                title = "onDeviceTitle2",
            ),
            WallpaperModelUtils.getStaticWallpaperModel(
                wallpaperId = "testId3",
                collectionId = "testCollection3",
                title = "onDeviceTitle3",
            ),
        )

    override val categoryTitle: StateFlow<String>
        get() = MutableStateFlow("sample title")

    private val _selectedCategoryWallpapers = MutableStateFlow(fakeWallpapers)
    override val selectedCategoryWallpapers: StateFlow<List<WallpaperModel>> =
        _selectedCategoryWallpapers.asStateFlow()

    override val isWallpapersFetching: StateFlow<Boolean> = MutableStateFlow(false)

    fun setWallpapers(wallpaperModels: List<WallpaperModel>) {
        _selectedCategoryWallpapers.value = wallpaperModels
    }
}

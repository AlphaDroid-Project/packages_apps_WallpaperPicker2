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

package com.android.wallpaper.picker.wallpapers.domain.interactor

import com.android.wallpaper.picker.data.WallpaperModel
import com.android.wallpaper.picker.wallpapers.data.repository.CategoryWallpapersRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

/** This class provides the wallpaper related [Flow] data for the selected [CategoryModel] */
@Singleton
class DefaultCategoryWallpapersInteractor
@Inject
constructor(private val categoryWallpapersRepository: CategoryWallpapersRepository) :
    CategoryWallpapersInteractor {
    override val selectedCategoryWallpapers: StateFlow<List<WallpaperModel>>
        get() = categoryWallpapersRepository.selectedCategoryWallpapers

    override val categoryTitle: Flow<String>
        get() =
            categoryWallpapersRepository.selectedCategoryModel.map {
                it?.commonCategoryData?.title ?: ""
            }

    override val isWallpapersFetching: StateFlow<Boolean>
        get() = categoryWallpapersRepository.isWallpapersFetching
}

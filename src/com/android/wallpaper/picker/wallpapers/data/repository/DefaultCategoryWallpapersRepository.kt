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

package com.android.wallpaper.picker.wallpapers.data.repository

import android.content.Context
import com.android.wallpaper.picker.data.WallpaperModel
import com.android.wallpaper.picker.data.category.CategoryModel
import com.android.wallpaper.picker.di.modules.BackgroundDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Singleton
open class DefaultCategoryWallpapersRepository
@Inject
constructor(
    @ApplicationContext val context: Context,
    @BackgroundDispatcher private val backgroundScope: CoroutineScope,
    @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher,
) : CategoryWallpapersRepository {

    /** The selected [CategoryModel] */
    private val _selectedCategoryModel = MutableStateFlow<CategoryModel?>(null)
    override val selectedCategoryModel: StateFlow<CategoryModel?> =
        _selectedCategoryModel.asStateFlow()

    /**
     * A mutable map that associates a unique [String] collection id with a [WallpaperModel] for the
     * category represented by the collection id.
     *
     * This map is used to cache wallpapers by their associated category's collection id identifier,
     * name, or any other unique string key.
     *
     * Example usage:
     * ```
     * wallpaperMap["beach"] = beachWallpaperModel
     * val mountainWallpaper = wallpaperMap["mountain"]
     * ```
     */
    private val wallpapersCache: MutableMap<String, List<WallpaperModel>> = mutableMapOf()

    private val _selectedCategoryWallpapers = MutableStateFlow<List<WallpaperModel>>(emptyList())
    override val selectedCategoryWallpapers: StateFlow<List<WallpaperModel>> =
        _selectedCategoryWallpapers.asStateFlow()

    private val _isWallpapersFetching = MutableStateFlow<Boolean>(false)
    override val isWallpapersFetching: StateFlow<Boolean> = _isWallpapersFetching.asStateFlow()

    override fun setSelectedCategory(category: CategoryModel) {
        _selectedCategoryModel.value = category

        // trigger fetching of wallpapers or retrieve from cache
        getWallpapers(category)
    }

    private fun getWallpapers(category: CategoryModel) {
        _isWallpapersFetching.value = true
        backgroundScope.launch {
            val result =
                withContext(backgroundDispatcher) {
                    category.commonCategoryData.fetchWallpapers?.invoke(
                        category.commonCategoryData.collectionId
                    )
                }
            _selectedCategoryWallpapers.value = result ?: emptyList()
            _isWallpapersFetching.value = false
        }
    }

    override fun refreshWallpapers() {
        _selectedCategoryModel.value?.let { getWallpapers(it) }
    }
}

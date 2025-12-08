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

import com.android.wallpaper.picker.data.WallpaperModel
import com.android.wallpaper.picker.data.category.CategoryModel
import kotlinx.coroutines.flow.StateFlow

/**
 * A repository interface for accessing wallpaper-related data for a [CategoryModel]
 *
 * This interface exposes a [Flow] which emits the selected [CategoryModel] and he its wallpapers
 * and the fetching status.
 */
interface CategoryWallpapersRepository {

    val selectedCategoryModel: StateFlow<CategoryModel?>

    /**
     * A [StateFlow] that emits the list of [WallpaperModel]s associated with the currently selected
     * wallpaper category.
     *
     * Observers can collect this flow to receive updates whenever the selected category's
     * wallpapers change.
     */
    val selectedCategoryWallpapers: StateFlow<List<WallpaperModel>>

    /**
     * A [StateFlow] that indicates whether wallpapers are currently being fetched.
     *
     * `true` when a fetch operation is ongoing, `false` otherwise.
     */
    val isWallpapersFetching: StateFlow<Boolean>

    /**
     * Updates the selected wallpaper category to the given [category].
     *
     * Implementations should use this method to trigger loading or caching of wallpapers associated
     * with the provided category, and update the [selectedCategoryWallpapers] flow accordingly.
     *
     * @param category The new category to select for displaying wallpapers.
     */
    fun setSelectedCategory(category: CategoryModel)

    /** Updates the selected wallpaper category to reload its wallpaper data. */
    fun refreshWallpapers()
}

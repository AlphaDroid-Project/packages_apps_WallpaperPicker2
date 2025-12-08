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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/** Interface that defines stateful wallpapers data for the selected category */
interface CategoryWallpapersInteractor {

    val categoryTitle: Flow<String>

    /**
     * A [StateFlow] that emits the list of [WallpaperModel]s for the currently selected category.
     *
     * This flow is updated whenever the user selects a new category, and reflects the current set
     * of wallpapers associated with that category.
     */
    val selectedCategoryWallpapers: StateFlow<List<WallpaperModel>>

    /**
     * A [StateFlow] that indicates whether the wallpaper data is currently being fetched.
     *
     * Emits `true` while wallpapers are loading, and `false` when loading is complete or idle.
     */
    val isWallpapersFetching: StateFlow<Boolean>
}

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

package com.android.wallpaper.picker.wallpapers.ui.view.viewmodel

/**
 * View model representing the overall layout of the Wallpapers screen.
 *
 * @property rotationEnabled Indicates whether wallpaper rotation (e.g., daily refresh) is supported
 *   for the current category.
 * @property wallpaperItems A list of [CategoryWallpapersItemViewModel] representing the various
 *   sections or items displayed on the screen.
 */
data class CategoryWallpapersContentViewModel(
    val rotationEnabled: Boolean,
    val title: String,
    val wallpaperItems: List<CategoryWallpapersItemViewModel>,
)

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

import android.content.Intent
import com.android.wallpaper.asset.Asset

/**
 * A sealed class that represents the different types of items or sections on the Wallpapers screen.
 */
sealed class CategoryWallpapersItemViewModel {

    /**
     * Represents the primary header section which holds the main title of the Wallpapers screen.
     *
     * @property title The title text to display in the primary header.
     */
    data class PrimaryHeaderViewModelCategory(val title: String) :
        CategoryWallpapersItemViewModel()

    /**
     * Represents the secondary header section which holds any secondary label on the Wallpapers
     * screen.
     *
     * @property title The title text to display in the secondary header.
     */
    data class SecondaryHeaderViewModelCategory(val title: String) :
        CategoryWallpapersItemViewModel()

    /**
     * Represents a section that holds a dual row view of template thumbnails.
     *
     * @property thumbnailAssets A list of [ThumbnailsViewModelCategory] representing the template
     *   thumbnails to display.
     */
    data class TemplateThumbnailsViewModelCategory(
        val thumbnailAssets: List<ThumbnailsViewModelCategory>
    ) : CategoryWallpapersItemViewModel()

    /**
     * Represents a section that holds plain wallpaper thumbnails
     *
     * @property thumbnailAssets A list of [ThumbnailsViewModelCategory] representing the thumbnails
     *   to display.
     */
    data class PlainThumbnailsViewModelCategory(
        val thumbnailAssets: List<ThumbnailsViewModelCategory>
    ) : CategoryWallpapersItemViewModel()

    /**
     * Represents a section that holds a single thumbnail preview of a wallpaper or template.
     *
     * @property thumbnailAsset The [Asset] associated with the thumbnail.
     * @property title An optional title associated with the thumbnail.
     * @property contentDescription An optional content description for accessibility.
     * @property onSectionClicked An optional callback invoked when the thumbnail is clicked.
     */
    data class ThumbnailsViewModelCategory(
        val thumbnailAsset: Asset,
        val title: String?,
        val contentDescription: String?,
        val isApplied: Boolean = false,
        val isDownloadable: Boolean = false,
        val onSectionClicked: (() -> Intent)? = null,
    ) : CategoryWallpapersItemViewModel()
}

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

package com.android.wallpaper.model

/** Defines the data columns for the wallpaper generation related information. */
object WallpaperModelContract {
    const val CATEGORY_ID = "category_id"
    const val CATEGORY_TITLE = "category_title"
    const val CATEGORY_THUMBNAIL = "category_thumbnail"
    const val CATEGORY_PRIORITY = "category_priority"
    const val CATEGORY_IS_COLLECTION_WALLPAPER = "category_is_collection_wallpaper"
    const val ASSET_ID = "asset_id"
    const val WALLPAPER_TITLE = "wallpaper_title"
    const val WALLPAPER_AUTHOR = "wallpaper_author"
    const val WALLPAPER_DESCRIPTION = "wallpaper_description"
    const val WALLPAPER_DESCRIPTION_CONTENT_HANDLING = "wallpaper_description_content_handling"
    const val WALLPAPER_CONTENT_DESCRIPTION = "wallpaper_content_description"
    const val WALLPAPER_THUMBNAIL = "wallpaper_thumbnail"
    const val WALLPAPER_CONFIG_PREVIEW_URI = "wallpaper_config_preview_uri"
    const val WALLPAPER_CLEAN_PREVIEW_URI = "wallpaper_clean_preview_uri"
    const val WALLPAPER_DELETE_URI = "wallpaper_delete_uri"
    const val WALLPAPER_SHARE_URI = "wallpaper_share_uri"
    const val WALLPAPER_GROUP_NAME = "wallpaper_group_name"
    const val WALLPAPER_IS_APPLIED = "wallpaper_is_applied"
    const val WALLPAPER_IS_NEW_CREATIVE_WALLPAPER = "wallpaper_is_new_creative_wallpaper"
    const val WALLPAPER_EFFECTS_SECTION_TITLE = "wallpaper_effects_bottom_sheet_title"
    const val WALLPAPER_EFFECTS_SECTION_SUBTITLE = "wallpaper_effects_bottom_sheet_subtitle"
    const val WALLPAPER_EFFECTS_CLEAR_URI = "wallpaper_effects_clear_uri"
    const val WALLPAPER_EFFECTS_CURRENT_ID = "wallpaper_effects_current_id"
    const val WALLPAPER_EFFECTS_BUTTON_LABEL = "wallpaper_effects_button_label"
    const val WALLPAPER_EFFECTS_TOGGLE_URI = "wallpaper_effects_toggle_uri"
    const val WALLPAPER_EFFECTS_TOGGLE_ID = "wallpaper_effects_toggle_id"
    const val CURRENT_DESTINATION = "destination"
    const val CURRENT_CONFIG_PREVIEW_URI = "wallpaper_config_preview_uri"
    const val CURRENT_DESCRIPTION = "wallpaper_description_content_handling"
}

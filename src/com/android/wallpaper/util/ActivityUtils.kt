/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.android.wallpaper.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.android.wallpaper.R
import com.android.wallpaper.module.logging.UserEventLogger
import com.android.wallpaper.picker.preview.ui.WallpaperPreviewActivity
import com.android.wallpaper.util.LaunchSourceUtils.LAUNCH_SOURCE_SETTINGS
import com.android.wallpaper.util.LaunchSourceUtils.LAUNCH_SOURCE_SETTINGS_HOMEPAGE
import com.android.wallpaper.util.LaunchSourceUtils.LAUNCH_SOURCE_SETTINGS_SEARCH
import com.android.wallpaper.util.LaunchSourceUtils.WALLPAPER_LAUNCH_SOURCE

object ActivityUtils {
    private const val SUW_NOT_YET = 0
    private const val SUW_COMPLETE = 1

    /**
     * Starts an activity with the given intent "safely" - i.e., catches exceptions that may occur
     * and displays a toast to the user in response to such issues.
     *
     * @param activity
     * @param intent
     * @param requestCode
     */
    @JvmStatic
    fun startActivityForResultSafely(activity: Activity, intent: Intent, requestCode: Int) {
        try {
            activity.startActivityForResult(intent, requestCode)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(activity, R.string.app_not_found, Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Toast.makeText(activity, R.string.app_not_found, Toast.LENGTH_SHORT).show()
            Log.e(
                "Wallpaper",
                "Wallpaper does not have the permission to launch " +
                    intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity.",
                e,
            )
        }
    }

    /**
     * Starts an activity with the given intent "safely" - i.e., catches exceptions that may occur
     * and displays a toast to the user in response to such issues.
     *
     * @param launcher
     * @param intent
     */
    @JvmStatic
    fun startActivityForResultSafely(
        context: Context,
        launcher: ActivityResultLauncher<Intent>,
        intent: Intent,
    ) {
        try {
            launcher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, R.string.app_not_found, Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Toast.makeText(context, R.string.app_not_found, Toast.LENGTH_SHORT).show()
            Log.e(
                "Wallpaper",
                ("Wallpaper does not have the permission to launch " +
                    intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity."),
                e,
            )
        }
    }

    /**
     * Starts the wallpaper preview activity.
     *
     * @param activity: The current screen that is launching the wallpaper preview
     * @param isCreativeCategories: Set to true for "creative" wallpaper categories
     * @param shouldNavigateToExtendedWallpaperEffects: If true, opens magic portrait effects
     *   options
     * @param isViewAsHome: If true, previews the wallpaper on the home screen
     * @param requestCode: A unique code to identify the result when the preview screen closes
     * @param isMultiPanesEnabled: If true, uses a multi-pane layout suitable for large screens like
     *   tablets
     */
    @JvmStatic
    fun startWallpaperPreviewActivity(
        activity: Activity,
        isCreativeCategories: Boolean,
        shouldNavigateToExtendedWallpaperEffects: Boolean,
        isViewAsHome: Boolean,
        requestCode: Int,
        isMultiPanesEnabled: Boolean,
        @UserEventLogger.SetWallpaperEntryPoint setWallpaperEntryPoint: Int,
    ) {
        val context = activity.applicationContext

        val previewIntent =
            WallpaperPreviewActivity.intentBuilder(context, true)
                .viewAsHome(isViewAsHome)
                .newTask(isMultiPanesEnabled)
                .refreshCategory(isCreativeCategories)
                .navigateToExtendedEffects(shouldNavigateToExtendedWallpaperEffects)
                .build()

        startActivityForResultSafely(activity, previewIntent, requestCode)
    }

    /**
     * Returns true if wallpaper launch source is from Settings related.
     *
     * @param intent activity intent.
     */
    @JvmStatic
    fun isLaunchedFromSettingsRelated(intent: Intent?): Boolean {
        if (intent == null) {
            return false
        }

        return isLaunchedFromSettings(intent) || isLaunchedFromSettingsSearch(intent)
    }

    /**
     * Checks if the Activity's launch source is from Settings' trampoline.
     *
     * @param intent intent to start the Activity
     * @return {@code true} if the Activity's launch source is from Settings' trampoline.
     */
    @JvmStatic
    fun isLaunchedFromSettingsTrampoline(intent: Intent?): Boolean {
        return isLaunchedFromSettingsHome(intent)
    }

    /**
     * Returns true if wallpaper launch source is from Settings.
     *
     * @param intent activity intent.
     */
    @JvmStatic
    private fun isLaunchedFromSettings(intent: Intent): Boolean {
        return TextUtils.equals(
            LAUNCH_SOURCE_SETTINGS,
            intent.getStringExtra(WALLPAPER_LAUNCH_SOURCE),
        )
    }

    @JvmStatic
    private fun isLaunchedFromSettingsHome(intent: Intent?): Boolean {
        return intent?.getBooleanExtra(LAUNCH_SOURCE_SETTINGS_HOMEPAGE, false) ?: false
    }

    /**
     * Returns true if wallpaper launch source is from Settings Search.
     *
     * @param intent activity intent.
     */
    @JvmStatic
    fun isLaunchedFromSettingsSearch(intent: Intent): Boolean {
        return TextUtils.equals(
            LAUNCH_SOURCE_SETTINGS_SEARCH,
            intent.getStringExtra(WALLPAPER_LAUNCH_SOURCE),
        )
    }

    /**
     * Returns true if wallpaper is in SUW mode.
     *
     * @param context activity's context.
     */
    @JvmStatic
    fun isSUWMode(context: Context): Boolean {
        return (Settings.Secure.getInt(
            context.contentResolver,
            Settings.Secure.USER_SETUP_COMPLETE,
            SUW_COMPLETE,
        ) == SUW_NOT_YET)
    }

    /**
     * Returns true if it's wallpaper only mode.
     *
     * @param intent activity intent.
     */
    @JvmStatic
    fun isWallpaperOnlyMode(intent: Intent): Boolean {
        return "wallpaper_only" == intent.getStringExtra("com.android.launcher3.WALLPAPER_FLAVOR")
    }

    /**
     * Returns `true` if the activity was launched from the home screen (launcher); `false`
     * otherwise.
     */
    @JvmStatic
    fun isLaunchedFromLauncher(intent: Intent): Boolean {
        return LaunchSourceUtils.LAUNCH_SOURCE_LAUNCHER ==
            intent.getStringExtra(WALLPAPER_LAUNCH_SOURCE)
    }
}

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

package com.android.wallpaper.util

import android.app.Activity.RESULT_OK
import android.app.WallpaperManager.FLAG_LOCK
import android.app.WallpaperManager.FLAG_SYSTEM
import android.app.wallpaper.WallpaperDescription
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.graphics.Rect
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.android.systemui.shared.Flags.panAndZoomInExtendedWallpaperEffects
import com.android.wallpaper.config.BaseFlags
import com.android.wallpaper.model.WallpaperInfoContract.WALLPAPER_DESCRIPTION_CONTENT_HANDLING
import com.android.wallpaper.module.ExtendedEffectsHelper
import com.android.wallpaper.picker.data.Destination
import com.android.wallpaper.picker.data.WallpaperModel
import com.android.wallpaper.picker.data.WallpaperModel.LiveWallpaperModel
import com.android.wallpaper.picker.data.WallpaperModel.StaticWallpaperModel
import com.android.wallpaper.picker.preview.ui.fragment.SmallPreviewFragment.Companion.PREVIEW_RESULT_REGISTRY
import com.android.wallpaper.picker.preview.ui.util.ContentHandlingUtil
import com.android.wallpaper.picker.preview.ui.viewmodel.WallpaperPreviewViewModel
import com.android.wallpaper.util.wallpaperconnection.WallpaperConnectionUtils
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** This class provides utility methods to facilitate the extended wallpaper effects flow */
object ExtendedWallpaperEffectsUtils {
    const val TAG = "ExtendedWallpaperEffectsUtils"

    /** Parameters of the Intent that starts the editor activity */
    const val PHOTO_URI = "PHOTO_URI"
    const val PHOTO_CROPS = "PHOTO_CROPS"
    const val SOURCE_BITMAP_SCREEN = "SOURCE_BITMAP_SCREEN"

    private lateinit var extendedEffectsHelper: ExtendedEffectsHelper

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface EntryPointInjector {
        fun getExtendedEffectsHelper(): ExtendedEffectsHelper
    }

    private fun getExtendedEffectsHelper(context: Context): ExtendedEffectsHelper {
        if (!this::extendedEffectsHelper.isInitialized) {
            extendedEffectsHelper =
                EntryPoints.get(context, EntryPointInjector::class.java).getExtendedEffectsHelper()
        }
        return extendedEffectsHelper
    }

    fun isExtendedEffectWallpaper(context: Context, component: ComponentName): Boolean {
        return getExtendedEffectsHelper(context).isExtendedEffectWallpaper(component)
    }

    fun registerExtendedWallpaperEffectsActivityLauncher(
        activity: FragmentActivity,
        lifecycleOwner: LifecycleOwner,
        wallpaperPreviewViewModel: WallpaperPreviewViewModel,
        context: Context?,
    ): ActivityResultLauncher<Intent> {
        return activity.activityResultRegistry.register(
            PREVIEW_RESULT_REGISTRY,
            lifecycleOwner,
            object : ActivityResultContract<Intent, Int>() {
                override fun createIntent(context: Context, input: Intent): Intent {
                    return input
                }

                override fun parseResult(resultCode: Int, intent: Intent?): Int {
                    if (resultCode == RESULT_OK) {
                        wallpaperPreviewViewModel.wallpaper.value?.let { unpackedWallpaperModel ->
                            context?.let { unpackedContext ->
                                ContentHandlingUtil.updatePreview(
                                    context = unpackedContext.applicationContext,
                                    wallpaperModel = unpackedWallpaperModel,
                                    wallpaperDescription =
                                        intent
                                            ?.extras
                                            ?.getParcelable(
                                                WALLPAPER_DESCRIPTION_CONTENT_HANDLING,
                                                WallpaperDescription::class.java,
                                            ),
                                ) { wallpaperModel ->
                                    wallpaperPreviewViewModel.setShouldUpdateSelectedPreviewTab(
                                        true
                                    )
                                    wallpaperPreviewViewModel.setPreviewWallpaperModel(
                                        wallpaperModel
                                    )
                                }
                            }
                        }
                    }
                    return resultCode
                }
            },
        ) {}
    }

    private fun isExtendedEffectWallpaperModel(
        model: WallpaperModel?,
        context: Context,
        flags: BaseFlags,
    ): Boolean =
        flags.isExtendedWallpaperEnabled() &&
            model is LiveWallpaperModel &&
            model.liveWallpaperData.isEffectWallpaper &&
            isExtendedEffectWallpaper(
                context,
                model.liveWallpaperData.systemWallpaperInfo.component,
            )

    suspend fun startExtendedWallpaperEffects(
        wallpaper: WallpaperModel,
        launcher: ActivityResultLauncher<Intent>,
        context: Context,
        wallpaperConnectionUtils: WallpaperConnectionUtils,
        flags: BaseFlags,
    ) {
        val isExtendedEffect = isExtendedEffectWallpaperModel(wallpaper, context, flags)
        launchExtendedWallpaperEffects(wallpaper, launcher, isExtendedEffect, context)
        if (isExtendedEffect) {
            // Disconnect engine if it's live extended effect wallpaper
            wallpaperConnectionUtils.disconnect(
                (wallpaper as LiveWallpaperModel)
                    .liveWallpaperData
                    .systemWallpaperInfo
                    .component
                    .packageName
            )
        }
    }

    private fun launchExtendedWallpaperEffects(
        wallpaper: WallpaperModel,
        launcher: ActivityResultLauncher<Intent>,
        isExtendedEffect: Boolean,
        context: Context,
    ) {
        val extendedWallpaperIntent = getExtendedEffectsHelper(context).getExtendedEffectIntent()
        val extendedWallpaperPackageName = extendedWallpaperIntent.component?.packageName ?: ""
        if (isExtendedEffect) {
            // Extended effect wallpaper, launch with description
            extendedWallpaperIntent.putExtra(
                WALLPAPER_DESCRIPTION_CONTENT_HANDLING,
                (wallpaper as LiveWallpaperModel).liveWallpaperData.description,
            )
        } else if (BaseFlags.get().isRecentWallpapersFromSystemEnabled(context)) {
            Log.d(TAG, "destination: ${wallpaper.commonWallpaperData.destination}")
            when (wallpaper.commonWallpaperData.destination) {
                Destination.NOT_APPLIED -> {
                    wallpaper as StaticWallpaperModel
                    wallpaper.imageWallpaperData?.uri?.let { photoUri ->
                        Log.d(TAG, "Using photoUri: $photoUri")
                        context.grantUriPermission(
                            extendedWallpaperPackageName,
                            photoUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION,
                        )
                        extendedWallpaperIntent.putExtra(PHOTO_URI, photoUri)
                        if (panAndZoomInExtendedWallpaperEffects()) {
                            val parcelableMap =
                                HashMap<Point, Rect>(
                                    wallpaper.staticWallpaperData.cropHints ?: emptyMap()
                                )
                            extendedWallpaperIntent.putExtra(PHOTO_CROPS, parcelableMap)
                        }
                    }
                }

                Destination.APPLIED_TO_SYSTEM,
                Destination.APPLIED_TO_SYSTEM_LOCK -> {
                    Log.d(TAG, "Using home screen bitmap")
                    extendedWallpaperIntent.putExtra(SOURCE_BITMAP_SCREEN, FLAG_SYSTEM)
                }

                Destination.APPLIED_TO_LOCK -> {
                    Log.d(TAG, "Using lock screen bitmap")
                    extendedWallpaperIntent.putExtra(SOURCE_BITMAP_SCREEN, FLAG_LOCK)
                }
            }
        } else {
            val photoUri = (wallpaper as StaticWallpaperModel).imageWallpaperData?.uri
            Log.d("ExtendedWallpaperEffectsUtils", "PhotoURI is: $photoUri")
            photoUri?.let {
                context.grantUriPermission(
                    extendedWallpaperPackageName,
                    photoUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
                extendedWallpaperIntent.putExtra("PHOTO_URI", it)
            }
        }
        try {
            launcher.launch(extendedWallpaperIntent)
        } catch (ex: ActivityNotFoundException) {
            Log.e(TAG, "Extended Wallpaper Activity is not available", ex)
        }
    }
}

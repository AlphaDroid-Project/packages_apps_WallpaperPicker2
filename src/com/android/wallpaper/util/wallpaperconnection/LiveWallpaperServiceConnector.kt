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

package com.android.wallpaper.util.wallpaperconnection

import android.app.WallpaperInfo
import android.app.wallpaper.WallpaperDescription
import android.graphics.Point
import android.graphics.Rect
import android.os.IBinder
import android.service.wallpaper.IWallpaperConnection
import android.service.wallpaper.IWallpaperService
import android.util.Log
import android.view.WindowManager
import com.android.wallpaper.config.BaseFlags
import java.lang.reflect.InvocationTargetException

/**
 * Util to attach a live wallpaper [IWallpaperConnection] to [IWallpaperService].
 *
 * [LiveWallpaperServiceConnector] is only used when the flag refactor_wallpaper_preview_screen_flag
 * is turned on and should be gated with the flag.
 */
object LiveWallpaperServiceConnector {

    private const val TAG = "LiveWallpaperServiceConnector"

    /**
     * This method tries to call historical versions of IWallpaperService#attach since this code may
     * be running against older versions of Android. We have no control over what versions of
     * Android third party users of this code will be running.
     */
    fun attachWallpaperConnection(
        wallpaperConnection: IWallpaperConnection,
        wallpaperService: IWallpaperService,
        destinationFlag: Int,
        description: WallpaperDescription,
        displaySize: Point,
        windowToken: IBinder,
        displayId: Int,
    ) {
        if (!BaseFlags.get().isRefactorWallpaperPreviewScreenEnabled()) {
            throw IllegalStateException(
                "LiveWallpaperServiceConnector can only be used when " +
                    "refactor_wallpaper_preview_screen_flag is turned on."
            )
        }

        // 1. Try the latest API to attach
        try {
            wallpaperService.attach(
                wallpaperConnection,
                windowToken,
                WindowManager.LayoutParams.TYPE_APPLICATION_MEDIA,
                true,
                displaySize.x,
                displaySize.y,
                Rect(0, 0, 0, 0),
                displayId,
                destinationFlag,
                null,
                description,
            )
            return
        } catch (e: NoSuchMethodError) {
            Log.w(TAG, "Error calling Baklava version of attach, trying previous versions", e)
        }

        // 2. Fall back to call the API used in pre-Baklava versions
        if (
            tryPreBAttach(
                wallpaperConnection,
                wallpaperService,
                destinationFlag,
                displaySize,
                windowToken,
                displayId,
            )
        ) {
            return
        }

        // 3. Fall back to call the API used in pre-Udc versions
        if (
            tryPreUAttach(
                wallpaperConnection,
                wallpaperService,
                destinationFlag,
                displaySize,
                windowToken,
                displayId,
            )
        ) {
            return
        }
    }

    /**
     * Tries to call the attach method used in Android 14(U) and earlier, returning true on success
     * otherwise false.
     */
    private fun tryPreUAttach(
        wallpaperConnection: IWallpaperConnection,
        wallpaperService: IWallpaperService,
        destinationFlag: Int,
        displaySize: Point,
        windowToken: IBinder,
        displayId: Int,
    ): Boolean {
        try {
            val method =
                wallpaperService.javaClass.getMethod(
                    "attach",
                    IWallpaperConnection::class.java,
                    IBinder::class.java,
                    Int::class.javaPrimitiveType,
                    Boolean::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType,
                    Rect::class.java,
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType,
                )
            method.invoke(
                wallpaperService,
                wallpaperConnection,
                windowToken,
                WindowManager.LayoutParams.TYPE_APPLICATION_MEDIA,
                true,
                displaySize.x,
                displaySize.y,
                Rect(0, 0, 0, 0),
                displayId,
                destinationFlag,
            )
            return true
        } catch (e: Exception) {
            when (e) {
                is NoSuchMethodException,
                is InvocationTargetException,
                is IllegalAccessException -> {
                    Log.w(
                        TAG,
                        "live wallpaper content handling enabled, but pre-U attach method called",
                    )
                    return false
                }

                else -> throw e
            }
        }
    }

    /**
     * Tries to call the attach method used in Android 16(B) and earlier, returning true on success
     * otherwise false.
     */
    private fun tryPreBAttach(
        wallpaperConnection: IWallpaperConnection,
        wallpaperService: IWallpaperService,
        destinationFlag: Int,
        displaySize: Point,
        windowToken: IBinder,
        displayId: Int,
    ): Boolean {
        try {
            val method =
                wallpaperService.javaClass.getMethod(
                    "attach",
                    IWallpaperConnection::class.java,
                    IBinder::class.java,
                    Int::class.javaPrimitiveType,
                    Boolean::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType,
                    Rect::class.java,
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType,
                    WallpaperInfo::class.java,
                )
            method.invoke(
                wallpaperService,
                wallpaperConnection,
                windowToken,
                WindowManager.LayoutParams.TYPE_APPLICATION_MEDIA,
                true,
                displaySize.x,
                displaySize.y,
                Rect(0, 0, 0, 0),
                displayId,
                destinationFlag,
                null,
            )
            return true
        } catch (e: Exception) {
            when (e) {
                is NoSuchMethodException,
                is InvocationTargetException,
                is IllegalAccessException -> {
                    Log.w(
                        TAG,
                        "live wallpaper content handling enabled, but pre-B attach method called",
                    )
                    return false
                }

                else -> throw e
            }
        }
    }
}

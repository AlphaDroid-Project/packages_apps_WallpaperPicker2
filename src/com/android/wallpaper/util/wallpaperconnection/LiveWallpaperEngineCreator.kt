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

import android.app.WallpaperColors
import android.app.wallpaper.WallpaperDescription
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.os.RemoteException
import android.service.wallpaper.IWallpaperConnection
import android.service.wallpaper.IWallpaperEngine
import android.service.wallpaper.IWallpaperService
import android.util.Log
import androidx.core.os.bundleOf
import com.android.wallpaper.config.BaseFlags
import com.android.wallpaper.util.WallpaperConnection.WhichPreview
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Creates live wallpaper engine with [IWallpaperService].
 *
 * [LiveWallpaperEngineCreator] is only used when the flag refactor_wallpaper_preview_screen_flag is
 * turned on and should be gated with the flag.
 */
object LiveWallpaperEngineCreator {

    private const val TAG = "LiveWallpaperEngineCreator"

    private const val COMMAND_PREVIEW_INFO = "android.wallpaper.previewinfo"
    private const val WHICH_PREVIEW = "which_preview"

    suspend fun createEngine(
        wallpaperService: IWallpaperService,
        destinationFlag: Int,
        description: WallpaperDescription,
        displaySize: Point,
        windowToken: IBinder,
        displayId: Int,
        whichPreview: WhichPreview,
        onWallpaperColorsChanged: (colors: WallpaperColors?, displayId: Int) -> Unit,
    ): IWallpaperEngine {
        if (!BaseFlags.get().isRefactorWallpaperPreviewScreenEnabled()) {
            throw IllegalStateException(
                "LiveWallpaperEngineCreator can only be used when " +
                    "refactor_wallpaper_preview_screen_flag is turned on."
            )
        }
        return suspendCancellableCoroutine { c: CancellableContinuation<IWallpaperEngine> ->
            val wallpaperConnection =
                object : IWallpaperConnection.Stub() {

                    override fun attachEngine(engine: IWallpaperEngine?, displayId: Int) {
                        engine?.apply {
                            try {
                                setVisibility(true)
                                resizePreview(Rect(0, 0, displaySize.x, displaySize.y))
                                requestWallpaperColors()
                            } catch (e: RemoteException) {
                                Log.w(TAG, "Error in attachEngine", e)
                            }
                        }
                    }

                    override fun engineShown(engine: IWallpaperEngine?) {
                        if (engine != null) {
                            engine.dispatchWallpaperPreviewInfo(whichPreview)
                            c.resumeWith(Result.success(engine))
                        }
                    }

                    override fun setWallpaper(p0: String?): ParcelFileDescriptor? {
                        // Do nothing intended.
                        return null
                    }

                    override fun onWallpaperColorsChanged(
                        colors: WallpaperColors?,
                        displayId: Int,
                    ) {
                        onWallpaperColorsChanged.invoke(colors, displayId)
                    }

                    override fun onLocalWallpaperColorsChanged(
                        p0: RectF?,
                        p1: WallpaperColors?,
                        p2: Int,
                    ) {
                        // Do nothing intended.
                    }
                }

            LiveWallpaperServiceConnector.attachWallpaperConnection(
                wallpaperConnection = wallpaperConnection,
                wallpaperService = wallpaperService,
                destinationFlag = destinationFlag,
                description = description,
                displaySize = displaySize,
                windowToken = windowToken,
                displayId = displayId,
            )
        }
    }

    private fun IWallpaperEngine.dispatchWallpaperPreviewInfo(whichPreview: WhichPreview) {
        val bundle = bundleOf(WHICH_PREVIEW to whichPreview.value)
        try {
            // Dispatches a command to the wallpaper engine to inform it about which preview
            // context is currently active (e.g., if it's in a preview or edit mode).
            dispatchWallpaperCommand(COMMAND_PREVIEW_INFO, 0, 0, 0, bundle)
        } catch (e: RemoteException) {
            Log.e(TAG, "Error dispatching wallpaper command: $whichPreview", e)
        }
    }
}

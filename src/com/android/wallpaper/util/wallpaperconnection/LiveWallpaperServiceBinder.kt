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

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.service.wallpaper.IWallpaperService
import android.service.wallpaper.WallpaperService
import com.android.wallpaper.config.BaseFlags
import com.android.wallpaper.picker.data.WallpaperModel.LiveWallpaperModel
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Binds the live wallpaper service.
 *
 * [LiveWallpaperServiceBinder] is only used when the flag refactor_wallpaper_preview_screen_flag is
 * turned on and should be gated with the flag.
 */
object LiveWallpaperServiceBinder {

    suspend fun bindWallpaperService(
        context: Context,
        wallpaperModel: LiveWallpaperModel,
    ): Pair<ServiceConnection, IWallpaperService> {
        if (!BaseFlags.get().isRefactorWallpaperPreviewScreenEnabled()) {
            throw IllegalStateException(
                "LiveWallpaperServiceBinder can only be used when " +
                    "refactor_wallpaper_preview_screen_flag is turned on."
            )
        }

        return suspendCancellableCoroutine {
            c: CancellableContinuation<Pair<ServiceConnection, IWallpaperService>> ->
            val intent = wallpaperModel.getWallpaperServiceIntent()
            val serviceConnection =
                WallpaperServiceConnection(
                    object : WallpaperServiceConnection.WallpaperServiceConnectionListener {
                        override fun onWallpaperServiceConnected(
                            serviceConnection: ServiceConnection,
                            wallpaperService: IWallpaperService,
                        ) {
                            if (c.isActive) {
                                c.resumeWith(
                                    Result.success(Pair(serviceConnection, wallpaperService))
                                )
                            }
                        }
                    }
                )
            val success =
                context.bindService(
                    intent,
                    serviceConnection,
                    Context.BIND_AUTO_CREATE or
                        Context.BIND_IMPORTANT or
                        Context.BIND_ALLOW_ACTIVITY_STARTS,
                )
            if (!success && c.isActive) {
                c.resumeWith(Result.failure(Exception("Fail to bind the live wallpaper service.")))
            }
        }
    }

    private fun LiveWallpaperModel.getWallpaperServiceIntent(): Intent {
        return liveWallpaperData.systemWallpaperInfo.let {
            Intent(WallpaperService.SERVICE_INTERFACE).setClassName(it.packageName, it.serviceName)
        }
    }
}

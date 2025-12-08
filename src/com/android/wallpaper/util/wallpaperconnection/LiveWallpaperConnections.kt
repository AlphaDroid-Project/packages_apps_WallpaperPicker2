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
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import android.service.wallpaper.IWallpaperEngine
import android.service.wallpaper.IWallpaperService
import android.util.Log
import com.android.wallpaper.config.BaseFlags
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Keeps references of the connections created when rendering a live wallpapers, so that they can
 * later be cleaned up when needed.
 *
 * [LiveWallpaperConnections] is only used when the flag refactor_wallpaper_preview_screen_flag is
 * turned on and should be gated with the flag.
 */
data class LiveWallpaperConnections(
    val wallpaperEngine: WeakReference<IWallpaperEngine>,
    val serviceConnection: WeakReference<ServiceConnection>,
    val wallpaperService: WeakReference<IWallpaperService>,
    val windowToken: WeakReference<IBinder>,
) {
    private val disconnected = AtomicBoolean(false)

    init {
        if (!BaseFlags.get().isRefactorWallpaperPreviewScreenEnabled()) {
            throw IllegalStateException(
                "LiveWallpaperConnections can only be used when " +
                    "refactor_wallpaper_preview_screen_flag is turned on."
            )
        }
    }

    fun disconnect(context: Context) {
        if (disconnected.compareAndSet(false, true)) {
            wallpaperEngine.get()?.apply {
                try {
                    destroy()
                } catch (e: RemoteException) {
                    Log.w(
                        TAG,
                        "Error destroying wallpaper engine. It can be that engine is " +
                            "already destroyed.",
                        e,
                    )
                }
            }
            windowToken.get()?.let { windowToken ->
                wallpaperService.get()?.let { service ->
                    try {
                        service.detach(windowToken)
                    } catch (e: RemoteException) {
                        Log.w(
                            TAG,
                            "Error disconnecting wallpaper service. It can be that " +
                                "service has already detached.",
                            e,
                        )
                    }
                }
            }
            serviceConnection.get()?.let {
                try {
                    context.unbindService(it)
                } catch (e: RemoteException) {
                    Log.w(
                        TAG,
                        "Error unbinding service connection. It can be that service " +
                            "connection has already unbound.",
                        e,
                    )
                }
            }
        }
    }

    companion object {
        const val TAG = "LiveWallpaperConnections"
    }
}

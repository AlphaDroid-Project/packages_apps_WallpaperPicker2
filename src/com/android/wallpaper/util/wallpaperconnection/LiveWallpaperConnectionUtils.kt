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
import android.app.WallpaperInfo
import android.app.wallpaper.WallpaperDescription
import android.content.Context
import android.content.ServiceConnection
import android.graphics.Point
import android.os.IBinder
import android.service.wallpaper.IWallpaperEngine
import com.android.wallpaper.config.BaseFlags
import com.android.wallpaper.picker.data.WallpaperModel.LiveWallpaperModel
import com.android.wallpaper.util.WallpaperConnection.WhichPreview
import com.android.wallpaper.util.toDescription
import dagger.hilt.android.scopes.ActivityRetainedScoped
import java.lang.ref.WeakReference
import javax.inject.Inject
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Handles connecting the rendering of live wallpapers.
 *
 * [LiveWallpaperConnectionUtils] is only used when the flag refactor_wallpaper_preview_screen_flag
 * is turned on and should be gated with the flag.
 */
@ActivityRetainedScoped
class LiveWallpaperConnectionUtils @Inject constructor() {

    // Note that we only need to use mutex and cache the engine map when forceSingleEngine is true.
    // Otherwise, we always create a new engine when connect.
    private val liveWallpaperEngine: MutableMap<String, IWallpaperEngine> = mutableMapOf()
    private val mutex = Mutex()

    init {
        if (!BaseFlags.get().isRefactorWallpaperPreviewScreenEnabled()) {
            throw IllegalStateException(
                "LiveWallpaperConnectionUtils can only be used when " +
                    "refactor_wallpaper_preview_screen_flag is turned on."
            )
        }
    }

    suspend fun connect(
        context: Context,
        wallpaperModel: LiveWallpaperModel,
        forceSingleEngine: Boolean,
        destinationFlag: Int,
        displaySize: Point,
        windowToken: IBinder,
        displayId: Int,
        whichPreview: WhichPreview,
        onEngineCreated: (engine: IWallpaperEngine) -> Unit,
        onWallpaperColorsChanged: (colors: WallpaperColors?, displayId: Int) -> Unit,
    ): IWallpaperEngine {
        val connectionKey = wallpaperModel.getConnectionKey()
        if (forceSingleEngine) {
            mutex.withLock {
                if (liveWallpaperEngine.contains(connectionKey)) {
                    return liveWallpaperEngine.getValue(connectionKey)
                }
            }
        }

        val (serviceConnection, wallpaperService) =
            LiveWallpaperServiceBinder.bindWallpaperService(
                context = context,
                wallpaperModel = wallpaperModel,
            )
        val engine: IWallpaperEngine =
            LiveWallpaperEngineCreator.createEngine(
                    wallpaperService = wallpaperService,
                    destinationFlag = destinationFlag,
                    description = wallpaperModel.toDescription(),
                    displaySize = displaySize,
                    windowToken = windowToken,
                    displayId = displayId, // TODO(b/423956081): give a proper ID here
                    whichPreview = whichPreview,
                    onWallpaperColorsChanged = onWallpaperColorsChanged,
                )
                .also {
                    onEngineCreated.invoke(it)
                    if (forceSingleEngine) {
                        mutex.withLock { liveWallpaperEngine[connectionKey] = it }
                    }
                }

        val connection =
            LiveWallpaperConnections(
                wallpaperEngine = WeakReference(engine),
                serviceConnection = WeakReference(serviceConnection),
                wallpaperService = WeakReference(wallpaperService),
                windowToken = WeakReference(windowToken),
            )

        // Listen to link death and disconnect connection
        (serviceConnection as? WallpaperServiceConnection)?.deadConnectionListener =
            object : WallpaperServiceConnection.DeadConnectionListener {
                override fun onConnectionDead(serviceConnection: ServiceConnection) {
                    connection.disconnect(context)
                }
            }
        wallpaperService.asBinder()?.linkToDeath({ connection.disconnect(context) }, 0)
        engine.asBinder()?.linkToDeath({ connection.disconnect(context) }, 0)

        return engine
    }

    private fun LiveWallpaperModel.getConnectionKey(): String {
        val wallpaperInfo: WallpaperInfo = liveWallpaperData.systemWallpaperInfo
        val description: WallpaperDescription = liveWallpaperData.description
        return "${wallpaperInfo.packageName}:${wallpaperInfo.serviceName}:${description.id}"
    }
}

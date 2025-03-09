/*
 * Copyright (C) 2024 The Android Open Source Project
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

package com.android.wallpaper.picker.customization.data.repository

import android.os.Bundle
import com.android.systemui.shared.customization.data.content.CustomizationProviderClient
import com.android.systemui.shared.customization.data.content.CustomizationProviderContract
import com.android.wallpaper.picker.di.modules.BackgroundDispatcher
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

@Singleton
class CustomizationRuntimeValuesRepository
@Inject
constructor(
    @BackgroundDispatcher private val scope: CoroutineScope,
    client: CustomizationProviderClient,
) {

    private val runtimeValues: Flow<Bundle> =
        client
            .observeRuntimeValues()
            .shareIn(scope = scope, started = SharingStarted.WhileSubscribed(), replay = 1)

    /**
     * Whether the shade layout should be wide (true) or narrow (false).
     *
     * In a wide layout, notifications and quick settings each take up only half the screen width
     * (whether they are shown at the same time or not). In a narrow layout, they can each be as
     * wide as the entire screen.
     */
    val isShadeLayoutWide: Flow<Boolean> =
        runtimeValues.map {
            it.getBoolean(
                CustomizationProviderContract.RuntimeValuesTable.KEY_IS_SHADE_LAYOUT_WIDE,
                false,
            )
        }
}

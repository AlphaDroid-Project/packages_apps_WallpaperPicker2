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

package com.android.wallpaper.picker.customization.ui.viewmodel

import android.content.Context
import android.content.res.Configuration
import com.android.systemui.monet.ColorScheme
import com.android.systemui.monet.Style
import com.android.wallpaper.R
import com.google.ux.material.libmonet.dynamiccolor.DynamicColor
import com.google.ux.material.libmonet.dynamiccolor.DynamicScheme
import com.google.ux.material.libmonet.dynamiccolor.MaterialDynamicColors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine

@ActivityScoped
class ColorUpdateViewModel @Inject constructor(@ApplicationContext private val context: Context) {
    private val _systemColorsUpdated: MutableSharedFlow<Unit> =
        MutableSharedFlow<Unit>(replay = 1).also { it.tryEmit(Unit) }
    val systemColorsUpdated = _systemColorsUpdated.asSharedFlow()

    private val previewingColorScheme: MutableStateFlow<DynamicScheme?> = MutableStateFlow(null)

    private val colors: MutableList<Color> = mutableListOf()

    private inner class Color(private val colorResId: Int, dynamicColor: DynamicColor) {
        private val color = MutableStateFlow(context.getColor(colorResId))
        val colorFlow =
            combine(color, previewingColorScheme) { systemColor, previewScheme ->
                if (previewScheme != null) {
                    previewScheme.getArgb(dynamicColor)
                } else systemColor
            }

        fun update() {
            color.value = context.getColor(colorResId)
        }
    }

    private fun createColorFlow(colorResId: Int, dynamicColor: DynamicColor): Flow<Int> {
        val color = Color(colorResId, dynamicColor)
        colors.add(color)
        return color.colorFlow
    }

    val colorPrimary = createColorFlow(R.color.system_primary, MaterialDynamicColors().primary())
    val colorOnPrimary =
        createColorFlow(R.color.system_on_primary, MaterialDynamicColors().onPrimary())
    val colorSecondaryContainer =
        createColorFlow(
            R.color.system_secondary_container,
            MaterialDynamicColors().secondaryContainer(),
        )
    val colorSurfaceContainer =
        createColorFlow(
            R.color.system_surface_container,
            MaterialDynamicColors().surfaceContainer(),
        )
    val colorOnSurface =
        createColorFlow(R.color.system_on_surface, MaterialDynamicColors().onSurface())
    val colorOnSurfaceVariant =
        createColorFlow(
            R.color.system_on_surface_variant,
            MaterialDynamicColors().onSurfaceVariant(),
        )
    val colorSurfaceContainerHighest =
        createColorFlow(
            R.color.system_surface_container_highest,
            MaterialDynamicColors().surfaceContainerHighest(),
        )

    fun previewColors(colorSeed: Int, @Style.Type style: Int) {
        val isDarkMode =
            (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
        previewingColorScheme.value = ColorScheme(colorSeed, isDarkMode, style).materialScheme
    }

    fun resetPreview() {
        previewingColorScheme.value = null
    }

    fun updateColors() {
        _systemColorsUpdated.tryEmit(Unit)
        colors.forEach { it.update() }
    }
}

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
package com.android.wallpaper.picker.preview.ui.binder

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import androidx.core.view.doOnLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.android.app.tracing.TraceUtils.trace
import com.android.wallpaper.picker.preview.ui.util.FullResImageViewUtil2
import com.android.wallpaper.picker.preview.ui.viewmodel.StaticWallpaperPreviewViewModel
import com.android.wallpaper.util.RtlUtils
import com.android.wallpaper.util.WallpaperCropUtils
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import kotlinx.coroutines.launch

object StaticWallpaperPreviewBinder2 {

    fun bind(
        scaleImageView: SubsamplingScaleImageView,
        viewModel: StaticWallpaperPreviewViewModel,
        applicationContext: Context,
        displaySize: Point,
        lifecycleOwner: LifecycleOwner,
    ) {
        // TODO(b/423956081): Implement panning to zoom of SubsamplingScaleImageView when fullscreen

        lifecycleOwner.lifecycleScope.launch {
            launch {
                viewModel.subsamplingScaleImageViewModel.collect { imageModel ->
                    trace(TAG) {
                        val cropHint = imageModel.fullPreviewCropModels?.get(displaySize)?.cropHint
                        scaleImageView.setImage(
                            ImageSource.cachedBitmap(imageModel.rawWallpaperBitmap),
                            imageModel.rawWallpaperSize,
                            displaySize,
                            cropHint,
                            RtlUtils.isRtl(applicationContext),
                        )
                    }
                }
            }
        }
    }

    private fun SubsamplingScaleImageView.setImage(
        imageSource: ImageSource,
        rawWallpaperSize: Point,
        displaySize: Point,
        cropHint: Rect?,
        isRtl: Boolean,
    ) {
        val scale: Float = WallpaperCropUtils.getSystemWallpaperMaximumScale(context)
        // Set the full res image
        setImage(imageSource)
        // Calculate the scale and the center point for the full res image
        doOnLayout {
            FullResImageViewUtil2.getScaleAndCenter(
                    displaySize,
                    rawWallpaperSize,
                    displaySize,
                    cropHint,
                    isRtl,
                    systemScale = scale,
                )
                .let { scaleAndCenter ->
                    minScale = scaleAndCenter.minScale
                    maxScale = scaleAndCenter.maxScale
                    setScaleAndCenter(scaleAndCenter.defaultScale, scaleAndCenter.center)
                }
        }
    }

    private const val TAG = "StaticWallpaperPreviewBinder2"
}

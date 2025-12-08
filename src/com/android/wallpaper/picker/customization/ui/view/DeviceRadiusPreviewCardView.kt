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

package com.android.wallpaper.picker.customization.ui.view

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.RoundedCorner.POSITION_TOP_LEFT
import android.view.ViewTreeObserver
import androidx.cardview.widget.CardView
import com.android.wallpaper.R

/**
 * This [CardView] displays wallpaper previews while respecting the shape if the device's window. It
 * ensures that the same corner radii are applied to the preview that are on the device. The
 * preview's corner radii are sized proportionally to the size of the preview.
 */
class DeviceRadiusPreviewCardView(context: Context, attrs: AttributeSet?) :
    CardView(context, attrs) {

    private var fullCornerRadius: Float = 0f
    private var screenHeight: Int = 0

    // in the event that the corner radius can't be determined, this value is used a fallback
    // this seems to be the case with unfolded screens failing to get the screen corner radius
    private val cornerRadiusFallback =
        resources.getDimensionPixelSize(R.dimen.preview_corner_radius)

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            viewTreeObserver.addOnPreDrawListener(
                object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        viewTreeObserver.removeOnPreDrawListener(this)

                        val insets = rootWindowInsets ?: return true
                        val roundedCorners = insets.getRoundedCorner(POSITION_TOP_LEFT)

                        fullCornerRadius =
                            roundedCorners?.radius?.toFloat() ?: cornerRadiusFallback.toFloat()
                        screenHeight = context.resources.displayMetrics.heightPixels

                        requestLayout() // Re-measure with new radius
                        return true
                    }
                }
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && screenHeight != 0) {
            val scale = measuredHeight.toFloat() / screenHeight.toFloat()
            radius = fullCornerRadius * scale
        }
    }
}

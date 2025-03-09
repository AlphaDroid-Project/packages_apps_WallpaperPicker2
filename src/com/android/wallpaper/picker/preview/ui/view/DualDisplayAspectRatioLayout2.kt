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
package com.android.wallpaper.picker.preview.ui.view

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.android.wallpaper.R
import com.android.wallpaper.model.wallpaper.DeviceDisplayType
import com.android.wallpaper.picker.preview.ui.view.DualDisplayAspectRatioLayout.Companion.getViewId

/**
 * This LinearLayout view group implements the dual preview view for the small preview screen for
 * foldable devices.
 */
class DualDisplayAspectRatioLayout2(context: Context, attrs: AttributeSet?) :
    LinearLayout(context, attrs) {

    private var previewDisplaySizes: Map<DeviceDisplayType, Point>? = null
    private var firstMeasuredWidth = 0

    /**
     * This measures the desired size of the preview views for both of foldable device's displays.
     * Each preview view respects the aspect ratio of the display it corresponds to while trying to
     * have the maximum possible height.
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (previewDisplaySizes == null) {
            setMeasuredDimension(widthMeasureSpec, heightMeasureSpec)
            return
        }
        if (firstMeasuredWidth == 0) {
            firstMeasuredWidth = measuredWidth
        }

        // there are three spaces to consider
        // the margin before the folded preview, the margin in between the folded and unfolded and
        // the margin after the unfolded view
        val spaceBetweenPreviews =
            resources.getDimension(R.dimen.foldable_small_preview_space_between_preview)

        val ratio = 1.0 - SCREEN_WIDTH_RATIO_FOR_NEXT_PAGE
        val singlePageWidth = (firstMeasuredWidth * ratio).toFloat()
        val parentWidth = singlePageWidth - spaceBetweenPreviews

        val smallDisplaySize = checkNotNull(getPreviewDisplaySize(DeviceDisplayType.FOLDED))
        val largeDisplaySize = checkNotNull(getPreviewDisplaySize(DeviceDisplayType.UNFOLDED))

        // calculate the aspect ratio (ar) of the folded display
        val smallDisplayAR = smallDisplaySize.x.toFloat() / smallDisplaySize.y

        // calculate the aspect ratio of the unfolded display
        val largeDisplayAR = largeDisplaySize.x.toFloat() / largeDisplaySize.y

        // Width based calculation
        var newHeight = parentWidth / (largeDisplayAR + smallDisplayAR)
        if (newHeight > measuredHeight) {
            // If new height derived from width is larger than original height, use height based
            // calculation.
            newHeight = measuredHeight.toFloat()
        }

        val widthFolded = newHeight * smallDisplayAR
        val widthUnfolded = newHeight * largeDisplayAR

        val foldedView = findViewById<View>(DeviceDisplayType.FOLDED.getViewId())
        foldedView?.measure(
            MeasureSpec.makeMeasureSpec(widthFolded.toInt(), MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(newHeight.toInt(), MeasureSpec.EXACTLY),
        )

        val unfoldedView = findViewById<View>(DeviceDisplayType.UNFOLDED.getViewId())
        unfoldedView?.measure(
            MeasureSpec.makeMeasureSpec(widthUnfolded.toInt(), MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(newHeight.toInt(), MeasureSpec.EXACTLY),
        )

        val marginPixels =
            context.resources.getDimension(R.dimen.small_preview_inter_preview_margin).toInt()

        setMeasuredDimension(
            MeasureSpec.makeMeasureSpec(
                (widthFolded + widthUnfolded + 2 * marginPixels).toInt(),
                MeasureSpec.EXACTLY,
            ),
            MeasureSpec.makeMeasureSpec(newHeight.toInt(), MeasureSpec.EXACTLY),
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        // margins
        val spaceBetweenPreviews =
            resources.getDimension(R.dimen.foldable_small_preview_space_between_preview).toInt()

        // the handheld preview will be position first
        val foldedView = getChildAt(0)
        val foldedViewWidth = foldedView.measuredWidth
        val foldedViewHeight = foldedView.measuredHeight
        foldedView.layout(0, 0, foldedViewWidth, foldedViewHeight)

        // the unfolded view will be position after
        val unfoldedView = getChildAt(1)
        val unfoldedViewWidth = unfoldedView.measuredWidth
        val unfoldedViewHeight = unfoldedView.measuredHeight
        unfoldedView.layout(
            foldedViewWidth + spaceBetweenPreviews,
            0,
            unfoldedViewWidth + foldedViewWidth + spaceBetweenPreviews,
            unfoldedViewHeight,
        )
    }

    fun setDisplaySizes(displaySizes: Map<DeviceDisplayType, Point>) {
        previewDisplaySizes = displaySizes
    }

    fun getPreviewDisplaySize(display: DeviceDisplayType): Point? {
        return previewDisplaySizes?.get(display)
    }

    companion object {
        /** Defines percentage of the screen width is used for showing part of the next page. */
        private const val SCREEN_WIDTH_RATIO_FOR_NEXT_PAGE = 0.1

        /** Defines children view ids for [DualDisplayAspectRatioLayout2]. */
        fun DeviceDisplayType.getViewId(): Int {
            return when (this) {
                DeviceDisplayType.SINGLE ->
                    throw IllegalStateException(
                        "DualDisplayAspectRatioLayout does not supper handheld DeviceDisplayType"
                    )
                DeviceDisplayType.FOLDED -> R.id.small_preview_folded_preview
                DeviceDisplayType.UNFOLDED -> R.id.small_preview_unfolded_preview
            }
        }
    }
}

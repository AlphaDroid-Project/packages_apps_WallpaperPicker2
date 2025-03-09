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

package com.android.wallpaper.picker.option.ui.view

import android.animation.ArgbEvaluator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.android.wallpaper.R

open class OptionItemBackground
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr) {

    private val colorUnselected =
        context.resources.getColor(R.color.system_surface_container_high, null)
    private val colorSelected = context.resources.getColor(R.color.system_primary, null)
    private val argbEvaluator = ArgbEvaluator()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    // progress 0 is unselected and 1 is selected
    var progress = 0f
        private set

    fun setProgress(progress: Float) {
        this.progress = progress
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val cornerRadius = (width / 2) * (1f - 0.25f * progress)
        paint.color = argbEvaluator.evaluate(progress, colorUnselected, colorSelected) as Int

        canvas.drawRoundRect(0f, 0f, width, height, cornerRadius, cornerRadius, paint)
    }
}

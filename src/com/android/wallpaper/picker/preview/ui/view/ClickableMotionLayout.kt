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
import android.graphics.Rect
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.ViewParent
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.ancestors
import androidx.core.view.children

/** A [MotionLayout] that performs click on one of its child if it is the recipient. */
class ClickableMotionLayout(context: Context, attrs: AttributeSet?) : MotionLayout(context, attrs) {

    /** True for this view to intercept all motion events. */
    var shouldInterceptTouch = true

    private val clickableViewIds = mutableListOf<Int>()
    private val singleTapDetector =
        GestureDetector(
            context,
            object : SimpleOnGestureListener() {
                override fun onSingleTapUp(event: MotionEvent): Boolean {
                    // Check if any immediate child view is clicked
                    children
                        .find {
                            isEventPointerInRect(event, Rect(it.left, it.top, it.right, it.bottom))
                        }
                        ?.let { child ->
                            // Find all the clickable ids in the hierarchy of the clicked view and
                            // perform click on the exact view that should be clicked.
                            clickableViewIds
                                .mapNotNull { child.findViewById(it) }
                                .find { clickableView ->
                                    if (clickableView == child) {
                                        true
                                    } else {
                                        // Find ancestors of this clickable view up until this
                                        // layout and transform coordinates to align with motion
                                        // event.
                                        val ancestors = clickableView.ancestors
                                        var ancestorsLeft = 0
                                        var ancestorsTop = 0
                                        ancestors
                                            .filter {
                                                ancestors.indexOf(it) <=
                                                    ancestors.indexOf(child as ViewParent)
                                            }
                                            .forEach {
                                                it as ViewGroup
                                                ancestorsLeft += it.left
                                                ancestorsTop += it.top
                                            }
                                        isEventPointerInRect(
                                            event,
                                            Rect(
                                                /* left= */ ancestorsLeft + clickableView.left,
                                                /* top= */ ancestorsTop + clickableView.top,
                                                /* right= */ ancestorsLeft + clickableView.right,
                                                /* bottom= */ ancestorsTop + clickableView.bottom,
                                            ),
                                        )
                                    }
                                }
                                ?.performClick()
                        }

                    return true
                }
            },
        )

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        // MotionEvent.ACTION_DOWN is the first MotionEvent received and is necessary to detect
        // various gesture, returns true to intercept all event so they are forwarded into
        // onTouchEvent.
        return shouldInterceptTouch
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)

        // Handle single tap
        singleTapDetector.onTouchEvent(event)

        return true
    }

    fun setClickableViewIds(ids: List<Int>) {
        clickableViewIds.apply {
            clear()
            addAll(ids)
        }
    }

    private fun isEventPointerInRect(e: MotionEvent, rect: Rect): Boolean {
        return e.x >= rect.left && e.x <= rect.right && e.y >= rect.top && e.y <= rect.bottom
    }
}

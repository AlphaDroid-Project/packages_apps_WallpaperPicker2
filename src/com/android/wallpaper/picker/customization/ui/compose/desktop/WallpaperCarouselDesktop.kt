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

package com.android.wallpaper.picker.customization.ui.compose.desktop

import android.content.Context
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.widget.ImageView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.android.wallpaper.R
import com.android.wallpaper.module.logging.UserEventLogger
import com.android.wallpaper.picker.category.ui.viewmodel.TileViewModel
import com.android.wallpaper.util.CuratedPhotosTimeUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

@Composable
fun WallpaperCarouselDesktop(
    items: List<TileViewModel>,
    curatedPhotosTimeUtil: CuratedPhotosTimeUtil,
    userEventLogger: UserEventLogger,
    modifier: Modifier = Modifier,
) {
    val itemWidthThreshold = 80.dp
    val itemSpacing = dimensionResource(id = R.dimen.curated_photo_horizontal_margin)
    val minItems = integerResource(id = R.integer.suggested_wallpapers_desktop_min_column_count)
    val maxItems = integerResource(id = R.integer.suggested_wallpapers_desktop_max_column_count)

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val containerWidth = maxWidth
        val itemsCount =
            calculateNumberOfItems(
                containerWidth,
                itemWidthThreshold,
                itemSpacing,
                minItems,
                maxItems,
            )

        LazyRow(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(itemSpacing),
        ) {
            itemsIndexed(items.take(itemsCount)) { index, item ->
                val itemWidth =
                    calculateCarouselItemWidth(
                        index,
                        containerWidth,
                        itemSpacing,
                        itemsCount,
                        itemWidthThreshold,
                        minItems,
                        maxItems,
                    )
                WallpaperItem(
                    item = item,
                    curatedPhotosTimeUtil = curatedPhotosTimeUtil,
                    userEventLogger = userEventLogger,
                    modifier = Modifier.width(itemWidth),
                    onClick = { item.onClicked?.invoke() },
                )
            }
        }
    }
}

@Composable
fun WallpaperItem(
    item: TileViewModel,
    curatedPhotosTimeUtil: CuratedPhotosTimeUtil,
    userEventLogger: UserEventLogger,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val cornerRadius = remember { getDialogCornerRadius(context) }

    Card(
        modifier =
            modifier
                .height(dimensionResource(id = R.dimen.curated_photo_desktop_height))
                .clip(RoundedCornerShape(cornerRadius)),
        shape = RoundedCornerShape(cornerRadius),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        AndroidView(
            factory = {
                ImageView(it).apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    contentDescription = item.contentDescription
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { imageView ->
                item.thumbnailAsset?.let { asset ->
                    asset.loadDrawableWithTransition(
                        context,
                        imageView,
                        context.resources.getInteger(android.R.integer.config_mediumAnimTime),
                        {
                            val startTime = curatedPhotosTimeUtil.getStartTime()
                            val timeMilliseconds = System.currentTimeMillis() - startTime
                            userEventLogger.logCuratedPhotosRendered(timeMilliseconds, true)
                        },
                        context.getColor(R.color.system_surface_bright),
                    )
                }
                    ?: run {
                        Glide.with(context)
                            .load(item.defaultDrawable)
                            .addListener(
                                object : RequestListener<Drawable> {
                                    override fun onResourceReady(
                                        resource: Drawable,
                                        model: Any,
                                        target: Target<Drawable>,
                                        dataSource: DataSource,
                                        isFirstResource: Boolean,
                                    ): Boolean {
                                        if (resource is AnimatedImageDrawable) {
                                            resource.repeatCount = 0
                                            resource.start()
                                        }
                                        val startTime = curatedPhotosTimeUtil.getStartTime()
                                        val timeMilliseconds =
                                            System.currentTimeMillis() - startTime
                                        userEventLogger.logCuratedPhotosRendered(
                                            timeMilliseconds,
                                            false,
                                        )
                                        return false
                                    }

                                    override fun onLoadFailed(
                                        e: GlideException?,
                                        model: Any?,
                                        target: Target<Drawable>,
                                        isFirstResource: Boolean,
                                    ): Boolean {
                                        return false
                                    }
                                }
                            )
                            .into(imageView)
                    }
            },
        )
    }
}

private fun calculateNumberOfItems(
    containerWidth: Dp,
    itemWidthThreshold: Dp,
    itemSpacing: Dp,
    minItems: Int,
    maxItems: Int,
): Int {
    return when {
        (itemWidthThreshold * maxItems + itemSpacing * (maxItems - 1)) <= containerWidth -> maxItems
        else -> minItems
    }
}

private fun calculateCarouselItemWidth(
    index: Int,
    containerWidth: Dp,
    itemSpacing: Dp,
    itemsCount: Int,
    itemWidthThreshold: Dp,
    minItems: Int,
    maxItems: Int,
): Dp {
    val totalSpacing = itemSpacing * (itemsCount - 1)
    val remainingContainerWidth = containerWidth - totalSpacing
    return if (itemsCount == maxItems || itemWidthThreshold * minItems <= remainingContainerWidth) {
        remainingContainerWidth / itemsCount
    } else {
        // Show `minItems` on the carousel but the first item have the width value double than
        // others.
        val unitWidth = remainingContainerWidth / (minItems + 1)
        return if (index == 0) {
            unitWidth * 2
        } else {
            unitWidth
        }
    }
}

private fun getDialogCornerRadius(context: Context): Dp {
    val typedValue = TypedValue()
    context.theme.resolveAttribute(android.R.attr.dialogCornerRadius, typedValue, true)
    val radiusInPx = typedValue.getDimension(context.resources.displayMetrics)
    val radiusInDp = radiusInPx / context.resources.displayMetrics.density
    return radiusInDp.dp
}

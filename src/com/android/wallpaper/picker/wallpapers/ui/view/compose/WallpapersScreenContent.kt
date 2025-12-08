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

package com.android.wallpaper.picker.wallpapers.ui.view.compose

import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.android.compose.modifiers.width
import com.android.compose.theme.PlatformTheme
import com.android.compose.ui.graphics.painter.rememberDrawablePainter
import com.android.wallpaper.R
import com.android.wallpaper.picker.wallpapers.ui.view.viewmodel.CategoryWallpapersContentViewModel
import com.android.wallpaper.picker.wallpapers.ui.view.viewmodel.CategoryWallpapersItemViewModel
import com.android.wallpaper.util.ResourceUtils
import com.android.wallpaper.util.SizeCalculator

/** Scale factor used to calculate the height of template tiles */
private const val THUMBNAIL_TILE_HEIGHT_SCALE_FACTOR: Float = 1.2f

/**
 * Displays the main screen content for category wallpapers using a [LazyColumn].
 *
 * @param viewModel The [CategoryWallpapersContentViewModel] providing the wallpaper items to
 *   render.
 */
@Composable
fun WallpapersScreenContent(
    viewModel: CategoryWallpapersContentViewModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
    ) {
        TopToolbar(viewModel.title, modifier = modifier)
        LazyColumn(modifier = modifier.fillMaxSize()) {
            items(viewModel.wallpaperItems) { item ->
                when (item) {
                    is CategoryWallpapersItemViewModel.PrimaryHeaderViewModelCategory -> {
                        SectionLabel(item.title, modifier.wrapContentSize().padding(start = 18.dp))
                    }

                    is CategoryWallpapersItemViewModel.SecondaryHeaderViewModelCategory -> {
                        SectionLabel(item.title, modifier.wrapContentSize().padding(start = 20.dp))
                    }

                    is CategoryWallpapersItemViewModel.TemplateThumbnailsViewModelCategory -> {
                        HorizontalGridSection(thumbnails = item.thumbnailAssets, maxRows = 2)
                    }

                    is CategoryWallpapersItemViewModel.ThumbnailsViewModelCategory -> {
                        ThumbnailCard(item)
                    }

                    is CategoryWallpapersItemViewModel.PlainThumbnailsViewModelCategory -> {
                        ThumbnailGridSection(thumbnails = item.thumbnailAssets, columns = 3)
                    }
                }
            }
        }
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier) {
    PlatformTheme {
        val colorScheme = MaterialTheme.colorScheme
        Text(
            text = text,
            modifier = modifier,
            fontSize = 16.sp,
            lineHeight = 15.sp,
            color = colorScheme.onSurface,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.W500,
        )
    }
}

@Composable
fun TopToolbar(title: String, modifier: Modifier) {
    val activity = LocalActivity.current as ComponentActivity

    PlatformTheme {
        val colorScheme = MaterialTheme.colorScheme
        Row(
            modifier = modifier.padding(vertical = 8.dp, horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Box(
                modifier =
                    Modifier.clickable { /* Handle back */ }
                        .padding(vertical = 4.dp)
                        .padding(end = 16.dp)
            ) {
                IconButton(
                    modifier = Modifier.size(40.dp),
                    onClick = { activity.onBackPressedDispatcher.onBackPressed() },
                    colors =
                        IconButtonDefaults.iconButtonColors()
                            .copy(containerColor = colorScheme.surfaceContainerHighest),
                    shape = CircleShape,
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_nav_back_24dp),
                        contentDescription = stringResource(R.string.bottom_action_bar_back),
                        tint = colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                modifier = Modifier.weight(1f),
                text = title,
                fontSize = 20.sp,
                color = colorScheme.onSurface,
            )
        }
    }
}

/**
 * Displays a horizontal scrolling grid of wallpaper thumbnails with a fixed number of rows.
 *
 * This grid is intended to be used inside a [LazyColumn], and hence requires an explicitly defined
 * height based on tile size.
 *
 * @param thumbnails List of thumbnail view models to display.
 * @param maxRows Maximum number of rows in the horizontal grid.
 */
@Composable
fun HorizontalGridSection(
    thumbnails: List<CategoryWallpapersItemViewModel.ThumbnailsViewModelCategory>,
    maxRows: Int,
    modifier: Modifier = Modifier,
) {
    val density: Density = LocalDensity.current
    val tileHeightPx: Float =
        LocalActivity.current?.let { SizeCalculator.getFeaturedIndividualTileSize(it).y.toFloat() }
            ?: with(density) { 260.dp.toPx() }

    val heightDp =
        with(density) { (THUMBNAIL_TILE_HEIGHT_SCALE_FACTOR * tileHeightPx).toInt().toDp() }
    val tileSize = getTileSizeAsDp()
    LazyHorizontalGrid(
        rows = GridCells.Fixed(maxRows),
        modifier =
            modifier
                .fillMaxWidth()
                .height(
                    heightDp * 2
                ) // need to set a fixed height for this grid as its inside of a LazyColumn
                .padding(
                    vertical =
                        dimensionResource(R.dimen.creative_category_individual_item_view_space)
                ),
        horizontalArrangement =
            Arrangement.spacedBy(
                dimensionResource(R.dimen.creative_category_individual_item_view_space)
            ),
        verticalArrangement =
            Arrangement.spacedBy(
                dimensionResource(R.dimen.creative_category_individual_item_view_space)
            ),
        contentPadding =
            PaddingValues(
                horizontal = dimensionResource(R.dimen.featured_wallpaper_grid_edge_space)
            ),
    ) {
        items(thumbnails) { thumbnail ->
            ThumbnailCard(
                thumbnail,
                modifier.size(width = tileSize.width, height = heightDp),
                showLabel = true,
            )
        }
    }
}

/**
 * Displays a grid of thumbnails arranged in a fixed number of columns.
 *
 * @param thumbnails List of thumbnail view models to display.
 * @param columns Number of columns in the grid.
 */
@Composable
fun ThumbnailGridSection(
    thumbnails: List<CategoryWallpapersItemViewModel.ThumbnailsViewModelCategory>,
    columns: Int,
    modifier: Modifier = Modifier,
) {
    val rows = (thumbnails.size + columns - 1) / columns
    val tileSize = getTileSizeAsDp()

    Column(
        modifier = modifier.fillMaxWidth().padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (rowIndex in 0 until rows) {
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                for (colIndex in 0 until columns) {
                    val itemIndex = rowIndex * columns + colIndex
                    if (itemIndex < thumbnails.size) {
                        ThumbnailCard(
                            thumbnails[itemIndex],
                            modifier.size(width = tileSize.width, height = tileSize.height),
                        )
                    } else {
                        // Add an empty space to keep the columns aligned
                        Spacer(
                            modifier =
                                modifier.size(width = tileSize.width, height = tileSize.height)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun getTileSizeAsDp(): DpSize {
    val activity = LocalActivity.current
    val density = LocalDensity.current

    val tileSize = activity?.let { SizeCalculator.getIndividualTileSize(it) }

    val widthInDp = with(density) { (tileSize?.x ?: 0).toDp() }
    val heightInDp = with(density) { (tileSize?.y ?: 0).toDp() }

    return DpSize(width = widthInDp, height = heightInDp)
}

/**
 * Renders a single wallpaper thumbnail card with a title and click interaction. This will
 * eventually support an image also
 *
 * @param thumbnail The thumbnail view model containing metadata and click behavior.
 */
@Composable
fun ThumbnailCard(
    thumbnail: CategoryWallpapersItemViewModel.ThumbnailsViewModelCategory,
    modifier: Modifier = Modifier,
    showLabel: Boolean = false,
) {
    Card(
        modifier = modifier.clickable { thumbnail.onSectionClicked?.invoke() },
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.grid_item_all_radius_small)),
        elevation = CardDefaults.cardElevation(),
    ) {
        Box(modifier = modifier.fillMaxSize()) {
            AssetImageView(thumbnail = thumbnail, modifier = Modifier.fillMaxSize())
            if (showLabel) {
                Text(
                    text = thumbnail.title ?: stringResource(R.string.default_wallpaper_title),
                    modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                    color = Color.White,
                )
            }
            if (thumbnail.isDownloadable) {
                LayerListImage(
                    id = R.drawable.ic_download_badge,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                )
            } else if (thumbnail.isApplied) {
                LayerListImage(
                    id = R.drawable.wallpaper_check_circle_24dp,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                )
            }
        }
    }
}

@Composable
fun LayerListImage(
    @DrawableRes id: Int,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val context = LocalContext.current
    val drawable = AppCompatResources.getDrawable(context, id)

    drawable?.let {
        Image(
            painter = rememberDrawablePainter(drawable = it),
            contentDescription = contentDescription,
            modifier = modifier,
        )
    }
}

// TODO(b/441293395): Deprecate Asset class and migrate image loading to use GlideImageView
@Composable
fun AssetImageView(
    thumbnail: CategoryWallpapersItemViewModel.ThumbnailsViewModelCategory,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    AndroidView(
        factory = { context ->
            ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                layoutParams =
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
            }
        },
        update = { imageView ->
            thumbnail.thumbnailAsset.loadDrawable(
                context,
                imageView,
                ResourceUtils.getColorAttr(context, android.R.attr.colorSecondary),
            )
        },
        modifier =
            modifier.clickable {
                val intent = thumbnail.onSectionClicked?.invoke()
                context.startActivity(intent)
            },
    )
}

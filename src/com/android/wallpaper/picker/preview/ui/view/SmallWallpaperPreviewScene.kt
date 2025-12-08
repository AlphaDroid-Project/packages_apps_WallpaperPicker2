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

package com.android.wallpaper.picker.preview.ui.view

import android.view.View
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.compose.animation.scene.ContentScope
import com.android.compose.animation.scene.MutableSceneTransitionLayoutState
import com.android.wallpaper.R
import com.android.wallpaper.picker.preview.ui.fragment.WallpaperPreviewFragment
import com.android.wallpaper.picker.preview.ui.fragment.WallpaperPreviewFragment.Elements
import com.android.wallpaper.picker.preview.ui.fragment.WallpaperPreviewFragment.Scenes
import com.android.wallpaper.picker.preview.ui.fragment.WallpaperPreviewFragment.SharedElements
import kotlinx.coroutines.CoroutineScope

/**
 * [SmallWallpaperPreviewScene] is one scene in [WallpaperPreviewFragment]'s SceneTransitionLayout.
 * It is bound to the [WallpaperPreviewFragment] and is not expected to be used somewhere else.
 */
@Composable
fun ContentScope.SmallWallpaperPreviewScene(
    sceneState: MutableSceneTransitionLayoutState,
    pagerState: PagerState,
    lockScreenPreview: View,
    homeScreenPreview: View,
) {
    val colorScheme: ColorScheme = MaterialTheme.colorScheme
    val systemBarPadding: PaddingValues = WindowInsets.systemBars.asPaddingValues()

    Column {
        // Status bar space to avoid the top toolbar overlapping with the status bar.
        Spacer(modifier = Modifier.height(systemBarPadding.calculateTopPadding()))

        TopToolbar(
            modifier = Modifier.element(Elements.SmallPreviewTopToolbar).fillMaxWidth(),
            sceneState = sceneState,
        )

        Spacer(modifier = Modifier.height(12.dp))

        PreviewPager(
            modifier = Modifier.fillMaxWidth().weight(1f),
            sceneState = sceneState,
            pagerState = pagerState,
            lockScreenPreview = lockScreenPreview,
            homeScreenPreview = homeScreenPreview,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier =
                Modifier.element(Elements.SmallPreviewBottomActionBar)
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier.clickable { /* Handle back */ }
                        .padding(vertical = 4.dp)
                        .padding(end = 16.dp)
            ) {
                IconButton(
                    modifier = Modifier.size(56.dp),
                    onClick = {},
                    colors =
                        IconButtonDefaults.iconButtonColors()
                            .copy(containerColor = colorScheme.surfaceContainerHighest),
                    shape = CircleShape,
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_info_filled),
                        contentDescription = stringResource(R.string.tab_info),
                        tint = colorScheme.primary,
                    )
                }
            }
        }

        // Bottom handle bar space to avoid the bottom icon overlapping with the handle bar.
        Spacer(modifier = Modifier.height(systemBarPadding.calculateBottomPadding()))
    }
}

@Composable
private fun ContentScope.PreviewPager(
    sceneState: MutableSceneTransitionLayoutState,
    pagerState: PagerState,
    lockScreenPreview: View,
    homeScreenPreview: View,
    modifier: Modifier = Modifier,
) {
    val phoneAspectRatio: Float =
        LocalWindowInfo.current.containerSize.width.toFloat() /
            LocalWindowInfo.current.containerSize.height.toFloat()
    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    BoxWithConstraints(modifier) {
        val minHorizontalPadding: Dp = 48.dp
        val minVerticalPadding: Dp = 8.dp
        val availableWidth: Dp = maxWidth - minHorizontalPadding * 2
        val availableHeight: Dp = maxHeight - minVerticalPadding * 2
        val pagerAspectRatio: Float = availableWidth / availableHeight
        val isPreviewFillMaxHeight: Boolean = pagerAspectRatio > phoneAspectRatio
        val pageWidth: Dp =
            if (isPreviewFillMaxHeight) availableHeight * phoneAspectRatio else availableWidth
        val pageHeight: Dp =
            if (isPreviewFillMaxHeight) availableHeight else availableWidth / phoneAspectRatio
        // Use content padding to center the selected page horizontally and vertically
        val contentPaddingHorizontal: Dp = (maxWidth - pageWidth) / 2
        val contentPaddingVertical: Dp = (maxHeight - pageHeight) / 2

        HorizontalPager(
            pagerState,
            modifier = Modifier.fillMaxSize(),
            contentPadding =
                PaddingValues(
                    horizontal = contentPaddingHorizontal,
                    vertical = contentPaddingVertical,
                ),
            pageSpacing = 12.dp,
        ) { page ->
            val isCurrentPage = pagerState.currentPage == page
            when (page) {
                0 ->
                    MovableElement(
                        key = SharedElements.LockScreen,
                        Modifier.size(pageWidth, pageHeight),
                    ) {
                        content {
                            PreviewScreen(
                                preview = lockScreenPreview,
                                modifier =
                                    Modifier.fillMaxSize().clickable(
                                        enabled =
                                            isCurrentPage &&
                                                sceneState.currentScene == Scenes.SmallPreview
                                    ) {
                                        sceneState.setTargetScene(
                                            Scenes.FullLockPreview,
                                            animationScope = coroutineScope,
                                        )
                                    },
                            )
                        }
                    }

                1 ->
                    MovableElement(
                        key = SharedElements.HomeScreen,
                        Modifier.size(pageWidth, pageHeight),
                    ) {
                        content {
                            PreviewScreen(
                                preview = homeScreenPreview,
                                modifier =
                                    Modifier.fillMaxSize().clickable(
                                        enabled =
                                            isCurrentPage &&
                                                sceneState.currentScene == Scenes.SmallPreview
                                    ) {
                                        sceneState.setTargetScene(
                                            Scenes.FullHomePreview,
                                            animationScope = coroutineScope,
                                        )
                                    },
                            )
                        }
                    }
            }
        }
    }
}

@Composable
fun TopToolbar(modifier: Modifier, sceneState: MutableSceneTransitionLayoutState) {
    val colorScheme = MaterialTheme.colorScheme
    val coroutineScope = rememberCoroutineScope()

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
                onClick = {},
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
            text = stringResource(R.string.preview),
            fontSize = 20.sp,
            color = colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.width(12.dp))

        Box(modifier = Modifier.padding(vertical = 4.dp)) {
            Button(
                modifier = Modifier.height(40.dp),
                onClick = {
                    sceneState.setTargetScene(
                        Scenes.ApplyWallpaper,
                        animationScope = coroutineScope,
                    )
                },
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary,
                    ),
                shape = RoundedCornerShape(percent = 50),
            ) {
                Text(text = stringResource(R.string.next_page_content_description))
            }
        }
    }
}

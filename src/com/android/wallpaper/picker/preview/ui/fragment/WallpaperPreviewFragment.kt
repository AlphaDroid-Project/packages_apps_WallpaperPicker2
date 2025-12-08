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
package com.android.wallpaper.picker.preview.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.View.OnAttachStateChangeListener
import android.view.ViewGroup
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.IntSize
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.android.compose.animation.scene.Back
import com.android.compose.animation.scene.ContentScope
import com.android.compose.animation.scene.DefaultElementContentPicker
import com.android.compose.animation.scene.ElementKey
import com.android.compose.animation.scene.MovableElementKey
import com.android.compose.animation.scene.SceneKey
import com.android.compose.animation.scene.SceneTransitionLayout
import com.android.compose.animation.scene.SceneTransitions
import com.android.compose.animation.scene.rememberMutableSceneTransitionLayoutState
import com.android.compose.animation.scene.transitions
import com.android.compose.theme.PlatformTheme
import com.android.wallpaper.model.Screen
import com.android.wallpaper.model.wallpaper.DeviceDisplayType
import com.android.wallpaper.picker.AppbarFragment
import com.android.wallpaper.picker.common.preview.ui.binder.PreviewBinder
import com.android.wallpaper.picker.preview.ui.view.ApplyWallpaperScene
import com.android.wallpaper.picker.preview.ui.view.PreviewScreen
import com.android.wallpaper.picker.preview.ui.view.SmallWallpaperPreviewScene
import com.android.wallpaper.picker.preview.ui.viewmodel.WallpaperPreviewViewModel
import com.android.wallpaper.util.DisplayUtils
import com.android.wallpaper.util.wallpaperconnection.LiveWallpaperConnectionUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * This fragment displays the preview of the selected wallpaper on all available workspaces and
 * device displays.
 */
@AndroidEntryPoint(AppbarFragment::class)
class WallpaperPreviewFragment : Hilt_WallpaperPreviewFragment() {

    object Scenes {
        val SmallPreview = SceneKey(debugName = "SmallPreviewScene")
        val FullLockPreview = SceneKey(debugName = "FullLockPreviewScene")
        val FullHomePreview = SceneKey(debugName = "FullHomePreviewScene")
        val ApplyWallpaper = SceneKey(debugName = "ApplyWallpaperScene")
    }

    object Elements {
        val SmallPreviewTopToolbar = ElementKey(debugName = "SmallPreviewTopToolbar")
        val SmallPreviewBottomActionBar = ElementKey(debugName = "SmallPreviewBottomActionBar")
        val ApplyWallpaperTitle = ElementKey(debugName = "ApplyWallpaperTitle")
        val ApplyWallpaperLockScreenCheckbox =
            ElementKey(debugName = "ApplyWallpaperLockScreenCheckbox")
        val ApplyWallpaperHomeScreenCheckbox =
            ElementKey(debugName = "ApplyWallpaperHomeScreenCheckbox")
        val ApplyWallpaperBottomButtons = ElementKey(debugName = "ApplyWallpaperBottomButtons")
    }

    object SharedElements {
        val LockScreen =
            MovableElementKey(
                debugName = "LockScreen",
                contentPicker =
                    DefaultElementContentPicker(
                        contents =
                            setOf(
                                Scenes.SmallPreview,
                                Scenes.FullLockPreview,
                                Scenes.FullHomePreview,
                                Scenes.ApplyWallpaper,
                            )
                    ),
            )
        val HomeScreen =
            MovableElementKey(
                debugName = "HomeScreen",
                contentPicker =
                    DefaultElementContentPicker(
                        contents =
                            setOf(
                                Scenes.SmallPreview,
                                Scenes.FullLockPreview,
                                Scenes.FullHomePreview,
                                Scenes.ApplyWallpaper,
                            )
                    ),
            )
    }

    @Inject lateinit var displayUtils: DisplayUtils
    @Inject lateinit var liveWallpaperConnectionUtils: LiveWallpaperConnectionUtils

    private val wallpaperPreviewViewModel by activityViewModels<WallpaperPreviewViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val lockScreenPreview =
            SurfaceView(context).also {
                // Hide surface view until the to-be-parented surface controls are ready. This makes
                // sure surfaceCreated is called and we can reparent the surface controls in the
                // callback.
                it.isVisible = false
            }
        val homeScreenPreview =
            SurfaceView(context).also {
                // Hide surface view until the to-be-parented surface controls are ready. This makes
                // sure surfaceCreated is called and we can reparent the surface controls in the
                // callback.
                it.isVisible = false
            }
        container?.addOnAttachStateChangeListener(
            object : OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(view: View) {
                    // We need to wait until the parent container view is attached to window to get
                    // the surface control's token as the host token for rendering external
                    // rendering.
                    // The host token is used by the external rendering to listen to its lifecycle,
                    // so that when the token is dead, the external rendering can release resources
                    // accordingly.
                    val hostToken = view.rootSurfaceControl?.inputTransferToken?.token ?: return
                    val applicationContext: Context = view.context.applicationContext
                    // Bind lock screen preview
                    // TODO(b/332742248): Handle foldable case for DeviceDisplayType.
                    PreviewBinder.bind(
                        preview = lockScreenPreview,
                        viewModel = wallpaperPreviewViewModel,
                        applicationContext = applicationContext,
                        viewLifecycleOwner = viewLifecycleOwner,
                        screen = Screen.LOCK_SCREEN,
                        displaySize = displayUtils.getRealSize(displayUtils.getWallpaperDisplay()),
                        deviceDisplayType = DeviceDisplayType.SINGLE,
                        display = requireActivity().display,
                        hostToken = hostToken,
                        windowToken = view.windowToken,
                        liveWallpaperConnectionUtils = liveWallpaperConnectionUtils,
                    )
                    // Bind home screen preview
                    PreviewBinder.bind(
                        preview = homeScreenPreview,
                        viewModel = wallpaperPreviewViewModel,
                        applicationContext = applicationContext,
                        viewLifecycleOwner = viewLifecycleOwner,
                        screen = Screen.HOME_SCREEN,
                        displaySize = displayUtils.getRealSize(displayUtils.getWallpaperDisplay()),
                        deviceDisplayType = DeviceDisplayType.SINGLE,
                        display = requireActivity().display,
                        hostToken = hostToken,
                        windowToken = view.windowToken,
                        liveWallpaperConnectionUtils = liveWallpaperConnectionUtils,
                    )
                }

                override fun onViewDetachedFromWindow(p0: View) {
                    // Do nothing intended
                }
            }
        )

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PlatformTheme {
                    WallpaperPreviewRootContent(
                        lockScreenPreview = lockScreenPreview,
                        homeScreenPreview = homeScreenPreview,
                    )
                }
            }
        }
    }

    @Composable
    fun WallpaperPreviewRootContent(
        lockScreenPreview: SurfaceView,
        homeScreenPreview: SurfaceView,
        modifier: Modifier = Modifier,
    ) {
        val sceneState =
            rememberMutableSceneTransitionLayoutState(
                initialScene = Scenes.SmallPreview,
                transitions = remember { sceneTransitions() },
            )
        // Pager state needs to be outside the scope of the SceneTransitionLayout so that after
        // transitioning back to the small preview scene, the selected page index can be retained.
        val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })

        SceneTransitionLayout(state = sceneState, modifier = modifier) {
            // The order of the scene here matters. During transitions the first defined scene will
            // be drawn below the second, scene, which will be drawn below the third one, etc.
            scene(Scenes.SmallPreview) {
                SmallWallpaperPreviewScene(
                    sceneState = sceneState,
                    pagerState = pagerState,
                    lockScreenPreview = lockScreenPreview,
                    homeScreenPreview = homeScreenPreview,
                )
            }
            scene(Scenes.ApplyWallpaper, userActions = mapOf(Back to Scenes.SmallPreview)) {
                ApplyWallpaperScene(
                    lockScreenPreview = lockScreenPreview,
                    homeScreenPreview = homeScreenPreview,
                )
            }
            scene(Scenes.FullLockPreview, userActions = mapOf(Back to Scenes.SmallPreview)) {
                FullPreviewScene(Screen.LOCK_SCREEN, lockScreenPreview)
            }
            scene(Scenes.FullHomePreview, userActions = mapOf(Back to Scenes.SmallPreview)) {
                FullPreviewScene(Screen.HOME_SCREEN, homeScreenPreview)
            }
        }
    }

    @Composable
    fun ContentScope.FullPreviewScene(screen: Screen, preview: View) {
        val windowSize: IntSize = LocalWindowInfo.current.containerSize
        val phoneAspectRatio: Float = windowSize.width.toFloat() / windowSize.height.toFloat()
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            MovableElement(
                key =
                    when (screen) {
                        Screen.LOCK_SCREEN -> SharedElements.LockScreen
                        Screen.HOME_SCREEN -> SharedElements.HomeScreen
                    },
                modifier = Modifier.fillMaxHeight().aspectRatio(phoneAspectRatio),
            ) {
                content { PreviewScreen(preview = preview, modifier = Modifier.fillMaxSize()) }
            }
        }
    }

    private fun sceneTransitions(): SceneTransitions {
        return transitions {
            from(Scenes.SmallPreview, to = Scenes.ApplyWallpaper) {
                spec = spring()
                fractionRange(end = 0.7f) { fade(Elements.SmallPreviewTopToolbar) }
                fractionRange(end = 0.7f) { fade(Elements.SmallPreviewBottomActionBar) }
                fractionRange(start = 0.7f) { fade(Elements.ApplyWallpaperTitle) }
                fractionRange(start = 0.7f) { fade(Elements.ApplyWallpaperLockScreenCheckbox) }
                fractionRange(start = 0.7f) { fade(Elements.ApplyWallpaperHomeScreenCheckbox) }
                fractionRange(start = 0.7f) { fade(Elements.ApplyWallpaperBottomButtons) }
            }
            from(Scenes.SmallPreview, to = Scenes.FullLockPreview) {
                spec = spring()
                fractionRange(end = 0.7f) { fade(Elements.SmallPreviewTopToolbar) }
                fractionRange(end = 0.7f) { fade(Elements.SmallPreviewBottomActionBar) }
                fractionRange(end = 0.7f) { fade(SharedElements.HomeScreen) }
            }
            from(Scenes.SmallPreview, to = Scenes.FullHomePreview) {
                spec = spring()
                fractionRange(end = 0.7f) { fade(Elements.SmallPreviewTopToolbar) }
                fractionRange(end = 0.7f) { fade(Elements.SmallPreviewBottomActionBar) }
                fractionRange(end = 0.7f) { fade(SharedElements.LockScreen) }
            }
        }
    }
}

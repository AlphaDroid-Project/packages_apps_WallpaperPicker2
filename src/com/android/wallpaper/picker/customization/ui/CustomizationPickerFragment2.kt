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

package com.android.wallpaper.picker.customization.ui

import android.annotation.TargetApi
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toolbar
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.MotionLayout.TransitionListener
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.android.customization.picker.clock.ui.view.ClockViewFactory
import com.android.wallpaper.R
import com.android.wallpaper.model.Screen
import com.android.wallpaper.model.Screen.HOME_SCREEN
import com.android.wallpaper.model.Screen.LOCK_SCREEN
import com.android.wallpaper.module.LargeScreenMultiPanesChecker
import com.android.wallpaper.picker.category.ui.view.CategoriesFragment
import com.android.wallpaper.picker.common.preview.data.repository.PersistentWallpaperModelRepository
import com.android.wallpaper.picker.common.preview.ui.binder.BasePreviewBinder
import com.android.wallpaper.picker.common.preview.ui.binder.WorkspaceCallbackBinder
import com.android.wallpaper.picker.customization.ui.binder.ColorUpdateBinder
import com.android.wallpaper.picker.customization.ui.binder.CustomizationOptionsBinder
import com.android.wallpaper.picker.customization.ui.binder.CustomizationPickerBinder2
import com.android.wallpaper.picker.customization.ui.binder.PagerTouchInterceptorBinder
import com.android.wallpaper.picker.customization.ui.binder.ToolbarBinder
import com.android.wallpaper.picker.customization.ui.util.CustomizationOptionUtil
import com.android.wallpaper.picker.customization.ui.util.CustomizationOptionUtil.CustomizationOption
import com.android.wallpaper.picker.customization.ui.view.adapter.PreviewPagerAdapter
import com.android.wallpaper.picker.customization.ui.view.transformer.PreviewPagerPageTransformer
import com.android.wallpaper.picker.customization.ui.viewmodel.ColorUpdateViewModel
import com.android.wallpaper.picker.customization.ui.viewmodel.CustomizationPickerViewModel2
import com.android.wallpaper.picker.di.modules.MainDispatcher
import com.android.wallpaper.picker.preview.ui.WallpaperPreviewActivity
import com.android.wallpaper.util.ActivityUtils
import com.android.wallpaper.util.DisplayUtils
import com.android.wallpaper.util.WallpaperConnection
import com.android.wallpaper.util.wallpaperconnection.WallpaperConnectionUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@AndroidEntryPoint(Fragment::class)
class CustomizationPickerFragment2 : Hilt_CustomizationPickerFragment2() {

    @Inject lateinit var customizationOptionUtil: CustomizationOptionUtil
    @Inject lateinit var customizationOptionsBinder: CustomizationOptionsBinder
    @Inject lateinit var toolbarBinder: ToolbarBinder
    @Inject lateinit var colorUpdateViewModel: ColorUpdateViewModel
    @Inject lateinit var clockViewFactory: ClockViewFactory
    @Inject lateinit var workspaceCallbackBinder: WorkspaceCallbackBinder
    @Inject lateinit var displayUtils: DisplayUtils
    @Inject lateinit var wallpaperConnectionUtils: WallpaperConnectionUtils
    @Inject lateinit var persistentWallpaperModelRepository: PersistentWallpaperModelRepository
    @Inject @MainDispatcher lateinit var mainScope: CoroutineScope

    private val customizationPickerViewModel: CustomizationPickerViewModel2 by viewModels()

    private var fullyCollapsed = false
    private var navBarHeight: Int = 0
    private var configuration: Configuration? = null

    private var onBackPressedCallback: OnBackPressedCallback? = null

    private var customizationOptionFloatingSheetViewMap: Map<CustomizationOption, View>? = null

    private val startForResult =
        this.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        configuration = Configuration(resources.configuration)

        val isFromLauncher =
            activity?.intent?.let { ActivityUtils.isLaunchedFromLauncher(it) } ?: false
        if (isFromLauncher) {
            customizationPickerViewModel.selectPreviewScreen(HOME_SCREEN)
        }

        val view = inflater.inflate(R.layout.fragment_customization_picker2, container, false)

        setupToolbar(
            view.requireViewById(R.id.nav_button),
            view.requireViewById(R.id.toolbar),
            view.requireViewById(R.id.apply_button),
        )

        val rootView = view.requireViewById<View>(R.id.root_view)
        ColorUpdateBinder.bind(
            setColor = { color -> rootView.setBackgroundColor(color) },
            color = colorUpdateViewModel.colorSurfaceContainer,
            shouldAnimate = { true },
            lifecycleOwner = viewLifecycleOwner,
        )

        val pickerMotionContainer = view.requireViewById<MotionLayout>(R.id.picker_motion_layout)
        ViewCompat.setOnApplyWindowInsetsListener(pickerMotionContainer) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            navBarHeight = insets.bottom
            view
                .requireViewById<FrameLayout>(R.id.customization_option_floating_sheet_container)
                .setPaddingRelative(0, 0, 0, navBarHeight)
            val statusBarHeight = insets.top
            val params =
                view.requireViewById<Toolbar>(R.id.toolbar).layoutParams as MarginLayoutParams
            params.setMargins(0, statusBarHeight, 0, 0)
            WindowInsetsCompat.CONSUMED
        }

        customizationOptionFloatingSheetViewMap =
            customizationOptionUtil.initFloatingSheet(
                pickerMotionContainer.requireViewById(
                    R.id.customization_option_floating_sheet_container
                ),
                layoutInflater,
            )

        pickerMotionContainer.setTransitionListener(
            object : EmptyTransitionListener {
                override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                    if (
                        currentId == R.id.expanded_header_primary ||
                            currentId == R.id.collapsed_header_primary
                    ) {
                        pickerMotionContainer.setTransition(R.id.transition_primary)
                    }
                }
            }
        )

        val previewViewModel = customizationPickerViewModel.basePreviewViewModel
        previewViewModel.setWhichPreview(WallpaperConnection.WhichPreview.EDIT_CURRENT)
        // TODO (b/348462236): adjust flow so this is always false when previewing current wallpaper
        previewViewModel.setIsWallpaperColorPreviewEnabled(false)

        initPreviewPager(
            view = view,
            isFirstBinding = savedInstanceState == null,
            initialScreen = if (isFromLauncher) HOME_SCREEN else LOCK_SCREEN,
        )

        val optionContainer =
            view.requireViewById<MotionLayout>(R.id.customization_option_container)
        // The collapsed header height should be updated when option container's height is known
        optionContainer.doOnPreDraw {
            // The bottom navigation bar height
            val collapsedHeaderHeight =
                pickerMotionContainer.height - optionContainer.height - navBarHeight
            if (
                collapsedHeaderHeight >
                    resources.getDimensionPixelSize(
                        R.dimen.customization_picker_preview_header_collapsed_height
                    )
            ) {
                pickerMotionContainer
                    .getConstraintSet(R.id.collapsed_header_primary)
                    ?.constrainHeight(R.id.preview_header, collapsedHeaderHeight)
                pickerMotionContainer.setTransition(R.id.transition_primary)
            }
        }

        CustomizationPickerBinder2.bind(
            view = pickerMotionContainer,
            lockScreenCustomizationOptionEntries =
                initCustomizationOptionEntries(view, LOCK_SCREEN),
            homeScreenCustomizationOptionEntries =
                initCustomizationOptionEntries(view, HOME_SCREEN),
            customizationOptionFloatingSheetViewMap = customizationOptionFloatingSheetViewMap,
            viewModel = customizationPickerViewModel,
            colorUpdateViewModel = colorUpdateViewModel,
            customizationOptionsBinder = customizationOptionsBinder,
            lifecycleOwner = this,
            navigateToPrimary = {
                if (pickerMotionContainer.currentState == R.id.secondary) {
                    pickerMotionContainer.transitionToState(
                        if (fullyCollapsed) R.id.collapsed_header_primary
                        else R.id.expanded_header_primary
                    )
                }
            },
            navigateToSecondary = { screen ->
                if (pickerMotionContainer.currentState != R.id.secondary) {
                    setCustomizationOptionFloatingSheet(view, pickerMotionContainer, screen) {
                        fullyCollapsed = pickerMotionContainer.progress == 1.0f
                        pickerMotionContainer.transitionToState(R.id.secondary)
                    }
                }
            },
            navigateToCategoriesScreen = { _ ->
                if (isAdded) {
                    parentFragmentManager.commit {
                        replace<CategoriesFragment>(R.id.fragment_container)
                        addToBackStack(null)
                    }
                }
            },
        )

        activity?.onBackPressedDispatcher?.let {
            it.addCallback {
                    isEnabled =
                        customizationPickerViewModel.customizationOptionsViewModel
                            .handleBackPressed()
                    if (!isEnabled) it.onBackPressed()
                }
                .also { callback -> onBackPressedCallback = callback }
        }

        return view
    }

    override fun onDestroyView() {
        context?.applicationContext?.let { appContext ->
            // TODO(b/333879532): Only disconnect when leaving the Activity without introducing
            // black
            //  preview. If onDestroy is caused by an orientation change, we should keep the
            // connection
            //  to avoid initiating the engines again.
            // TODO(b/328302105): MainScope ensures the job gets done non-blocking even if the
            //   activity has been destroyed already. Consider making this part of
            //   WallpaperConnectionUtils.
            mainScope.launch { wallpaperConnectionUtils.disconnectAll(appContext) }
        }

        super.onDestroyView()
        onBackPressedCallback?.remove()
    }

    @TargetApi(36)
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configuration?.let {
            val diff = newConfig.diff(it)
            val isAssetsPathsChange = diff and ActivityInfo.CONFIG_ASSETS_PATHS != 0
            if (isAssetsPathsChange) {
                colorUpdateViewModel.updateColors()
            }
        }
        configuration?.setTo(newConfig)
    }

    private fun setupToolbar(navButton: FrameLayout, toolbar: Toolbar, applyButton: Button) {
        toolbar.title = getString(R.string.app_name)
        toolbar.setBackgroundColor(Color.TRANSPARENT)
        toolbarBinder.bind(
            navButton,
            toolbar,
            applyButton,
            customizationPickerViewModel.customizationOptionsViewModel,
            colorUpdateViewModel,
            this,
        ) {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
    }

    private fun initPreviewPager(view: View, isFirstBinding: Boolean, initialScreen: Screen) {
        val appContext = context?.applicationContext ?: return
        val activity = activity ?: return

        PagerTouchInterceptorBinder.bind(
            view.requireViewById(R.id.pager_touch_interceptor),
            customizationPickerViewModel,
            viewLifecycleOwner,
        )

        val pager = view.requireViewById<ViewPager2>(R.id.preview_pager)
        val previewViewModel = customizationPickerViewModel.basePreviewViewModel
        pager.apply {
            adapter = PreviewPagerAdapter { viewHolder, position ->
                val previewCard = viewHolder.itemView.requireViewById<View>(R.id.preview_card)
                val screen =
                    if (position == 0) {
                        LOCK_SCREEN
                    } else {
                        HOME_SCREEN
                    }

                if (screen == LOCK_SCREEN) {
                    val clockHostView =
                        (previewCard.parent as? ViewGroup)?.let {
                            customizationOptionUtil.createClockPreviewAndAddToParent(
                                it,
                                layoutInflater,
                            )
                        }
                    if (clockHostView != null) {
                        customizationOptionsBinder.bindClockPreview(
                            context = context,
                            clockHostView = clockHostView,
                            viewModel = customizationPickerViewModel,
                            colorUpdateViewModel = colorUpdateViewModel,
                            lifecycleOwner = this@CustomizationPickerFragment2,
                            clockViewFactory = clockViewFactory,
                        )
                    }
                }

                BasePreviewBinder.bind(
                    applicationContext = appContext,
                    view = previewCard,
                    viewModel = customizationPickerViewModel,
                    colorUpdateViewModel = colorUpdateViewModel,
                    workspaceCallbackBinder = workspaceCallbackBinder,
                    screen = screen,
                    deviceDisplayType = displayUtils.getCurrentDisplayType(activity),
                    displaySize =
                        if (displayUtils.isOnWallpaperDisplay(activity))
                            previewViewModel.wallpaperDisplaySize.value
                        else previewViewModel.smallerDisplaySize,
                    mainScope = mainScope,
                    lifecycleOwner = this@CustomizationPickerFragment2,
                    wallpaperConnectionUtils = wallpaperConnectionUtils,
                    isFirstBindingDeferred = CompletableDeferred(isFirstBinding),
                    onLaunchPreview = { wallpaperModel ->
                        persistentWallpaperModelRepository.setWallpaperModel(wallpaperModel)
                        val multiPanesChecker = LargeScreenMultiPanesChecker()
                        val isMultiPanel = multiPanesChecker.isMultiPanesEnabled(appContext)
                        startForResult.launch(
                            WallpaperPreviewActivity.newIntent(
                                context = appContext,
                                isAssetIdPresent = false,
                                isViewAsHome = screen == HOME_SCREEN,
                                isNewTask = isMultiPanel,
                            )
                        )
                    },
                    clockViewFactory = clockViewFactory,
                )
            }
            setCurrentItem(
                when (initialScreen) {
                    LOCK_SCREEN -> 0
                    HOME_SCREEN -> 1
                },
                false,
            )
            // Disable over scroll
            (getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            // The neighboring view should be inflated when pager is rendered
            offscreenPageLimit = 1
            // When pager's height changes, request transform to recalculate the preview offset
            // to make sure correct space between the previews.
            // TODO (b/348462236): figure out how to scale surface view content with layout change
            addOnLayoutChangeListener { view, _, _, _, _, _, topWas, _, bottomWas ->
                val isHeightChanged = (bottomWas - topWas) != view.height
                if (isHeightChanged) {
                    pager.requestTransform()
                }
            }
        }

        // Only when pager is laid out, we can get the width and set the preview's offset correctly
        pager.doOnLayout {
            (it as ViewPager2).apply {
                setPageTransformer(PreviewPagerPageTransformer(Point(width, height)))
            }
        }
    }

    private fun initCustomizationOptionEntries(
        view: View,
        screen: Screen,
    ): List<Pair<CustomizationOption, View>> {
        val optionEntriesContainer =
            view.requireViewById<LinearLayout>(
                when (screen) {
                    LOCK_SCREEN -> R.id.lock_customization_option_container
                    HOME_SCREEN -> R.id.home_customization_option_container
                }
            )
        val optionEntries =
            customizationOptionUtil.getOptionEntries(screen, optionEntriesContainer, layoutInflater)
        optionEntries.onEachIndexed { index, (_, view) ->
            val isFirst = index == 0
            val isLast = index == optionEntries.size - 1
            view.setBackgroundResource(
                if (isFirst) R.drawable.customization_option_entry_top_background
                else if (isLast) R.drawable.customization_option_entry_bottom_background
                else R.drawable.customization_option_entry_background
            )
            optionEntriesContainer.addView(view)
        }
        return optionEntries
    }

    /**
     * Set customization option floating sheet to the floating sheet container and get the new
     * container's height for repositioning the preview's guideline.
     */
    private fun setCustomizationOptionFloatingSheet(
        view: View,
        motionContainer: MotionLayout,
        option: CustomizationOption,
        onComplete: () -> Unit,
    ) {
        val floatingSheetViewContent =
            customizationOptionFloatingSheetViewMap?.get(option) ?: return

        val floatingSheetContainer =
            view.requireViewById<FrameLayout>(R.id.customization_option_floating_sheet_container)
        floatingSheetContainer.removeAllViews()
        floatingSheetContainer.addView(floatingSheetViewContent)

        floatingSheetViewContent.doOnPreDraw {
            val height = floatingSheetViewContent.height + navBarHeight
            floatingSheetContainer.translationY = 0.0f
            floatingSheetContainer.alpha = 0.0f
            // Update the motion container
            motionContainer.getConstraintSet(R.id.expanded_header_primary)?.apply {
                setTranslationY(
                    R.id.customization_option_floating_sheet_container,
                    height.toFloat(),
                )
                setAlpha(R.id.customization_option_floating_sheet_container, 0.0f)
                connect(
                    R.id.customization_option_floating_sheet_container,
                    ConstraintSet.BOTTOM,
                    R.id.picker_motion_layout,
                    ConstraintSet.BOTTOM,
                )
                constrainHeight(
                    R.id.customization_option_floating_sheet_container,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                )
            }
            motionContainer.getConstraintSet(R.id.collapsed_header_primary)?.apply {
                setTranslationY(
                    R.id.customization_option_floating_sheet_container,
                    height.toFloat(),
                )
                setAlpha(R.id.customization_option_floating_sheet_container, 0.0f)
                connect(
                    R.id.customization_option_floating_sheet_container,
                    ConstraintSet.BOTTOM,
                    R.id.picker_motion_layout,
                    ConstraintSet.BOTTOM,
                )
                constrainHeight(
                    R.id.customization_option_floating_sheet_container,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                )
            }
            motionContainer.getConstraintSet(R.id.secondary)?.apply {
                setTranslationY(R.id.customization_option_floating_sheet_container, 0.0f)
                setAlpha(R.id.customization_option_floating_sheet_container, 1.0f)
                constrainHeight(
                    R.id.customization_option_floating_sheet_container,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                )
            }
            onComplete()
        }
    }

    interface EmptyTransitionListener : TransitionListener {
        override fun onTransitionStarted(motionLayout: MotionLayout?, startId: Int, endId: Int) {
            // Do nothing intended
        }

        override fun onTransitionChange(
            motionLayout: MotionLayout?,
            startId: Int,
            endId: Int,
            progress: Float,
        ) {
            // Do nothing intended
        }

        override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
            // Do nothing intended
        }

        override fun onTransitionTrigger(
            motionLayout: MotionLayout?,
            triggerId: Int,
            positive: Boolean,
            progress: Float,
        ) {
            // Do nothing intended
        }
    }
}

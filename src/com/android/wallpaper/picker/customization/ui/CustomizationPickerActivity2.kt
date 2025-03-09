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

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.android.customization.picker.clock.ui.view.ClockViewFactory
import com.android.wallpaper.R
import com.android.wallpaper.module.MultiPanesChecker
import com.android.wallpaper.picker.AppbarFragment
import com.android.wallpaper.picker.common.preview.data.repository.PersistentWallpaperModelRepository
import com.android.wallpaper.picker.common.preview.ui.binder.WorkspaceCallbackBinder
import com.android.wallpaper.picker.customization.ui.binder.CustomizationOptionsBinder
import com.android.wallpaper.picker.customization.ui.binder.ToolbarBinder
import com.android.wallpaper.picker.customization.ui.util.CustomizationOptionUtil
import com.android.wallpaper.picker.customization.ui.viewmodel.ColorUpdateViewModel
import com.android.wallpaper.picker.di.modules.BackgroundDispatcher
import com.android.wallpaper.picker.di.modules.MainDispatcher
import com.android.wallpaper.util.ActivityUtils
import com.android.wallpaper.util.DisplayUtils
import com.android.wallpaper.util.converter.WallpaperModelFactory
import com.android.wallpaper.util.wallpaperconnection.WallpaperConnectionUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope

@AndroidEntryPoint(AppCompatActivity::class)
class CustomizationPickerActivity2 :
    Hilt_CustomizationPickerActivity2(), AppbarFragment.AppbarFragmentHost {

    @Inject lateinit var multiPanesChecker: MultiPanesChecker
    @Inject lateinit var customizationOptionUtil: CustomizationOptionUtil
    @Inject lateinit var customizationOptionsBinder: CustomizationOptionsBinder
    @Inject lateinit var workspaceCallbackBinder: WorkspaceCallbackBinder
    @Inject lateinit var toolbarBinder: ToolbarBinder
    @Inject lateinit var wallpaperModelFactory: WallpaperModelFactory
    @Inject lateinit var persistentWallpaperModelRepository: PersistentWallpaperModelRepository
    @Inject lateinit var displayUtils: DisplayUtils
    @Inject @BackgroundDispatcher lateinit var backgroundScope: CoroutineScope
    @Inject @MainDispatcher lateinit var mainScope: CoroutineScope
    @Inject lateinit var wallpaperConnectionUtils: WallpaperConnectionUtils
    @Inject lateinit var colorUpdateViewModel: ColorUpdateViewModel
    @Inject lateinit var clockViewFactory: ClockViewFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (
            multiPanesChecker.isMultiPanesEnabled(this) &&
                !ActivityUtils.isLaunchedFromSettingsTrampoline(intent) &&
                !ActivityUtils.isLaunchedFromSettingsRelated(intent)
        ) {
            // If the device supports multi panes, we check if the activity is launched by settings.
            // If not, we need to start an intent to have settings launch the customization
            // activity. In case it is a two-pane situation and the activity should be embedded in
            // the settings app, instead of in the full screen.
            val multiPanesIntent = multiPanesChecker.getMultiPanesIntent(intent)
            ActivityUtils.startActivityForResultSafely(
                this, /* activity */
                multiPanesIntent,
                0, /* requestCode */
            )
            finish()
            return
        }

        setContentView(R.layout.activity_cusomization_picker2)
        WindowCompat.setDecorFitsSystemWindows(window, ActivityUtils.isSUWMode(this))

        val fragment = CustomizationPickerFragment2()
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, fragment).commit()
    }

    override fun onUpArrowPressed() {
        onBackPressedDispatcher.onBackPressed()
    }

    override fun isUpArrowSupported(): Boolean {
        return !ActivityUtils.isSUWMode(baseContext)
    }
}

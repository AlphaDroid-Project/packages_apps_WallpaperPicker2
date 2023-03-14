/*
 * Copyright (C) 2019 The Android Open Source Project
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
package com.android.wallpaper.testing

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.android.wallpaper.compat.WallpaperManagerCompat
import com.android.wallpaper.config.BaseFlags
import com.android.wallpaper.effects.EffectsController
import com.android.wallpaper.effects.EffectsController.EffectsServiceListener
import com.android.wallpaper.model.CategoryProvider
import com.android.wallpaper.model.WallpaperInfo
import com.android.wallpaper.module.AlarmManagerWrapper
import com.android.wallpaper.module.BitmapCropper
import com.android.wallpaper.module.CurrentWallpaperInfoFactory
import com.android.wallpaper.module.CustomizationSections
import com.android.wallpaper.module.DefaultLiveWallpaperInfoFactory
import com.android.wallpaper.module.DrawableLayerResolver
import com.android.wallpaper.module.ExploreIntentChecker
import com.android.wallpaper.module.Injector
import com.android.wallpaper.module.LiveWallpaperInfoFactory
import com.android.wallpaper.module.NetworkStatusNotifier
import com.android.wallpaper.module.PackageStatusNotifier
import com.android.wallpaper.module.PartnerProvider
import com.android.wallpaper.module.SystemFeatureChecker
import com.android.wallpaper.module.UserEventLogger
import com.android.wallpaper.module.WallpaperPersister
import com.android.wallpaper.module.WallpaperPreferences
import com.android.wallpaper.module.WallpaperPreviewFragmentManager
import com.android.wallpaper.module.WallpaperRefresher
import com.android.wallpaper.module.WallpaperRotationRefresher
import com.android.wallpaper.module.WallpaperStatusChecker
import com.android.wallpaper.monitor.PerformanceMonitor
import com.android.wallpaper.network.Requester
import com.android.wallpaper.picker.ImagePreviewFragment
import com.android.wallpaper.picker.PreviewFragment
import com.android.wallpaper.picker.individual.IndividualPickerFragment
import com.android.wallpaper.picker.undo.data.repository.UndoRepository
import com.android.wallpaper.picker.undo.domain.interactor.UndoInteractor
import com.android.wallpaper.util.DisplayUtils
import kotlinx.coroutines.GlobalScope

/** Test implementation of [Injector] */
open class TestInjector : Injector {
    private var alarmManagerWrapper: AlarmManagerWrapper? = null
    private var bitmapCropper: BitmapCropper? = null
    private var categoryProvider: CategoryProvider? = null
    private var currentWallpaperInfoFactory: CurrentWallpaperInfoFactory? = null
    private var customizationSections: CustomizationSections? = null
    private var drawableLayerResolver: DrawableLayerResolver? = null
    private var exploreIntentChecker: ExploreIntentChecker? = null
    private var networkStatusNotifier: NetworkStatusNotifier? = null
    private var packageStatusNotifier: PackageStatusNotifier? = null
    private var partnerProvider: PartnerProvider? = null
    private var performanceMonitor: PerformanceMonitor? = null
    private var requester: Requester? = null
    private var systemFeatureChecker: SystemFeatureChecker? = null
    private var userEventLogger: UserEventLogger? = null
    private var wallpaperManagerCompat: WallpaperManagerCompat? = null
    private var wallpaperPersister: WallpaperPersister? = null
    private var prefs: WallpaperPreferences? = null
    private var wallpaperPreviewFragmentManager: WallpaperPreviewFragmentManager? = null
    private var wallpaperRefresher: WallpaperRefresher? = null
    private var wallpaperRotationRefresher: WallpaperRotationRefresher? = null
    private var flags: BaseFlags? = null
    private var undoInteractor: UndoInteractor? = null

    override fun getAlarmManagerWrapper(context: Context): AlarmManagerWrapper {
        return alarmManagerWrapper ?: TestAlarmManagerWrapper().also { alarmManagerWrapper = it }
    }

    override fun getBitmapCropper(): BitmapCropper {
        return bitmapCropper ?: TestBitmapCropper().also { bitmapCropper = it }
    }

    override fun getCategoryProvider(context: Context): CategoryProvider {
        return categoryProvider ?: TestCategoryProvider().also { categoryProvider = it }
    }

    override fun getCurrentWallpaperInfoFactory(context: Context): CurrentWallpaperInfoFactory {
        return currentWallpaperInfoFactory
            ?: TestCurrentWallpaperInfoFactory(context.applicationContext).also {
                currentWallpaperInfoFactory = it
            }
    }

    override fun getCustomizationSections(activity: Activity): CustomizationSections {
        return customizationSections
            ?: TestCustomizationSections().also { customizationSections = it }
    }

    override fun getDeepLinkRedirectIntent(context: Context, uri: Uri): Intent {
        return Intent()
    }

    override fun getDisplayUtils(context: Context): DisplayUtils {
        return DisplayUtils(context)
    }

    override fun getDownloadableIntentAction(): String? {
        return null
    }

    override fun getDrawableLayerResolver(): DrawableLayerResolver {
        return drawableLayerResolver
            ?: TestDrawableLayerResolver().also { drawableLayerResolver = it }
    }

    override fun getEffectsController(
        context: Context,
        listener: EffectsServiceListener
    ): EffectsController? {
        return null
    }

    override fun getExploreIntentChecker(context: Context): ExploreIntentChecker {
        return exploreIntentChecker ?: TestExploreIntentChecker().also { exploreIntentChecker = it }
    }

    override fun getIndividualPickerFragment(collectionId: String): IndividualPickerFragment {
        return IndividualPickerFragment.newInstance(collectionId)
    }

    override fun getLiveWallpaperInfoFactory(context: Context): LiveWallpaperInfoFactory {
        return DefaultLiveWallpaperInfoFactory()
    }

    override fun getNetworkStatusNotifier(context: Context): NetworkStatusNotifier {
        return networkStatusNotifier
            ?: TestNetworkStatusNotifier().also { networkStatusNotifier = it }
    }

    override fun getPackageStatusNotifier(context: Context): PackageStatusNotifier {
        return packageStatusNotifier
            ?: TestPackageStatusNotifier().also { packageStatusNotifier = it }
    }

    override fun getPartnerProvider(context: Context): PartnerProvider {
        return partnerProvider ?: TestPartnerProvider().also { partnerProvider = it }
    }

    override fun getPerformanceMonitor(): PerformanceMonitor? {
        return performanceMonitor ?: TestPerformanceMonitor().also { performanceMonitor = it }
    }

    override fun getPreviewFragment(
        context: Context,
        wallpaperInfo: WallpaperInfo,
        mode: Int,
        viewAsHome: Boolean,
        viewFullScreen: Boolean,
        testingModeEnabled: Boolean
    ): Fragment {
        val args = Bundle()
        args.putParcelable(PreviewFragment.ARG_WALLPAPER, wallpaperInfo)
        args.putInt(PreviewFragment.ARG_PREVIEW_MODE, mode)
        args.putBoolean(PreviewFragment.ARG_VIEW_AS_HOME, viewAsHome)
        args.putBoolean(PreviewFragment.ARG_FULL_SCREEN, viewFullScreen)
        args.putBoolean(PreviewFragment.ARG_TESTING_MODE_ENABLED, testingModeEnabled)
        val fragment = ImagePreviewFragment()
        fragment.arguments = args
        return fragment
    }

    override fun getRequester(unused: Context): Requester {
        return requester ?: TestRequester().also { requester = it }
    }

    override fun getSystemFeatureChecker(): SystemFeatureChecker {
        return systemFeatureChecker ?: TestSystemFeatureChecker().also { systemFeatureChecker = it }
    }

    override fun getUserEventLogger(context: Context): UserEventLogger {
        return userEventLogger ?: TestUserEventLogger().also { userEventLogger = it }
    }

    override fun getWallpaperManagerCompat(context: Context): WallpaperManagerCompat {
        return wallpaperManagerCompat
            ?: TestWallpaperManagerCompat(context.applicationContext).also {
                wallpaperManagerCompat = it
            }
    }

    override fun getWallpaperPersister(context: Context): WallpaperPersister {
        return wallpaperPersister
            ?: TestWallpaperPersister(context.applicationContext).also { wallpaperPersister = it }
    }

    override fun getPreferences(context: Context): WallpaperPreferences {
        return prefs ?: TestWallpaperPreferences().also { prefs = it }
    }

    override fun getWallpaperPreviewFragmentManager(): WallpaperPreviewFragmentManager {
        return wallpaperPreviewFragmentManager
            ?: TestWallpaperPreviewFragmentManager().also { wallpaperPreviewFragmentManager = it }
    }

    override fun getWallpaperRefresher(context: Context): WallpaperRefresher {
        return wallpaperRefresher
            ?: TestWallpaperRefresher(context.applicationContext).also { wallpaperRefresher = it }
    }

    override fun getWallpaperRotationRefresher(): WallpaperRotationRefresher {
        return wallpaperRotationRefresher
            ?: WallpaperRotationRefresher {
                    context: Context?,
                    listener: WallpaperRotationRefresher.Listener ->
                    // Not implemented
                    listener.onError()
                }
                .also { wallpaperRotationRefresher = it }
    }

    override fun getWallpaperStatusChecker(): WallpaperStatusChecker {
        return object : WallpaperStatusChecker {
            override fun isHomeStaticWallpaperSet(context: Context): Boolean {
                return true
            }

            override fun isLockWallpaperSet(context: Context): Boolean {
                return true
            }
        }
    }

    override fun getFlags(): BaseFlags {
        return flags ?: object : BaseFlags() {}.also { flags = it }
    }

    override fun getUndoInteractor(context: Context): UndoInteractor {
        return undoInteractor
            ?: UndoInteractor(
                GlobalScope,
                UndoRepository(),
                HashMap()
            ) // Empty because we don't support undoing in WallpaperPicker2..also{}
    }
}

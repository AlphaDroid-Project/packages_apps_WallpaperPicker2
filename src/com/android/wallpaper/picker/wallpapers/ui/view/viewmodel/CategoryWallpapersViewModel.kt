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

package com.android.wallpaper.picker.wallpapers.ui.view.viewmodel

import android.app.WallpaperInfo
import android.app.WallpaperManager
import android.app.WallpaperManager.FLAG_LOCK
import android.app.WallpaperManager.FLAG_SYSTEM
import android.content.Context
import android.text.TextUtils
import android.util.ArraySet
import android.util.Log
import androidx.lifecycle.ViewModel
import com.android.wallpaper.module.WallpaperPreferences
import com.android.wallpaper.picker.common.preview.data.repository.PersistentWallpaperModelRepository
import com.android.wallpaper.picker.data.WallpaperModel
import com.android.wallpaper.picker.preview.ui.WallpaperPreviewActivity
import com.android.wallpaper.picker.wallpapers.domain.interactor.CategoryWallpapersInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * [CategoryWallpapersViewModel] is responsible for preparing and managing UI-related data for the
 * Wallpapers screen in a lifecycle-aware manner.
 */
@HiltViewModel
class CategoryWallpapersViewModel
@Inject
constructor(
    private val categoryWallpapersInteractor: CategoryWallpapersInteractor,
    private val persistentWallpaperModelRepository: PersistentWallpaperModelRepository,
    private val wallpaperManager: WallpaperManager,
    private val wallpaperPreferences: WallpaperPreferences,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    /**
     * This [Flow] emits [CategoryWallpapersContentViewModel] by mapping the [List<WallpaperModel>]
     * of a category to [List<CategoryWallpapersItemViewModel>]
     */
    val categoryWallpapersContentViewModel: Flow<CategoryWallpapersContentViewModel> =
        combine(
            categoryWallpapersInteractor.selectedCategoryWallpapers,
            categoryWallpapersInteractor.categoryTitle,
        ) { wallpapers, title ->
            if (DEBUG) {
                Log.d(TAG, "WallpaperModels: ${wallpapers.size}")
            }

            val groupedWallpapers =
                wallpapers
                    .groupBy { wallpaper ->
                        val groupName =
                            when (wallpaper) {
                                is WallpaperModel.LiveWallpaperModel ->
                                    wallpaper.liveWallpaperData.groupName
                                is WallpaperModel.StaticWallpaperModel ->
                                    wallpaper.downloadableWallpaperData?.groupName
                                else -> null
                            }

                        if (groupName.isNullOrEmpty()) {
                            DEFAULT_GROUP
                        } else {
                            groupName
                        }
                    }
                    .toMutableMap()

            if (DEBUG) {
                Log.d(TAG, "Wallpaper groups: ")
                for ((groupName, wallpapers) in groupedWallpapers) {
                    Log.d(TAG, "Group NAME: ${groupName}")
                    for (wallpaper in wallpapers) {
                        Log.d(TAG, "${wallpaper}")
                    }
                }
            }

            val firstEntry = groupedWallpapers.keys.firstOrNull()

            val templates = buildList {
                if (
                    firstEntry != null &&
                        firstEntry != DEFAULT_GROUP &&
                        (groupedWallpapers[firstEntry]?.get(0)
                            is WallpaperModel.LiveWallpaperModel) &&
                        (groupedWallpapers[firstEntry]?.get(0)
                                as? WallpaperModel.LiveWallpaperModel)
                            ?.creativeWallpaperData != null &&
                        (groupedWallpapers[firstEntry]?.size ?: 0) > 1
                ) {
                    add(CategoryWallpapersItemViewModel.PrimaryHeaderViewModelCategory(firstEntry))
                    val items =
                        groupedWallpapers[firstEntry]?.map {
                            CategoryWallpapersItemViewModel.ThumbnailsViewModelCategory(
                                thumbnailAsset = it.commonWallpaperData.thumbAsset,
                                title = it.commonWallpaperData.title,
                                contentDescription = it.commonWallpaperData.title,
                                onSectionClicked = {
                                    persistentWallpaperModelRepository.setWallpaperModel(it)
                                    val previewIntent =
                                        WallpaperPreviewActivity.intentBuilder(context, true)
                                            .refreshCategory(true)
                                            .navigateToExtendedEffects(false)
                                            .build()
                                    return@ThumbnailsViewModelCategory previewIntent
                                },
                            )
                        }
                    items?.let {
                        add(CategoryWallpapersItemViewModel.TemplateThumbnailsViewModelCategory(it))
                        groupedWallpapers.remove(firstEntry)
                    }
                }
            }

            val wallpaperItems = buildList {
                for ((groupName, wallpapers) in groupedWallpapers) {
                    if (groupName != DEFAULT_GROUP) {
                        add(
                            CategoryWallpapersItemViewModel.SecondaryHeaderViewModelCategory(
                                groupName
                            )
                        )
                    }
                    val currentHomeWallpaper: android.app.WallpaperInfo? =
                        wallpaperManager.getWallpaperInfo(FLAG_SYSTEM)
                    val currentLockWallpaper: android.app.WallpaperInfo? =
                        wallpaperManager.getWallpaperInfo(FLAG_LOCK)
                    val appliedWallpaperIds = getAppliedWallpaperIds()

                    val items =
                        wallpapers.map {
                            CategoryWallpapersItemViewModel.ThumbnailsViewModelCategory(
                                thumbnailAsset = it.commonWallpaperData.thumbAsset,
                                title = it.commonWallpaperData.title,
                                contentDescription = it.commonWallpaperData.title,
                                isApplied =
                                    if (it is WallpaperModel.LiveWallpaperModel) {
                                        it.isApplied(currentHomeWallpaper, currentLockWallpaper)
                                    } else {
                                        appliedWallpaperIds.contains(
                                            it.commonWallpaperData.id.uniqueId
                                        )
                                    },
                                isDownloadable =
                                    (it as? WallpaperModel.StaticWallpaperModel)
                                        ?.downloadableWallpaperData != null,
                                onSectionClicked = {
                                    persistentWallpaperModelRepository.setWallpaperModel(it)
                                    val previewIntent =
                                        WallpaperPreviewActivity.intentBuilder(context, true)
                                            .refreshCategory(true)
                                            .navigateToExtendedEffects(false)
                                            .build()
                                    return@ThumbnailsViewModelCategory previewIntent
                                },
                            )
                        }
                    add(CategoryWallpapersItemViewModel.PlainThumbnailsViewModelCategory(items))
                }
            }

            if (DEBUG) {
                Log.d(TAG, "here is the list of wallpaperItems yo: ${wallpaperItems}")
            }

            return@combine CategoryWallpapersContentViewModel(
                rotationEnabled = false,
                title = title,
                wallpaperItems = templates + wallpaperItems,
            )
        }

    // TODO(b/444284275): remove references to remote ids
    private fun getAppliedWallpaperIds(): Set<String> {
        val wallpaperInfo = wallpaperManager?.wallpaperInfo
        val appliedWallpaperIds: MutableSet<String> = ArraySet()
        val homeWallpaperId =
            if (wallpaperInfo != null) {
                wallpaperInfo.serviceName
            } else {
                wallpaperPreferences.getHomeWallpaperRemoteId()
            }
        if (!homeWallpaperId.isNullOrEmpty()) {
            appliedWallpaperIds.add(homeWallpaperId)
        }
        val isLockWallpaperApplied =
            wallpaperManager!!.getWallpaperId(WallpaperManager.FLAG_LOCK) >= 0
        val lockWallpaperId = wallpaperPreferences.getLockWallpaperRemoteId()
        if (isLockWallpaperApplied && !lockWallpaperId.isNullOrEmpty()) {
            appliedWallpaperIds.add(lockWallpaperId)
        }
        return appliedWallpaperIds
    }

    private fun WallpaperModel.LiveWallpaperModel.isApplied(
        currentHomeWallpaper: WallpaperInfo?,
        currentLockWallpaper: WallpaperInfo?,
    ): Boolean {
        val component: WallpaperInfo = liveWallpaperData.systemWallpaperInfo
        val serviceName = component.serviceName
        val isAppliedToHome =
            currentHomeWallpaper != null &&
                TextUtils.equals(currentHomeWallpaper.serviceName, serviceName)
        val isAppliedToLock =
            currentLockWallpaper != null &&
                TextUtils.equals(currentLockWallpaper.serviceName, serviceName)

        return if (creativeWallpaperData != null) {
            ((isAppliedToHome || isAppliedToLock) && creativeWallpaperData.isCurrent)
        } else {
            isAppliedToHome || isAppliedToLock
        }
    }

    companion object {
        const val DEBUG = false
        const val TAG = "CategoryWallpapersViewModel"
        const val DEFAULT_GROUP = "default_group"
    }
}

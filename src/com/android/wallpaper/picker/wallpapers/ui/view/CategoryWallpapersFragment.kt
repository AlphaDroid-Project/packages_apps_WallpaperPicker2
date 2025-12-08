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

package com.android.wallpaper.picker.wallpapers.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.wallpaper.picker.AppbarFragment
import com.android.wallpaper.picker.wallpapers.ui.view.compose.WallpapersScreenContent
import com.android.wallpaper.picker.wallpapers.ui.view.viewmodel.CategoryWallpapersViewModel
import dagger.hilt.android.AndroidEntryPoint

/** This fragment displays the collection of wallpapers for the selected category */
@AndroidEntryPoint(AppbarFragment::class)
class CategoryWallpapersFragment : Hilt_CategoryWallpapersFragment() {

    private val viewModel: CategoryWallpapersViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val state by
                    viewModel.categoryWallpapersContentViewModel.collectAsStateWithLifecycle(null)
                state?.let { WallpapersScreenContent(it) }
            }
        }
    }

    companion object {
        private const val TAG = "CategoryWallpapersFragment"

        fun newInstance(): CategoryWallpapersFragment {
            val fragment = CategoryWallpapersFragment()
            return fragment
        }
    }
}

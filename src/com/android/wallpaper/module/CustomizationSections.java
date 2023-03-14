package com.android.wallpaper.module;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;

import com.android.wallpaper.model.CustomizationSectionController;
import com.android.wallpaper.model.CustomizationSectionController.CustomizationSectionNavigationController;
import com.android.wallpaper.model.PermissionRequester;
import com.android.wallpaper.model.WallpaperColorsViewModel;
import com.android.wallpaper.model.WallpaperPreviewNavigator;
import com.android.wallpaper.model.WorkspaceViewModel;
import com.android.wallpaper.util.DisplayUtils;

import java.util.List;

/** Interface for carry {@link CustomizationSectionController}s. */
public interface CustomizationSections {

    /** Enumerates all screens supported by {@code getSectionControllersForScreen}. */
    enum Screen {
        LOCK_SCREEN,
        HOME_SCREEN,
    }

    /**
     * Gets a new instance of the section controller list for the given {@link Screen}.
     *
     * Note that the section views will be displayed by the list ordering.
     *
     * <p>Don't keep the section controllers as singleton since they contain views.
     */
    List<CustomizationSectionController<?>> getSectionControllersForScreen(
            Screen screen,
            FragmentActivity activity,
            LifecycleOwner lifecycleOwner,
            WallpaperColorsViewModel wallpaperColorsViewModel,
            WorkspaceViewModel workspaceViewModel,
            PermissionRequester permissionRequester,
            WallpaperPreviewNavigator wallpaperPreviewNavigator,
            CustomizationSectionNavigationController sectionNavigationController,
            @Nullable Bundle savedInstanceState,
            CurrentWallpaperInfoFactory wallpaperInfoFactory,
            DisplayUtils displayUtils);

    /**
     * Gets a new instance of the section controller list.
     *
     * Note that the section views will be displayed by the list ordering.
     *
     * <p>Don't keep the section controllers as singleton since they contain views.
     */
    List<CustomizationSectionController<?>> getAllSectionControllers(
            FragmentActivity activity,
            LifecycleOwner lifecycleOwner,
            WallpaperColorsViewModel wallpaperColorsViewModel,
            WorkspaceViewModel workspaceViewModel,
            PermissionRequester permissionRequester,
            WallpaperPreviewNavigator wallpaperPreviewNavigator,
            CustomizationSectionNavigationController sectionNavigationController,
            @Nullable Bundle savedInstanceState,
            DisplayUtils displayUtils);
}

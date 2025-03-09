package com.android.wallpaper.testing

import com.android.wallpaper.module.PackageStatusNotifier
import javax.inject.Inject
import javax.inject.Singleton

/** Test implementation of [PackageStatusNotifier] */
@Singleton
class TestPackageStatusNotifier @Inject constructor() : PackageStatusNotifier {
    override fun addListener(listener: PackageStatusNotifier.Listener?, action: String?) {
        // Do nothing intended
    }

    override fun removeListener(listener: PackageStatusNotifier.Listener?) {
        // Do nothing intended
    }
}

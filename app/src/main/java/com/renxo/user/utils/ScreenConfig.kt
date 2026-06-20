package com.renxo.user.utils


import android.content.Context
import android.content.res.Configuration

data class WindowInfo(
    val isTablet: Boolean,
    val isPortrait: Boolean,
)


fun getWindowInfo(context: Context): WindowInfo {
    val configuration = context.resources.configuration

    // Check if device is a tablet based on screen size
    val isTablet = configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >=
            Configuration.SCREENLAYOUT_SIZE_LARGE

    // Check if device is in portrait orientation
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    return WindowInfo(
        isTablet = isTablet,
        isPortrait = isPortrait
    )
}

package com.renxo.user.utils

import android.app.Application
import com.renxo.user.networking.NetworkConnectivityManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {
    companion object {
        private lateinit var _connectivityManager: NetworkConnectivityManager
        private lateinit var _preferenceManager: PreferenceManager
        val preferenceManager get() = _preferenceManager
        val connectivityManager get() = _connectivityManager

    }

    override fun onCreate() {
        super.onCreate()
        _connectivityManager = NetworkConnectivityManager(this)
        _preferenceManager =
            PreferenceManager(filesDir.resolve(AppConstants.Preferences.APP_PREFERENCES).absolutePath)

    }

    override fun onTerminate() {
        super.onTerminate()
        _connectivityManager.cleanup()
    }
}
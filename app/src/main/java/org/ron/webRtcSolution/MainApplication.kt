/**
 * Created by Ronil Gwalani
 * 
 */
package org.ron.webRtcSolution

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.ron.webRtcSolution.di.appModule
//import org.ron.webrtccall.di.libraryModule
//import org.ron.webrtccall.utils.AppVisibilityTracker

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
//        registerActivityLifecycleCallbacks(AppVisibilityTracker)

        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(/*libraryModule,*/ appModule)
        }

    }
}

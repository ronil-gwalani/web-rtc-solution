package org.ron.webrtccall.network

import android.app.Application
import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.ron.webrtccall.di.libraryModule
import org.ron.webrtccall.utils.AppVisibilityTracker
import java.io.InputStream

class SecretInputStream(private val resourceFile: Int) {


    fun Context.getImputeStream(): InputStream {
        return resources.openRawResource(resourceFile)
    }
}
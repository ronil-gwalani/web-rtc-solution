package org.ron.webrtccall.network

import android.content.Context
import java.io.InputStream

class SecretInputStream(private val context: Context, private val resourceFile: Int) {
    fun getImputeStream(): InputStream {
        return context.resources.openRawResource(resourceFile)
    }
}
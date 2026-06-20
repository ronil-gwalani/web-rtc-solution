package com.renxo.user.networking

import io.ktor.http.ContentType
import java.io.File

//KeyValue is used by ApiHelper to append ?key=value pairs.
data class KeyValue(val key: String, val value: String?)

//MultiPartObj is for file-upload (multipart) requests.
data class MultiPartObj(
    val key: String,
    val file: File,
    val contentType: ContentType? = null,
    val contentDisposition: String? = key,
)

//ApiException is thrown on non-200 HTTP responses.
data class ApiException(val code: Int = 0, val errorMessage: String) : Exception()

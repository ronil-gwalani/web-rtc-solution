package com.renxo.user.networking

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import io.ktor.http.contentType

class ApiHelper(val client: HttpClient) {
    val baseUrl = "http://url/api/"
    suspend inline fun <reified T> getRequest(
        endPoint: String,
        authToken: String? = null,
        params: List<KeyValue> = emptyList(),
    ): Result<T> {
        val res = fetchGetResponse(endPoint, params, authToken) {
            return Result.failure(
                ApiException(
                    0,
                    it
                )

            )
        }

        return if (res?.status?.value == 200) {
            Result.success(
                res.body<T>()
            )
        } else {
            Result.failure(
                ApiException(
                    res?.status?.value ?: 0,
                    res?.status?.description ?: ""
                )

            )
        }

    }

    suspend inline fun fetchGetResponse(
        endPoint: String,
        params: List<KeyValue> = emptyList(),
        authToken: String? = null,
        errorCase: (String) -> Unit

    ): HttpResponse? {

        val res = try {
            client.get(
                urlString = baseUrl + endPoint
            ) {

                params.forEach {
                    parameter(it.key, it.value)
                }
                headers {
                    append(HttpHeaders.Accept, ContentType.Application.Json.toString())
                    authToken?.let {
                        append(HttpHeaders.Authorization, it)
                    }
                }
            }
        } catch (e: Exception) {
            errorCase(e.message.toString())
            null
        }
        return res
    }


    suspend inline fun <reified T, reified K> postRequest(
        endPoint: String,
        authToken: String? = null,
        params: List<KeyValue> = emptyList(),
        formData: List<MultiPartObj> = emptyList(),
        body: K? = null,

        ): Result<T> {
        val res = fetchPostResponse(endPoint, authToken, params, formData, body) {
            return Result.failure(
                ApiException(
                    0,
                    it
                )

            )
        }
        return if (res?.status?.value == 200) {
            Result.success(
                res.body<T>()
            )
        } else {
            Result.failure(
                ApiException(
                    res?.status?.value ?: 0,
                    res?.status?.description ?: ""
                )

            )
        }

    }


    suspend inline fun <reified K> fetchPostResponse(
        endPoint: String,
        authToken: String? = null,
        params: List<KeyValue> = emptyList(),
        formData: List<MultiPartObj> = emptyList(),
        body: K? = null,
        errorCase: (String) -> Unit
    ): HttpResponse? {
        Log.e("fetchGetResponse", ": $endPoint")

        val res = try {
            client.post(
//                urlString = baseUrl + endPoint
                urlString = endPoint

            ) {
                headers {
                    append(HttpHeaders.Accept, ContentType.Application.Json.toString())
                    authToken?.let {
                        append(HttpHeaders.Authorization, it)
                    }
                }
                body?.let {
                    contentType(ContentType.Application.Json)
                    setBody(body)
                }
                if (params.isNotEmpty()) {
                    contentType(ContentType.Application.FormUrlEncoded)
                    setBody(FormDataContent(Parameters.build {
                        params.forEach {
                            append(it.key, it.value.toString())
                        }
                    }))
                }
                if (formData.isNotEmpty()) {
                    setBody(
                        MultiPartFormDataContent(
                        formData {
                            params.forEach {
                                append(it.key, it.value.toString())
                            }
                            formData.forEach {
                                append(it.key, it.file.readBytes(), Headers.build {
                                    append(
                                        HttpHeaders.ContentDisposition,
                                        "filename=\"${it.file.name}\""
                                    )
                                    it.contentType?.let { type ->
                                        append(HttpHeaders.ContentType, type.toString())
                                    }
                                })
                            }
                        }
                    ))
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
            errorCase(e.message.toString())
            null
        }
        return res
    }


    suspend inline fun <reified T, reified K> deleteRequest(
        endPoint: String,
        authToken: String? = null,
        params: List<KeyValue> = emptyList(),
        formData: List<MultiPartObj> = emptyList(),
        body: K? = null,

        ): Result<T?> {
        val res = fetchDeleteResponse(endPoint, authToken, params, formData, body)
        return if (res?.status?.value == 200) {
            Result.success(
                res.body<T>()
            )
        } else {
            Result.failure(
                ApiException(
                    res?.status?.value ?: 0,
                    res?.status?.description ?: ""
                )

            )
        }

    }

    suspend inline fun <reified K> fetchDeleteResponse(
        endPoint: String,
        authToken: String? = null,
        params: List<KeyValue> = emptyList(),
        formData: List<MultiPartObj> = emptyList(),
        body: K? = null,
    ): HttpResponse? {


        val res = try {
            client.delete(
                urlString = baseUrl + endPoint

            ) {
                headers {
                    append(HttpHeaders.Accept, ContentType.Application.Json.toString())
                    authToken?.let {
                        append(HttpHeaders.Authorization, it)
                    }
                }
                body?.let {
                    contentType(ContentType.Application.Json)
                    setBody(body)
                }
                if (params.isNotEmpty()) {
                    contentType(ContentType.Application.FormUrlEncoded)
                    setBody(FormDataContent(Parameters.build {
                        params.forEach {
                            append(it.key, it.value.toString())
                        }
                    }))
                }
                if (formData.isNotEmpty()) {
                    setBody(
                        MultiPartFormDataContent(
                        formData {
                            params.forEach {
                                append(it.key, it.value.toString())
                            }
                            formData.forEach {
                                append(it.key, it.file.readBytes(), Headers.build {
                                    append(
                                        HttpHeaders.ContentDisposition,
                                        "filename=\"${it.file.name}\""
                                    )
                                    it.contentType?.let { type ->
                                        append(HttpHeaders.ContentType, type.toString())
                                    }
                                })
                            }
                        }
                    ))
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        return res
    }


}




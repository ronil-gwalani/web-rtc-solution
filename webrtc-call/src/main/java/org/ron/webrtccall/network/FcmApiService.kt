/**
 * Created by Ronil Gwalani
 * 
 */
package org.ron.webrtccall.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface FcmApiService {
    @POST("v1/projects/{project}/messages:send")
    suspend fun sendMessage(
        @Path("project") projectId: String,
        @Header("Authorization") authHeader: String,
        @Body request: FcmRequest
    ): Response<FcmResponse>
}

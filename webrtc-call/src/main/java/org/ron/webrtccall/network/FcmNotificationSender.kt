/**
 * Created by Ronil Gwalani
 * WebRTC Solution - FCM Notification Sender Implementation
 */
package org.ron.webrtccall.network

import android.content.Context
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ron.webrtccall.data.PreferenceProvider


class FcmNotificationSender(
    private val preferenceProvider: PreferenceProvider,
    private val api: FcmApiService,
    private val inputStream: SecretInputStream
) : SignalingService {
    private val googleCredentials by lazy {
        GoogleCredentials
            .fromStream(inputStream.getImputeStream())
            .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
    }

    private suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        val savedToken = preferenceProvider.getOAuthToken()
        savedToken.ifEmpty {
            try {
                googleCredentials.refresh()
                val token = "Bearer ${googleCredentials.accessToken.tokenValue}"
                preferenceProvider.saveOAuthToken(token)
                token
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    override suspend fun sendCallNotification(
        targetToken: String,
        callerId: String,
        callerName: String,
        roomId: String,
        isAudioOnly: Boolean
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val authHeader = getAccessToken()
                ?: return@withContext Result.failure(Exception("Failed to get access token"))
            val projectId = googleCredentials.projectId
            val request = FcmRequest(
                message = RonPushNewNotificationMessageModel(
                    token = targetToken,
                    data = mapOf(
                        "type" to "incoming_call",
                        "callerId" to callerId,
                        "callerName" to callerName,
                        "roomId" to roomId,
                        "isAudioOnly" to isAudioOnly.toString()
                    ),
                )
            )

            val response = api.sendMessage(projectId, authHeader, request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                preferenceProvider.clearSavedOAuthToken()
                // Retry once
                val newAuthHeader = getAccessToken() ?: return@withContext Result.failure(
                    Exception(
                        "Retry failed: no token"
                    )
                )
                val retryResponse = api.sendMessage(projectId, newAuthHeader, request)
                if (retryResponse.isSuccessful) Result.success(Unit)
                else Result.failure(
                    Exception(
                        "FCM Error: ${
                            retryResponse.errorBody()?.string()
                        }"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


package com.renxo.user.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.renxo.user.networking.ApiHelper
import com.renxo.user.networking.ApiRepository
import com.renxo.user.networking.AuthResponse
import com.renxo.user.networking.NetworkCallback
import com.renxo.user.utils.AppConstants
import com.renxo.user.utils.BaseViewModel
import com.renxo.user.utils.preferenceManager
import com.renxo.user.webSocket.WebSocketInterface
import com.renxo.user.webSocket.WebSocketInterfaceImpl
import com.renxo.user.webSocket.WebSocketStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.HttpClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class ReAuthVM @Inject constructor(private val repository: ApiRepository) : BaseViewModel(),
    WebSocketInterface by WebSocketInterfaceImpl() {


    private val authCall by lazy { CallingHelper<AuthResponse>() }
    private var refreshTokenJob: Job? = null
    var errorMessage by mutableStateOf<String?>(null)


    fun startTokenRefreshTimer() {
        refreshTokenJob?.cancel() // Cancel any existing job

        refreshTokenJob =  viewModelScope.launch {  
            val expiryTimestamp =
                preferenceManager.getString(AppConstants.Preferences.EXPIRY_TIMEOUT)?.toLong() ?: 0L

            try {
                val currentTime = System.currentTimeMillis() / 1000 // Convert to seconds
                val timeUntilRefresh =
                    expiryTimestamp - currentTime - 300// Subtract 5 minutes (300 seconds)
                if (timeUntilRefresh <= 0) {
                    refreshToken() // Refresh immediately if already expired or close to expiry
                    return@launch
                }

                delay(timeUntilRefresh * 1000) // Convert seconds to milliseconds for delay
                refreshToken()
            } catch (e: Exception) {
                errorMessage = "Token refresh timer error: ${e.message}"
            }
        }
    }

    private suspend fun refreshToken() {
        if (webSocketStatus.last() != WebSocketStatus.Connected) {
            return
        }
        try {
            preferenceManager.getAuthToken()?.let { currentToken ->
                preferenceManager.getAuthUrl()?.let { currentAuthUrl ->
                    authCall.launchCall(
                        { repository.refreshToken(currentToken, currentAuthUrl) },
                        object : NetworkCallback<AuthResponse> {
                            override fun noInternetAvailable() {
                                errorMessage = "No internet connection"
                            }

                            override fun unKnownErrorFound(error: String) {
                                errorMessage = error
                            }

                            override fun onProgressing(value: Boolean) {
                                // Don't show1 progress for background refresh
                            }

                            override fun onRequestAgainRestarted() {
                            }

                            override fun onSuccess(result: AuthResponse) {
                                result.let {
                                     viewModelScope.launch {  
                                        it.token?.let { newToken ->
                                            preferenceManager.saveAuthToken(newToken)
                                        }
                                        it.url?.let { newUrl ->
                                            preferenceManager.saveMainUrl(newUrl)
                                        }
                                        // Start new timer for the next refresh
                                        it.expiry?.let { newExpiry ->
                                            preferenceManager.setValue(
                                                AppConstants.Preferences.EXPIRY_TIMEOUT, newExpiry
                                            )
                                            startTokenRefreshTimer()
                                        }
                                    }
                                }
                            }
                        })
                }
            }
        } catch (e: Exception) {
            errorMessage = "Token refresh error: ${e.message}"
        }
    }


    override fun onCleared() {
        Log.e("onCleared", ": ")
        super.onCleared()
        refreshTokenJob?.cancel()
    }

}


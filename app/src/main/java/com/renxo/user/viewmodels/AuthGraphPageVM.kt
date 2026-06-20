package com.renxo.user.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renxo.user.navigation.AuthRoutes
import com.renxo.user.navigation.UiEvents
import com.renxo.user.navigation.navigateTo

import com.renxo.user.utils.preferenceManager
import com.renxo.user.webSocket.WebSocketInterface
import com.renxo.user.webSocket.WebSocketInterfaceImpl
import com.renxo.user.webSocket.WebSocketStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AuthGraphPageVM : ViewModel(), WebSocketInterface by WebSocketInterfaceImpl() {
    private val _uiEventsFlow = MutableSharedFlow<UiEvents>()
    val uiEventsFlow: Flow<UiEvents> = _uiEventsFlow
    private var isHomeCalled by mutableStateOf(false)

    init {
        connectionListener()
        connectionStatusMessage()
    }

    private fun connectionStatusMessage() {
        viewModelScope.launch {
            webSocketErrorMessage.collectLatest {
                it.let {
                    _uiEventsFlow.emit(WebSocketConnectionMessage(it))
                }
            }

        }

    }

    private fun connectionListener() {

        viewModelScope.launch {
            webSocketStatus.collectLatest {
                if (it == WebSocketStatus.Connected) {
                    if (!isHomeCalled) {
                        isHomeCalled = true
                        launch {
                            _uiEventsFlow.emit(navigateTo(AuthRoutes.HomeScreen, finishAll = true))
                            _uiEventsFlow.emit(StartTokenRefreshTimer)
                        }

                    }
                } else if (it == WebSocketStatus.Disconnected) {
                    reconnectWebSocket()

                } else if (it == WebSocketStatus.Failure) {
                    isHomeCalled = false
                    viewModelScope.launch {
                        delay(500)
                        _uiEventsFlow.emit(
                            navigateTo(
                                AuthRoutes.AuthenticatePage,
                                finishAll = true
                            )
                        )
                    }

                }

            }
        }

    }


    private fun reconnectWebSocket() {
        viewModelScope.launch {
            val savedAuthToken = preferenceManager.getAuthToken()
            val mainUrl = preferenceManager.getMainUrl()
            connect(mainUrl, savedAuthToken)
        }
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
       clearWebsocket()

    }

    data object StartTokenRefreshTimer : UiEvents
    data class WebSocketConnectionMessage(val message: String) : UiEvents

}


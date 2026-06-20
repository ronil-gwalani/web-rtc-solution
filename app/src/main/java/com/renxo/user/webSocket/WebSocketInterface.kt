package com.renxo.user.webSocket

import com.renxo.user.models.ResponseModel
import kotlinx.coroutines.flow.Flow

interface WebSocketInterface {
    val response: Flow<ResponseModel>
    fun connect(url: String?, authToken: String?)
    fun sendMessage(message: String)
    fun disconnect()
    fun clearResponse()
    fun clearWebsocket()
    val webSocketErrorMessage: Flow<String>
    val webSocketStatus: Flow<WebSocketStatus>
}

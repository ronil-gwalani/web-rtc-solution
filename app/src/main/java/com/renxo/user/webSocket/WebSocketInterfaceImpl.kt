package com.renxo.user.webSocket

import com.renxo.user.models.ResponseModel
import com.renxo.user.utils.json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapNotNull

class WebSocketInterfaceImpl : WebSocketInterface {
    override val response: Flow<ResponseModel> = WebSocketManager.response.mapNotNull {
        it?.let { it1 ->
            json.decodeFromString<ResponseModel>(
                it1
            )
        }
    }

    override val webSocketErrorMessage: Flow<String> =
        WebSocketManager.webSocketErrorMessage.filterNotNull()

    override val webSocketStatus: Flow<WebSocketStatus> =
        WebSocketManager.connectionStatus.filterNotNull()

    override fun connect(url: String?, authToken: String?) {
        disconnect()// Ensure any lingering connection is terminated
        if (!url.isNullOrEmpty() && !authToken.isNullOrEmpty()) {
            WebSocketManager.connect(url, authToken)
        } else {
            WebSocketManager.setDisconnectStatus(WebSocketStatus.Failure)
        }

    }

    override fun sendMessage(message: String) {
        WebSocketManager.sendMessage(message)
    }

    override fun disconnect() {
        WebSocketManager.disconnect()
    }

    override fun clearResponse() {
        WebSocketManager.clearResponse()
    }

    override fun clearWebsocket() {
        WebSocketManager.clear()
    }


}
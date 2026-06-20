package com.renxo.user.webSocket

import android.util.Log
import com.renxo.user.utils.WebSocketInterceptor
import com.renxo.user.utils.preferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.ConcurrentLinkedQueue

object WebSocketManager {
    private val _responseFromServer = MutableSharedFlow<String?>(
//        replay = 2, // keeps last 3 values in buffer
    )
    val response: Flow<String?> = _responseFromServer


    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    private var onConnectionStateChanged: ((Boolean) -> Unit)? = null

    private val _isConnected = MutableStateFlow(false)
    val isConnected: Flow<Boolean> = _isConnected

    private val _connectionStatus = MutableStateFlow<WebSocketStatus?>(null)
    val connectionStatus: Flow<WebSocketStatus?> = _connectionStatus


    fun setDisconnectStatus(status: WebSocketStatus) {
        disconnectedJob?.cancel()
        _connectionStatus.value = status
    }


    private val _webSocketErrorMessage = MutableStateFlow<String?>(null)
    val webSocketErrorMessage: Flow<String?> = _webSocketErrorMessage

    fun clear() {
      cleanup()
    }   

    fun connect(url: String, authToken: String) {
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $authToken")
            .build()
        Log.d("WebSocket", "$url->>> $request")

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "WebSocket connected")
                _connectionStatus.value = WebSocketStatus.Connected
                _isConnected.value = true
                onConnectionStateChanged?.invoke(true)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "Message received: $text")
                WebSocketInterceptor.addJsonResponse(
                    "Received at ${WebSocketInterceptor.getCurrentTime()}",
                    text
                )
                CoroutineScope(Dispatchers.IO).launch {
                    _responseFromServer.emit(text)
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "WebSocket closed: $reason")
                _connectionStatus.value = WebSocketStatus.Disconnected
                disconnect()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "WebSocket error: ${t.message}")
                _webSocketErrorMessage.value = ("WebSocket error: ${t.message}")
                _connectionStatus.value = WebSocketStatus.Failure
                CoroutineScope(Dispatchers.IO).launch {
                    preferenceManager.clearAuthToken()
                    disconnect()
                    cleanup()
                    delay(200)
                    _webSocketErrorMessage.value = null
                }

            }
        })


    }

    private var disconnectedJob: Job? = null
    private var pendingMessages = ConcurrentLinkedQueue<String>()
    private var reconnectionJob: Job? = null

    fun sendMessage(message: String) {
        clearResponse()
        WebSocketInterceptor.addJsonResponse(
            "Send at ${WebSocketInterceptor.getCurrentTime()}",
            message
        )

        if (webSocket == null || _connectionStatus.value != WebSocketStatus.Connected) {
            // Store the message to send once reconnected
            pendingMessages.add(message)
            Log.d("WebSocket", "Message Not Yet Sent: $message")
            // Only start a new reconnection job if one isn't already running
            if (reconnectionJob == null || reconnectionJob?.isActive == false) {
                reconnectionJob = CoroutineScope(Dispatchers.IO).launch {
                    _connectionStatus.value = WebSocketStatus.Disconnected
                    _connectionStatus.collectLatest { status ->
                        if (status == WebSocketStatus.Connected && pendingMessages.isNotEmpty()) {
                            // Process all pending messages when connected
                            while (pendingMessages.isNotEmpty()) {
                                pendingMessages.poll()?.let {
                                    webSocket?.send(it)
                                    Log.d(
                                        "WebSocket",
                                        "Pending Message Sent After Reconnection $it"
                                    )
                                }
                            }
                            // Cancel the job once messages are sent
                            reconnectionJob?.cancel()
                            reconnectionJob = null
                        }
                    }
                }
            }
        } else {
            // If already connected, send immediately
            Log.d("WebSocket", "Message Sent: $message")
            webSocket?.send(message)
        }
    }

    // Call this when manually disconnecting or cleaning up
    private fun cleanup() {
        webSocket?.cancel()
        reconnectionJob?.cancel()
        reconnectionJob = null
        pendingMessages.clear()
    }

    fun disconnect() {
//        cleanup()
        _isConnected.value = false
        webSocket?.close(1000, "App closed")
        webSocket = null
        onConnectionStateChanged?.invoke(false)
    }


    fun clearResponse() {
        CoroutineScope(Dispatchers.IO).launch {
            _responseFromServer.emit(null) // Clear the response
        }
    }

}


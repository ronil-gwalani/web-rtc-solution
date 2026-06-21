package org.ron.webrtccall

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import java.util.Collections

class KtorSignaling(private val roomId: String) : WebRtcSignaling {
    private val client = HttpClient {
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var session: DefaultClientWebSocketSession? = null
    
    private val _onOffer = MutableSharedFlow<Pair<SessionDescription, Boolean>>(replay = 1)
    private val _onAnswer = MutableSharedFlow<SessionDescription>(replay = 1)
    private val _onCandidate = MutableSharedFlow<IceCandidate>(replay = 1)
    private val _onDisconnect = MutableSharedFlow<Unit>(replay = 1)
    private val _callType = MutableSharedFlow<Boolean>(replay = 1)
    
    // Deduplication for candidates received via sync and live
    private val processedCandidates = Collections.synchronizedSet(mutableSetOf<String>())

    init {
        connect()
    }

    private fun connect() {
        scope.launch {
            try {
                client.webSocket(SignalingConfig.getUrl(roomId)) {
                    session = this
                    // Send join message to trigger state sync from server
                    sendSerialized(SignalingMessage(type = "join", roomId = roomId))

                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            val msg = Json.decodeFromString<SignalingMessage>(frame.readText())
                            handleMessage(msg)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                delay(3000)
                if (isActive) connect() // Simple reconnect
            }
        }
    }

    private suspend fun handleMessage(msg: SignalingMessage) {
        when (msg.type) {
            "offer" -> {
                msg.sdp?.let { sdp ->
                    val isVideo = msg.isVideo ?: true
                    _callType.emit(isVideo) // Unblocks getCallType
                    _onOffer.emit(SessionDescription(SessionDescription.Type.OFFER, sdp) to isVideo)
                }
            }
            "answer" -> {
                msg.sdp?.let {
                    _onAnswer.emit(SessionDescription(SessionDescription.Type.ANSWER, it))
                }
            }
            "candidate" -> {
                msg.candidate?.let { model ->
                    if (processedCandidates.add(model.sdp)) {
                        _onCandidate.emit(IceCandidate(model.sdpMid, model.sdpMLineIndex, model.sdp))
                    }
                }
            }
            "callType" -> {
                _callType.emit(msg.isVideo ?: true)
            }
            "disconnect" -> {
                _onDisconnect.emit(Unit)
            }
        }
    }

    override fun sendOffer(sdp: SessionDescription, isVideo: Boolean) {
        scope.launch {
            session?.sendSerialized(SignalingMessage(
                type = "offer",
                roomId = roomId,
                sdp = sdp.description,
                isVideo = isVideo
            ))
        }
    }

    override fun sendAnswer(sdp: SessionDescription) {
        scope.launch {
            session?.sendSerialized(SignalingMessage(
                type = "answer",
                roomId = roomId,
                sdp = sdp.description
            ))
        }
    }

    override fun sendIceCandidate(candidate: IceCandidate, isCaller: Boolean) {
        scope.launch {
            session?.sendSerialized(SignalingMessage(
                type = "candidate",
                roomId = roomId,
                candidate = IceCandidateModel(candidate.sdp, candidate.sdpMid, candidate.sdpMLineIndex)
            ))
        }
    }

    override fun observeRoom(onOffer: (SessionDescription, Boolean) -> Unit) {
        scope.launch {
            _onOffer.collect { (sdp, isVideo) -> onOffer(sdp, isVideo) }
        }
    }

    override fun observeAnswer(onAnswer: (SessionDescription) -> Unit) {
        scope.launch {
            _onAnswer.collect { onAnswer(it) }
        }
    }

    override fun observeIceCandidates(isCaller: Boolean, onCandidate: (IceCandidate) -> Unit) {
        scope.launch {
            _onCandidate.collect { onCandidate(it) }
        }
    }

    override fun observeDisconnect(onDisconnected: () -> Unit) {
        scope.launch {
            _onDisconnect.collect { onDisconnected() }
        }
    }

    override fun getCallType(callback: (Boolean) -> Unit) {
        scope.launch {
            _callType.first().let { callback(it) }
        }
    }

    override fun markDisconnected() {
        scope.launch {
            session?.sendSerialized(SignalingMessage(type = "disconnect", roomId = roomId))
        }
    }

    override fun destroy() {
        scope.cancel()
        client.close()
    }
}

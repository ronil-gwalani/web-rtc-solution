package org.ron.webrtccall.manager

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import org.ron.webrtccall.models.CallState
import org.ron.webrtccall.models.IncomingCall

interface CallManager {
    val currentCall: StateFlow<CallState>
    val incomingCall: SharedFlow<IncomingCall>
    val cancelCall: SharedFlow<String>
    
    fun notifyIncomingCall(roomId: String, callerName: String, isAudioOnly: Boolean)
    fun notifyCancelCall(roomId: String)
    
    fun startCallUI(roomId: String, isAudioOnly: Boolean)
    fun answerCall()
    fun endCallUI()

    suspend fun startCall(targetUserId: String, isAudioOnly: Boolean): Result<String>
}

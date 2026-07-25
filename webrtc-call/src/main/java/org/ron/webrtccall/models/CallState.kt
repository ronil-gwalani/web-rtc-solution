package org.ron.webrtccall.models

sealed class CallState {
    object Idle : CallState()
    
    data class Incoming(
        val roomId: String,
        val callerName: String,
        val isAudioOnly: Boolean
    ) : CallState()
    
    data class Active(
        val roomId: String,
        val isCaller: Boolean,
        val isAudioOnly: Boolean
    ) : CallState()
}

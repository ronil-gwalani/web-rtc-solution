/**
 * Created by Ronil Gwalani
 * 
 */
package org.ron.webrtccall.manager

import android.content.Intent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import org.ron.webrtccall.models.CallState
import org.ron.webrtccall.models.IncomingCall

interface CallManager {

    suspend fun registerUser(userId: String, userName: String): Result<Unit>

    val isRegistered: Flow<Boolean>

    val currentCall: StateFlow<CallState>
    val incomingCall: SharedFlow<IncomingCall>
    val cancelCall: SharedFlow<String>

    fun notifyIncomingCall(roomId: String, callerName: String, isAudioOnly: Boolean)
    fun notifyCancelCall(roomId: String)

    fun startCallUI(roomId: String, isAudioOnly: Boolean)
    fun answerCall()
    fun endCallUI()

    suspend fun startCall(targetUserId: String, isAudioOnly: Boolean): Result<String>

    fun handleIntent(intent: Intent)


}

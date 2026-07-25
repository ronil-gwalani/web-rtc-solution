/**
 * Created by Ronil Gwalani
 * 
 */
package org.ron.webrtccall.manager

import android.content.Intent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.ron.webrtccall.data.PreferenceProvider
import org.ron.webrtccall.models.CallState
import org.ron.webrtccall.models.IncomingCall
import org.ron.webrtccall.network.SignalingService
import org.ron.webrtccall.repository.UserRepository

class WebRtcCallManager(
    private val userRepository: UserRepository,
    private val signalingService: SignalingService,
    private val preferenceProvider: PreferenceProvider
) : CallManager {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _currentCall = MutableStateFlow<CallState>(CallState.Idle)
    override val currentCall: StateFlow<CallState> = _currentCall.asStateFlow()
    override suspend fun registerUser(
        userId: String,
        userName: String
    ): Result<Unit> {
        return userRepository.registerUser(userId, userName)
    }

    override val isRegistered: Flow<Boolean> = preferenceProvider.isRegistered

    private val _incomingCall = MutableSharedFlow<IncomingCall>(extraBufferCapacity = 1)
    override val incomingCall: SharedFlow<IncomingCall> = _incomingCall.asSharedFlow()

    private val _cancelCall = MutableSharedFlow<String>(extraBufferCapacity = 1)
    override val cancelCall: SharedFlow<String> = _cancelCall.asSharedFlow()

    override fun notifyIncomingCall(roomId: String, callerName: String, isAudioOnly: Boolean) {
        scope.launch {
            _currentCall.value = CallState.Incoming(roomId, callerName, isAudioOnly)
            _incomingCall.emit(IncomingCall(roomId, callerName, isAudioOnly))
        }
    }

    override fun notifyCancelCall(roomId: String) {
        scope.launch {
            if (_currentCall.value is CallState.Incoming && (_currentCall.value as CallState.Incoming).roomId == roomId) {
                _currentCall.value = CallState.Idle
            }
            _cancelCall.emit(roomId)
        }
    }

    override fun startCallUI(roomId: String, isAudioOnly: Boolean) {
        _currentCall.value = CallState.Active(roomId, isCaller = true, isAudioOnly = isAudioOnly)
    }

    override fun answerCall() {
        val current = _currentCall.value
        if (current is CallState.Incoming) {
            _currentCall.value = CallState.Active(
                current.roomId,
                isCaller = false,
                isAudioOnly = current.isAudioOnly
            )
        }
    }

    override fun endCallUI() {
        _currentCall.value = CallState.Idle
    }

    override suspend fun startCall(targetUserId: String, isAudioOnly: Boolean): Result<String> {
        val myId =
            preferenceProvider.userId.first() ?: return Result.failure(Exception("Not registered"))
        val myName = preferenceProvider.userName.first() ?: "Unknown"

        return userRepository.getTargetUserToken(targetUserId).fold(
            onSuccess = { targetToken ->
                val roomId =
                    if (myId < targetUserId) "${myId}_$targetUserId" else "${targetUserId}_$myId"
                signalingService.sendCallNotification(
                    targetToken = targetToken,
                    callerId = myId,
                    callerName = myName,
                    roomId = roomId,
                    isAudioOnly = isAudioOnly
                ).map { roomId }
            },
            onFailure = { Result.failure(it) }
        )
    }

    override fun handleIntent(intent: Intent) {
        val action = intent.getStringExtra("action")
        if (intent.getBooleanExtra("isIncomingCall", false)) {
            val rid = intent.getStringExtra("roomId") ?: ""
            if (rid.isNotEmpty()) {
                val isAudioOnly = intent.getBooleanExtra("isAudioOnly", false)
                val callerName = intent.getStringExtra("callerName") ?: "Unknown"

                if (action == "answer") {
                    notifyIncomingCall(rid, callerName, isAudioOnly)
                    answerCall()
                } else {
                    notifyIncomingCall(rid, callerName, isAudioOnly)
                }
            }
        }

    }
}

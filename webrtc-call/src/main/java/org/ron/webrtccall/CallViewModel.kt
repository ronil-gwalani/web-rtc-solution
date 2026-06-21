package org.ron.webrtccall

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.webrtc.VideoTrack
import org.webrtc.EglBase

class CallViewModel(application: Application) : AndroidViewModel(application) {

    private var sessionManager: WebRtcSessionManager? = null
    private var signaling: WebRtcSignaling? = null
    private var isEndingCall = false
    private var timerJob: Job? = null
    private val proximityManager = ProximityManager(application)

    private val _localTrack = MutableStateFlow<VideoTrack?>(null)
    val localTrack: StateFlow<VideoTrack?> = _localTrack.asStateFlow()

    private val _remoteTrack = MutableStateFlow<VideoTrack?>(null)
    val remoteTrack: StateFlow<VideoTrack?> = _remoteTrack.asStateFlow()

    private val _eglContext = MutableStateFlow<EglBase.Context?>(null)
    val eglContext: StateFlow<EglBase.Context?> = _eglContext.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isSpeakerOn = MutableStateFlow(false)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn.asStateFlow()

    private val _isVideoEnabled = MutableStateFlow(true)
    val isVideoEnabled: StateFlow<Boolean> = _isVideoEnabled.asStateFlow()

    private val _isCalling = MutableStateFlow(false)
    val isCalling: StateFlow<Boolean> = _isCalling.asStateFlow()

    private val _isAudioOnly = MutableStateFlow(false)
    val isAudioOnly: StateFlow<Boolean> = _isAudioOnly.asStateFlow()

    private val _callDuration = MutableStateFlow("Connecting...")
    val callDuration: StateFlow<String> = _callDuration.asStateFlow()

    private var currentRoomId: String? = null

    fun initCall(roomId: String, isCaller: Boolean, isAudioOnly: Boolean) {
        if (_isCalling.value && currentRoomId == roomId) return
        
        isEndingCall = false
        _isCalling.value = true
        _callDuration.value = "Connecting..."
        currentRoomId = roomId
        
        signaling = FirebaseSignaling(roomId)
        
        if (isCaller) {
            _isAudioOnly.value = isAudioOnly
            _isVideoEnabled.value = !isAudioOnly
            _isSpeakerOn.value = !isAudioOnly
            
            signaling?.destroy()
            signaling = FirebaseSignaling(roomId)
            
            startSession(isCaller, !isAudioOnly)
        } else {
            signaling?.getCallType { isVideo ->
                _isAudioOnly.value = !isVideo
                _isVideoEnabled.value = isVideo
                _isSpeakerOn.value = isVideo
                startSession(isCaller, isVideo)
            }
        }
    }

    private fun startSession(isCaller: Boolean, isVideo: Boolean) {
        val signalingClient = signaling ?: return
        
        signalingClient.observeDisconnect {
            if (!isEndingCall) endCall()
        }

        sessionManager = WebRtcSessionManager(
            context = getApplication(),
            signaling = signalingClient,
            onLocalTrack = { track ->
                _localTrack.value = track
            },
            onRemoteTrack = { track ->
                _remoteTrack.value = track
            },
            onConnectionEstablished = {
                startTimer()
            },
            onConnectionClosed = { 
                if (!isEndingCall) endCall()
            }
        )
        
        _eglContext.value = sessionManager?.eglContext
        updateProximitySensor()

        if (isCaller) {
            sessionManager?.startCall(isVideo)
        } else {
            sessionManager?.joinCall(isVideo)
        }
    }

    private fun startTimer() {
        if (timerJob != null) return
        timerJob = viewModelScope.launch {
            var seconds = 0
            while (true) {
                val minutes = seconds / 60
                val remainingSeconds = seconds % 60
                _callDuration.value = String.format("%02d:%02d", minutes, remainingSeconds)
                delay(1000)
                seconds++
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun toggleMic() {
        _isMuted.value = !_isMuted.value
        sessionManager?.toggleMic(_isMuted.value)
    }

    fun toggleSpeaker() {
        _isSpeakerOn.value = !_isSpeakerOn.value
        sessionManager?.setSpeaker(_isSpeakerOn.value)
        updateProximitySensor()
    }

    fun toggleVideo() {
        _isVideoEnabled.value = !_isVideoEnabled.value
        sessionManager?.toggleVideo(_isVideoEnabled.value)
        updateProximitySensor()
    }

    fun switchCamera() {
        sessionManager?.switchCamera()
    }

    private fun updateProximitySensor() {
        if (_isCalling.value && (_isAudioOnly.value || !_isSpeakerOn.value)) {
            proximityManager.activate()
        } else {
            proximityManager.deactivate()
        }
    }

    fun endCall() {
        if (isEndingCall) return
        isEndingCall = true
        
        stopTimer()
        proximityManager.deactivate()
        
        sessionManager?.stopMedia()
        signaling?.markDisconnected()
        
        _isCalling.value = false
        currentRoomId = null
        
        viewModelScope.launch {
            delay(1000)
            sessionManager?.dispose()
            sessionManager = null
            
            _localTrack.value = null
            _remoteTrack.value = null
            _eglContext.value = null
            
            signaling?.destroy()
            signaling = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        endCall()
    }
}

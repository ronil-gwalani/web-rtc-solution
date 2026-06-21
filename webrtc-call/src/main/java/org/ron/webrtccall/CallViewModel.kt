package org.ron.webrtccall

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import org.webrtc.VideoTrack
import org.webrtc.EglBase
import java.util.*

class CallViewModel(application: Application) : AndroidViewModel(application) {

    private var sessionManager: WebRtcSessionManager? = null
    private var signaling: WebRtcSignaling? = null
    private var isEndingCall = false
    private var timer: Timer? = null

    private val _localTrack = mutableStateOf<VideoTrack?>(null)
    val localTrack: State<VideoTrack?> = _localTrack

    private val _remoteTrack = mutableStateOf<VideoTrack?>(null)
    val remoteTrack: State<VideoTrack?> = _remoteTrack

    private val _eglContext = mutableStateOf<EglBase.Context?>(null)
    val eglContext: State<EglBase.Context?> = _eglContext

    private val _isMuted = mutableStateOf(false)
    val isMuted: State<Boolean> = _isMuted

    private val _isSpeakerOn = mutableStateOf(false)
    val isSpeakerOn: State<Boolean> = _isSpeakerOn

    private val _isVideoEnabled = mutableStateOf(true)
    val isVideoEnabled: State<Boolean> = _isVideoEnabled

    private val _isCalling = mutableStateOf(false)
    val isCalling: State<Boolean> = _isCalling

    private val _isAudioOnly = mutableStateOf(false)
    val isAudioOnly: State<Boolean> = _isAudioOnly

    private val _callDuration = mutableStateOf("Connecting...")
    val callDuration: State<String> = _callDuration

    fun initCall(roomId: String, isCaller: Boolean, isAudioOnly: Boolean) {
        if (_isCalling.value) return
        isEndingCall = false
        _callDuration.value = "Connecting..."
        
        signaling = FirebaseSignaling(roomId)
        
        if (isCaller) {
            _isAudioOnly.value = isAudioOnly
            _isVideoEnabled.value = !isAudioOnly
            _isSpeakerOn.value = !isAudioOnly
            
            // Clean up old session data
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
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                if (!isEndingCall) endCall()
            }
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
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    if (!isEndingCall) endCall()
                }
            }
        )
        
        _eglContext.value = sessionManager?.eglContext
        _isCalling.value = true

        if (isCaller) {
            sessionManager?.startCall(isVideo)
        } else {
            sessionManager?.joinCall(isVideo)
        }
    }

    private fun startTimer() {
        if (timer != null) return
        var seconds = 0
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                seconds++
                val minutes = seconds / 60
                val remainingSeconds = seconds % 60
                _callDuration.value = String.format("%02d:%02d", minutes, remainingSeconds)
            }
        }, 0, 1000)
    }

    private fun stopTimer() {
        timer?.cancel()
        timer = null
    }

    fun toggleMic() {
        _isMuted.value = !_isMuted.value
        sessionManager?.toggleMic(_isMuted.value)
    }

    fun toggleSpeaker() {
        _isSpeakerOn.value = !_isSpeakerOn.value
        sessionManager?.setSpeaker(_isSpeakerOn.value)
    }

    fun toggleVideo() {
        _isVideoEnabled.value = !_isVideoEnabled.value
        sessionManager?.toggleVideo(_isVideoEnabled.value)
    }

    fun switchCamera() {
        sessionManager?.switchCamera()
    }

    fun endCall() {
        if (isEndingCall) return
        isEndingCall = true
        
        stopTimer()
        
        // 1. Stop data flow immediately to prevent crashes and frozen frames
        sessionManager?.stopMedia()
        
        // 2. Signal disconnection to peer
        signaling?.markDisconnected()
        
        // 3. Close the UI screen
        _isCalling.value = false
        
        // 4. Detailed cleanup after the UI has had time to release views
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            sessionManager?.dispose()
            sessionManager = null
            
            _localTrack.value = null
            _remoteTrack.value = null
            _eglContext.value = null
            
            signaling?.destroy()
            signaling = null
        }, 1000)
    }

    override fun onCleared() {
        super.onCleared()
        endCall()
    }
}

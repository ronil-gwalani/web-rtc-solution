package org.ron.webrtccall.webrtc

import android.content.Context
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import org.webrtc.*
import org.ron.webrtccall.network.WebRtcSignaling
import java.util.concurrent.Executors

class WebRtcSessionManager(
    private val context: Context,
    private val signaling: WebRtcSignaling,
    private val peerConnectionProvider: WebRtcPeerConnectionProvider,
    private val onLocalTrack: (VideoTrack) -> Unit,
    private val onRemoteTrack: (VideoTrack) -> Unit,
    private val onConnectionEstablished: () -> Unit,
    private val onConnectionClosed: () -> Unit
) : SessionManager {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val executor = Executors.newSingleThreadExecutor()
    private val rootEglBase = EglBase.create()
    override val eglContext: EglBase.Context get() = rootEglBase.eglBaseContext

    private var _peerConnectionFactory: PeerConnectionFactory? = null
    private val peerConnectionFactory: PeerConnectionFactory
        get() {
            if (_peerConnectionFactory == null) {
                _peerConnectionFactory = peerConnectionProvider.createPeerConnectionFactory(eglContext)
            }
            return _peerConnectionFactory!!
        }

    private var peerConnection: PeerConnection? = null
    private var videoCapturer: CameraVideoCapturer? = null
    private var localVideoSource: VideoSource? = null
    private var localVideoTrack: VideoTrack? = null
    private var localAudioTrack: AudioTrack? = null
    
    private var isCaller = false
    private var isDisposed = false
    private var hasRemoteDescriptionSet = false
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    override fun startCall(isVideo: Boolean) {
        isCaller = true
        initSession(isVideo)
        
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        }

        peerConnection?.createOffer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(p0: SessionDescription?) {
                peerConnection?.setLocalDescription(object : SimpleSdpObserver() {
                    override fun onSetSuccess() {
                        p0?.let { signaling.sendOffer(it, isVideo) }
                    }
                }, p0)
            }
        }, constraints)

        signaling.observeAnswer { sdp ->
            if (!hasRemoteDescriptionSet) {
                hasRemoteDescriptionSet = true
                peerConnection?.setRemoteDescription(SimpleSdpObserver(), sdp)
            }
        }
        observeIceCandidates()
    }

    override fun joinCall(isVideo: Boolean) {
        isCaller = false
        initSession(isVideo)
        
        signaling.observeRoom { offer, _ ->
            if (!hasRemoteDescriptionSet) {
                hasRemoteDescriptionSet = true
                peerConnection?.setRemoteDescription(object : SimpleSdpObserver() {
                    override fun onSetSuccess() {
                        val constraints = MediaConstraints().apply {
                            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
                            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
                        }
                        peerConnection?.createAnswer(object : SimpleSdpObserver() {
                            override fun onCreateSuccess(p0: SessionDescription?) {
                                peerConnection?.setLocalDescription(object : SimpleSdpObserver() {
                                    override fun onSetSuccess() {
                                        p0?.let { signaling.sendAnswer(it) }
                                    }
                                }, p0)
                            }
                        }, constraints)
                    }
                }, offer)
            }
        }
        observeIceCandidates()
    }

    private fun initSession(isVideo: Boolean) {
        setupPeerConnection()
        setupAudio()
        if (isVideo) {
            setupVideo()
            setSpeaker(true)
        } else {
            setSpeaker(false)
        }
    }

    private fun setupPeerConnection() {
        peerConnection = peerConnectionProvider.createPeerConnection(peerConnectionFactory, object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate?) {
                candidate?.let { signaling.sendIceCandidate(it, isCaller) }
            }
            override fun onTrack(transceiver: RtpTransceiver?) {
                val track = transceiver?.receiver?.track()
                if (track is VideoTrack) {
                    mainHandler.post { onRemoteTrack(track) }
                }
            }
            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
                when (state) {
                    PeerConnection.IceConnectionState.CONNECTED -> {
                        mainHandler.post { onConnectionEstablished() }
                    }
                    PeerConnection.IceConnectionState.DISCONNECTED,
                    PeerConnection.IceConnectionState.FAILED,
                    PeerConnection.IceConnectionState.CLOSED -> {
                        mainHandler.post { onConnectionClosed() }
                    }
                    else -> {}
                }
            }
            override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
            override fun onAddStream(p0: MediaStream?) {}
            override fun onRemoveStream(p0: MediaStream?) {}
            override fun onDataChannel(p0: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
        })
    }

    private fun setupAudio() {
        val source = peerConnectionProvider.createAudioSource(peerConnectionFactory)
        localAudioTrack = peerConnectionProvider.createAudioTrack(peerConnectionFactory, source)
        peerConnection?.addTrack(localAudioTrack, listOf("local_stream"))
    }

    private fun setupVideo() {
        val enumerator = Camera2Enumerator(context)
        val deviceName = enumerator.deviceNames.firstOrNull { enumerator.isFrontFacing(it) } ?: return
        
        videoCapturer = enumerator.createCapturer(deviceName, null)
        localVideoSource = peerConnectionProvider.createVideoSource(peerConnectionFactory, false)
        
        val surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglContext)
        videoCapturer?.initialize(surfaceTextureHelper, context, localVideoSource?.capturerObserver)
        videoCapturer?.startCapture(1280, 720, 30)

        localVideoTrack = peerConnectionProvider.createVideoTrack(peerConnectionFactory, localVideoSource!!)
        peerConnection?.addTrack(localVideoTrack, listOf("local_stream"))
        
        localVideoTrack?.let { track ->
            mainHandler.post { onLocalTrack(track) }
        }
    }

    private fun observeIceCandidates() {
        signaling.observeIceCandidates(isCaller) { candidate ->
            peerConnection?.addIceCandidate(candidate)
        }
    }

    override fun toggleMic(isMuted: Boolean) {
        localAudioTrack?.setEnabled(!isMuted)
    }

    override fun toggleVideo(isEnabled: Boolean) {
        localVideoTrack?.setEnabled(isEnabled)
    }

    override fun setSpeaker(isOn: Boolean) {
        audioManager.isSpeakerphoneOn = isOn
        audioManager.mode = if (isOn) AudioManager.MODE_IN_COMMUNICATION else AudioManager.MODE_IN_CALL
    }

    override fun switchCamera() {
        videoCapturer?.switchCamera(object : CameraVideoCapturer.CameraSwitchHandler {
            override fun onCameraSwitchDone(isFront: Boolean) {}
            override fun onCameraSwitchError(p0: String?) {}
        })
    }

    override fun stopMedia() {
        try {
            videoCapturer?.stopCapture()
        } catch (e: Exception) {}
        localVideoTrack?.setEnabled(false)
        localAudioTrack?.setEnabled(false)
    }

    override fun dispose() {
        if (isDisposed) return
        isDisposed = true
        
        mainHandler.removeCallbacksAndMessages(null)
        
        executor.execute {
            try {
                videoCapturer?.stopCapture()
                videoCapturer?.dispose()
                videoCapturer = null
                
                localVideoTrack = null
                localAudioTrack = null
                
                localVideoSource?.dispose()
                localVideoSource = null
                
                peerConnection?.close()
                peerConnection = null
                
                _peerConnectionFactory?.dispose()
                _peerConnectionFactory = null

                rootEglBase.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private open class SimpleSdpObserver : SdpObserver {
        override fun onCreateSuccess(p0: SessionDescription?) {}
        override fun onSetSuccess() {}
        override fun onCreateFailure(p0: String?) {}
        override fun onSetFailure(p0: String?) {}
    }
}

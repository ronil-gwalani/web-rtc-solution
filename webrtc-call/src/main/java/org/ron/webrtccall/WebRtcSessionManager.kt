package org.ron.webrtccall

import android.content.Context
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import org.webrtc.*
import java.util.concurrent.Executors

class WebRtcSessionManager(
    private val context: Context,
    private val signaling: WebRtcSignaling,
    private val onLocalTrack: (VideoTrack) -> Unit,
    private val onRemoteTrack: (VideoTrack) -> Unit,
    private val onConnectionEstablished: () -> Unit,
    private val onConnectionClosed: () -> Unit
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val executor = Executors.newSingleThreadExecutor()
    private val rootEglBase = EglBase.create()
    val eglContext: EglBase.Context get() = rootEglBase.eglBaseContext

    private val peerConnectionFactory: PeerConnectionFactory by lazy {
        val videoEncoderFactory = DefaultVideoEncoderFactory(eglContext, true, true)
        val videoDecoderFactory = DefaultVideoDecoderFactory(eglContext)
        PeerConnectionFactory.builder()
            .setVideoEncoderFactory(videoEncoderFactory)
            .setVideoDecoderFactory(videoDecoderFactory)
            .createPeerConnectionFactory()
    }

    private var peerConnection: PeerConnection? = null
    private var videoCapturer: CameraVideoCapturer? = null
    private var localVideoSource: VideoSource? = null
    private var localVideoTrack: VideoTrack? = null
    private var localAudioTrack: AudioTrack? = null
    
    private var isCaller = false
    private var isFrontCamera = true
    private var isDisposed = false
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    init {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .createInitializationOptions()
        )
    }

    fun startCall(isVideo: Boolean) {
        isCaller = true
        initSession(isVideo)
        
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", if (isVideo) "true" else "false"))
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
            peerConnection?.setRemoteDescription(SimpleSdpObserver(), sdp)
        }
        observeIceCandidates()
    }

    fun joinCall(isVideo: Boolean) {
        isCaller = false
        initSession(isVideo)
        
        signaling.observeRoom { offer, _ ->
            peerConnection?.setRemoteDescription(object : SimpleSdpObserver() {
                override fun onSetSuccess() {
                    peerConnection?.createAnswer(object : SimpleSdpObserver() {
                        override fun onCreateSuccess(p0: SessionDescription?) {
                            peerConnection?.setLocalDescription(object : SimpleSdpObserver() {
                                override fun onSetSuccess() {
                                    p0?.let { signaling.sendAnswer(it) }
                                }
                            }, p0)
                        }
                    }, MediaConstraints())
                }
            }, offer)
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
        val rtcConfig = PeerConnection.RTCConfiguration(listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        )).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        }

        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
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
        val source = peerConnectionFactory.createAudioSource(MediaConstraints())
        localAudioTrack = peerConnectionFactory.createAudioTrack("audio_track", source)
        peerConnection?.addTrack(localAudioTrack, listOf("local_stream"))
    }

    private fun setupVideo() {
        val enumerator = Camera2Enumerator(context)
        val deviceName = enumerator.deviceNames.firstOrNull { enumerator.isFrontFacing(it) } ?: return
        
        videoCapturer = enumerator.createCapturer(deviceName, null)
        localVideoSource = peerConnectionFactory.createVideoSource(false)
        
        val surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglContext)
        videoCapturer?.initialize(surfaceTextureHelper, context, localVideoSource?.capturerObserver)
        videoCapturer?.startCapture(1280, 720, 30)

        localVideoTrack = peerConnectionFactory.createVideoTrack("video_track", localVideoSource)
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

    fun toggleMic(isMuted: Boolean) {
        localAudioTrack?.setEnabled(!isMuted)
    }

    fun toggleVideo(isEnabled: Boolean) {
        localVideoTrack?.setEnabled(isEnabled)
    }

    fun setSpeaker(isOn: Boolean) {
        audioManager.isSpeakerphoneOn = isOn
        audioManager.mode = if (isOn) AudioManager.MODE_IN_COMMUNICATION else AudioManager.MODE_IN_CALL
    }

    fun switchCamera() {
        videoCapturer?.switchCamera(object : CameraVideoCapturer.CameraSwitchHandler {
            override fun onCameraSwitchDone(isFront: Boolean) { isFrontCamera = isFront }
            override fun onCameraSwitchError(p0: String?) {}
        })
    }

    /**
     * Step 1 of Disconnect: Stop all active hardware and data flow.
     * This should be called on the Main Thread.
     */
    fun stopMedia() {
        try {
            videoCapturer?.stopCapture()
        } catch (e: Exception) {}
        localVideoTrack?.setEnabled(false)
        localAudioTrack?.setEnabled(false)
    }

    /**
     * Step 2 of Disconnect: Fully dispose WebRTC native objects.
     * This is offloaded to a background thread to prevent UI freezing.
     */
    fun dispose() {
        if (isDisposed) return
        isDisposed = true
        
        mainHandler.removeCallbacksAndMessages(null)
        
        executor.execute {
            try {
                videoCapturer?.dispose()
                videoCapturer = null
                
                // Note: localVideoTrack and localAudioTrack are disposed when PeerConnection closes,
                // but we null them out to prevent further use.
                localVideoTrack = null
                localAudioTrack = null
                
                localVideoSource?.dispose()
                localVideoSource = null
                
                peerConnection?.close()
                peerConnection = null
                
                peerConnectionFactory.dispose()
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

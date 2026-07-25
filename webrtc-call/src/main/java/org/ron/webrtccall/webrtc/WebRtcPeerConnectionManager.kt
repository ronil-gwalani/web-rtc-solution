/**
 * Created by Ronil Gwalani
 * 
 */
package org.ron.webrtccall.webrtc

import android.content.Context
import org.webrtc.*

class WebRtcPeerConnectionManager(private val context: Context) : WebRtcPeerConnectionProvider {

    init {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .createInitializationOptions()
        )
    }

    override fun createPeerConnectionFactory(eglContext: EglBase.Context): PeerConnectionFactory {
        val videoEncoderFactory = DefaultVideoEncoderFactory(eglContext, true, true)
        val videoDecoderFactory = DefaultVideoDecoderFactory(eglContext)
        return PeerConnectionFactory.builder()
            .setVideoEncoderFactory(videoEncoderFactory)
            .setVideoDecoderFactory(videoDecoderFactory)
            .createPeerConnectionFactory()
    }

    override fun createPeerConnection(
        factory: PeerConnectionFactory,
        observer: PeerConnection.Observer
    ): PeerConnection? {
        val rtcConfig = PeerConnection.RTCConfiguration(listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        )).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        }
        return factory.createPeerConnection(rtcConfig, observer)
    }

    override fun createAudioSource(factory: PeerConnectionFactory): AudioSource {
        return factory.createAudioSource(MediaConstraints())
    }

    override fun createVideoSource(factory: PeerConnectionFactory, isScreencast: Boolean): VideoSource {
        return factory.createVideoSource(isScreencast)
    }

    override fun createAudioTrack(factory: PeerConnectionFactory, source: AudioSource): AudioTrack {
        return factory.createAudioTrack("audio_track", source)
    }

    override fun createVideoTrack(factory: PeerConnectionFactory, source: VideoSource): VideoTrack {
        return factory.createVideoTrack("video_track", source)
    }
}

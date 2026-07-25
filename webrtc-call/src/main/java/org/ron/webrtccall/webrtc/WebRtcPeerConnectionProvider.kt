package org.ron.webrtccall.webrtc

import org.webrtc.*

interface WebRtcPeerConnectionProvider {
    fun createPeerConnectionFactory(eglContext: EglBase.Context): PeerConnectionFactory
    fun createPeerConnection(factory: PeerConnectionFactory, observer: PeerConnection.Observer): PeerConnection?
    fun createAudioSource(factory: PeerConnectionFactory): AudioSource
    fun createVideoSource(factory: PeerConnectionFactory, isScreencast: Boolean): VideoSource
    fun createAudioTrack(factory: PeerConnectionFactory, source: AudioSource): AudioTrack
    fun createVideoTrack(factory: PeerConnectionFactory, source: VideoSource): VideoTrack
}

package org.ron.webrtccall.webrtc

import org.ron.webrtccall.network.WebRtcSignaling
import org.webrtc.VideoTrack

interface SessionManagerFactory {
    fun create(
        signaling: WebRtcSignaling,
        onLocalTrack: (VideoTrack) -> Unit,
        onRemoteTrack: (VideoTrack) -> Unit,
        onConnectionEstablished: () -> Unit,
        onConnectionClosed: () -> Unit
    ): SessionManager
}

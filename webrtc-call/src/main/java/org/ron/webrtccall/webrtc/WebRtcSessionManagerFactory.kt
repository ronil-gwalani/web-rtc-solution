package org.ron.webrtccall.webrtc

import android.content.Context
import org.ron.webrtccall.network.WebRtcSignaling
import org.webrtc.VideoTrack

class WebRtcSessionManagerFactory(
    private val context: Context,
    private val peerConnectionProvider: WebRtcPeerConnectionProvider
) : SessionManagerFactory {
    override fun create(
        signaling: WebRtcSignaling,
        onLocalTrack: (VideoTrack) -> Unit,
        onRemoteTrack: (VideoTrack) -> Unit,
        onConnectionEstablished: () -> Unit,
        onConnectionClosed: () -> Unit
    ): SessionManager {
        return WebRtcSessionManager(
            context,
            signaling,
            peerConnectionProvider,
            onLocalTrack,
            onRemoteTrack,
            onConnectionEstablished,
            onConnectionClosed
        )
    }
}

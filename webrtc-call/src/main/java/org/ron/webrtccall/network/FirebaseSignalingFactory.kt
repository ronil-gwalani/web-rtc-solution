package org.ron.webrtccall.network

class FirebaseSignalingFactory : SignalingClientFactory {
    override fun create(roomId: String): WebRtcSignaling {
        return FirebaseSignaling(roomId)
    }
}

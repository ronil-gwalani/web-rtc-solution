package org.ron.webrtccall

internal object SignalingFactory {
    enum class Type {
        KTOR, FIREBASE
    }

    /**
     * Set this to change the signaling provider globally
     */
    var signalingType = Type.KTOR

    fun create(roomId: String): WebRtcSignaling {
        return when (signalingType) {
            Type.KTOR -> KtorSignaling(roomId)
            Type.FIREBASE -> {
                // Return FirebaseSignaling(roomId) here when implemented/restored
                // For now, throw an error if Firebase is selected but not available
                throw IllegalStateException("FirebaseSignaling is not currently implemented in this version. Please use Type.KTOR.")
            }
        }
    }
}

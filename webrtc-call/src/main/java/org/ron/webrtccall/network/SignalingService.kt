/**
 * Created by Ronil Gwalani
 * 
 */
package org.ron.webrtccall.network

interface SignalingService {
    suspend fun sendCallNotification(
        targetToken: String,
        callerId: String,
        callerName: String,
        roomId: String,
        isAudioOnly: Boolean
    ): Result<Unit>
}

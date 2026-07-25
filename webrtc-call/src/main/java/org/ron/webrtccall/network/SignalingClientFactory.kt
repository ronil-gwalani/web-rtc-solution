/**
 * Created by Ronil Gwalani
 * 
 */
package org.ron.webrtccall.network

interface SignalingClientFactory {
    fun create(roomId: String): WebRtcSignaling
}

package org.ron.webrtccall.network

import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

interface WebRtcSignaling {
    fun sendOffer(sdp: SessionDescription, isVideo: Boolean)
    fun sendAnswer(sdp: SessionDescription)
    fun sendIceCandidate(candidate: IceCandidate, isCaller: Boolean)
    
    fun observeRoom(onOffer: (SessionDescription, Boolean) -> Unit)
    fun observeAnswer(onAnswer: (SessionDescription) -> Unit)
    fun observeIceCandidates(isCaller: Boolean, onCandidate: (IceCandidate) -> Unit)
    fun observeDisconnect(onDisconnected: () -> Unit)
    fun observeRejection(onRejected: () -> Unit)
    
    fun getCallType(callback: (Boolean) -> Unit)
    fun markDisconnected()
    fun rejectCall()
    fun destroy()
}

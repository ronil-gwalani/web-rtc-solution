package org.ron.webrtccall

import kotlinx.serialization.Serializable

@Serializable
data class SignalingMessage(
    val type: String,
    val roomId: String,
    val data: String? = null,
    val isVideo: Boolean? = null,
    val sdp: String? = null,
    val candidate: IceCandidateModel? = null
)

@Serializable
data class IceCandidateModel(
    val sdp: String,
    val sdpMid: String,
    val sdpMLineIndex: Int
)

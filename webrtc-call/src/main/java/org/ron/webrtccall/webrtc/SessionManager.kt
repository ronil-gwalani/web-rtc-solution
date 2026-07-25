/**
 * Created by Ronil Gwalani
 * 
 */
package org.ron.webrtccall.webrtc

import org.webrtc.EglBase
import org.webrtc.VideoTrack

interface SessionManager {
    val eglContext: EglBase.Context
    fun startCall(isVideo: Boolean)
    fun joinCall(isVideo: Boolean)
    fun toggleMic(isMuted: Boolean)
    fun toggleVideo(isEnabled: Boolean)
    fun setSpeaker(isOn: Boolean)
    fun switchCamera()
    fun stopMedia()
    fun dispose()
}

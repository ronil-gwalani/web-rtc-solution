/**
 * Created by Ronil Gwalani
 * 
 */
package org.ron.webrtccall.models

data class IncomingCall(val roomId: String, val callerName: String, val isAudioOnly: Boolean)

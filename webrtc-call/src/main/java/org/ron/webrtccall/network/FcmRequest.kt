/**
 * Created by Ronil Gwalani
 * 
 */
package org.ron.webrtccall.network

data class FcmRequest(
    var message: RonPushNewNotificationMessageModel? = null,
)

data class RonPushNewNotificationMessageModel(
    var token: String? = null,
    var data: Map<String, String>? = null,
    var android: Message = Message()
)

data class Message(
    var priority: String = "high",
)

data class FcmResponse(
    val name: String
)

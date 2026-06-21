package org.ron.webrtccall

object SignalingConfig {
    /**
     * The IP address or hostname of your Ktor signaling server.
     * - Use "10.0.2.2" for Android Emulator to connect to localhost.
     * - Use your machine's local IP (e.g., "192.168.1.50") for physical devices.
     */
//    const val HOST = "10.225.7.191"
    const val HOST = "10.0.2.2"


    /**
     * The port your Ktor server is listening on.
     */
    const val PORT = 8080

    /**
     * Full WebSocket URL
     */
    fun getUrl(roomId: String): String = "ws://$HOST:$PORT/call/$roomId"
}

package org.ron.webrtccall.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap
import java.util.Collections

// Room state class to mimic Firebase persistent behavior
class Room(val roomId: String) {
    val participants = Collections.synchronizedSet(LinkedHashSet<DefaultWebSocketServerSession>())
    var offer: String? = null
    var isVideo: Boolean? = null
    var answer: String? = null
    val callerCandidates = Collections.synchronizedList(mutableListOf<IceCandidateModel>())
    val calleeCandidates = Collections.synchronizedList(mutableListOf<IceCandidateModel>())
}

private val rooms = ConcurrentHashMap<String, Room>()

fun Application.configureSignalingRouting() {
    routing {
        // Health check web page
        get("/") {
            call.respondText(getHealthCheckHtml(), ContentType.Text.Html)
        }

        // WebRTC Signaling WebSocket
        webSocket("/call/{roomId}") {
            val roomId = call.parameters["roomId"] ?: return@webSocket
            
            val room = rooms.computeIfAbsent(roomId) { Room(roomId) }
            val participants = room.participants
            
            if (participants.size >= 2) {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Room is full"))
                return@webSocket
            }

            participants.add(this)
            println("Client joined room: $roomId. Total: ${participants.size}")

            try {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        val msg = Json.decodeFromString<SignalingMessage>(text)
                        handleSignalingMessage(room, this, msg, text)
                    }
                }
            } catch (e: Exception) {
                println("Error in room $roomId: ${e.message}")
            } finally {
                participants.remove(this)
                println("Client left room: $roomId. Total: ${participants.size}")
                
                if (participants.isNotEmpty()) {
                    val disconnectMsg = Json.encodeToString(SignalingMessage(type = "disconnect", roomId = roomId))
                    participants.forEach { session ->
                        try {
                            session.send(disconnectMsg)
                        } catch (e: Exception) { }
                    }
                }

                if (participants.isEmpty()) {
                    rooms.remove(roomId)
                    println("Room $roomId deleted")
                }
            }
        }
    }
}

private suspend fun DefaultWebSocketServerSession.handleSignalingMessage(
    room: Room,
    currentSession: DefaultWebSocketServerSession,
    msg: SignalingMessage,
    rawText: String
) {
    when (msg.type) {
        "join" -> {
            // If there's an offer already, sync it to the new joiner (callee)
            room.offer?.let { offerSdp ->
                val syncOffer = SignalingMessage(
                    type = "offer",
                    roomId = room.roomId,
                    sdp = offerSdp,
                    isVideo = room.isVideo
                )
                currentSession.send(Json.encodeToString(syncOffer))
                
                // Also sync existing caller candidates
                room.callerCandidates.forEach { candidate ->
                    val syncCandidate = SignalingMessage(
                        type = "candidate",
                        roomId = room.roomId,
                        candidate = candidate
                    )
                    currentSession.send(Json.encodeToString(syncCandidate))
                }
            }
        }
        "offer" -> {
            room.offer = msg.sdp
            room.isVideo = msg.isVideo
            println("Offer received for room: ${room.roomId}. isVideo: ${room.isVideo}")
            forwardToOthers(room, currentSession, rawText)
        }
        "answer" -> {
            room.answer = msg.sdp
            println("Answer received for room: ${room.roomId}")
            forwardToOthers(room, currentSession, rawText)
        }
        "candidate" -> {
            msg.candidate?.let {
                // Store candidate based on who sent it
                if (room.participants.first() == currentSession) {
                    room.callerCandidates.add(it)
                } else {
                    room.calleeCandidates.add(it)
                }
            }
            forwardToOthers(room, currentSession, rawText)
        }
        else -> forwardToOthers(room, currentSession, rawText)
    }
}

private suspend fun forwardToOthers(room: Room, currentSession: DefaultWebSocketServerSession, text: String) {
    room.participants.forEach { session ->
        if (session != currentSession) {
            try {
                session.send(text)
            } catch (e: Exception) { }
        }
    }
}

package org.ron.webrtccall

import com.google.firebase.database.*
import com.google.gson.Gson
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

class FirebaseSignalingClient(private val roomId: String) {

    private val db = FirebaseDatabase.getInstance().getReference("rooms").child(roomId)
    private val gson = Gson()

    fun sendOffer(sdp: SessionDescription, isVideo: Boolean) {
        val data = mapOf(
            "offer" to gson.toJson(sdp),
            "type" to if (isVideo) "video" else "voice",
            "active" to true
        )
        db.updateChildren(data)
    }

    fun sendAnswer(sdp: SessionDescription) {
        db.child("answer").setValue(gson.toJson(sdp))
    }

    fun sendCandidate(candidate: IceCandidate, isCaller: Boolean) {
        val child = if (isCaller) "callerCandidates" else "calleeCandidates"
        db.child(child).push().setValue(gson.toJson(candidate))
    }

    // New specific method to get call type BEFORE joining fully
    fun getCallType(callback: (Boolean) -> Unit) {
        db.child("type").get().addOnSuccessListener { snapshot ->
            callback(snapshot.getValue(String::class.java) == "video")
        }.addOnFailureListener {
            callback(true) // Default to video if check fails
        }
    }

    fun observeRoom(onOfferReceived: (SessionDescription, Boolean) -> Unit) {
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val offerSdp = snapshot.child("offer").getValue(String::class.java)
                val type = snapshot.child("type").getValue(String::class.java) ?: "video"
                
                if (offerSdp != null) {
                    onOfferReceived(
                        gson.fromJson(offerSdp, SessionDescription::class.java),
                        type == "video"
                    )
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun observeAnswer(onAnswerReceived: (SessionDescription) -> Unit) {
        db.child("answer").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue(String::class.java)?.let {
                    onAnswerReceived(gson.fromJson(it, SessionDescription::class.java))
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun observeCandidates(isCaller: Boolean, onCandidateReceived: (IceCandidate) -> Unit) {
        val child = if (isCaller) "calleeCandidates" else "callerCandidates"
        db.child(child).addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                snapshot.getValue(String::class.java)?.let {
                    onCandidateReceived(gson.fromJson(it, IceCandidate::class.java))
                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun observeDisconnect(onDisconnected: () -> Unit) {
        db.child("active").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.getValue(Boolean::class.java) == false) {
                    onDisconnected()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun setInactive() {
        db.child("active").setValue(false)
    }

    fun clearRoom() {
        db.removeValue()
    }
}

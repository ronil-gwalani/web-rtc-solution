package org.ron.webrtccall

import com.google.firebase.database.*
import com.google.gson.Gson
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

internal class FirebaseSignaling(private val roomId: String) : WebRtcSignaling {

    private val db = FirebaseDatabase.getInstance().getReference("rooms").child(roomId)
    private val gson = Gson()
    private var isDisposed = false

    override fun sendOffer(sdp: SessionDescription, isVideo: Boolean) {
        val data = mapOf(
            "offer" to gson.toJson(sdp),
            "type" to if (isVideo) "video" else "voice",
            "active" to true
        )
        db.updateChildren(data)
    }

    override fun sendAnswer(sdp: SessionDescription) {
        db.child("answer").setValue(gson.toJson(sdp))
    }

    override fun sendIceCandidate(candidate: IceCandidate, isCaller: Boolean) {
        val child = if (isCaller) "callerCandidates" else "calleeCandidates"
        db.child(child).push().setValue(gson.toJson(candidate))
    }

    override fun getCallType(callback: (Boolean) -> Unit) {
        db.child("type").get().addOnSuccessListener {
            if (isDisposed) return@addOnSuccessListener
            callback(it.getValue(String::class.java) == "video")
        }
    }

    override fun observeRoom(onOffer: (SessionDescription, Boolean) -> Unit) {
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (isDisposed) return
                val offer = snapshot.child("offer").getValue(String::class.java)
                val type = snapshot.child("type").getValue(String::class.java) ?: "video"
                if (offer != null) {
                    onOffer(gson.fromJson(offer, SessionDescription::class.java), type == "video")
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun observeAnswer(onAnswer: (SessionDescription) -> Unit) {
        db.child("answer").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (isDisposed) return
                snapshot.getValue(String::class.java)?.let {
                    onAnswer(gson.fromJson(it, SessionDescription::class.java))
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun observeIceCandidates(isCaller: Boolean, onCandidate: (IceCandidate) -> Unit) {
        val child = if (isCaller) "calleeCandidates" else "callerCandidates"
        db.child(child).addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (isDisposed) return
                snapshot.getValue(String::class.java)?.let {
                    onCandidate(gson.fromJson(it, IceCandidate::class.java))
                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun observeDisconnect(onDisconnected: () -> Unit) {
        db.child("active").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (isDisposed) return
                if (snapshot.exists() && snapshot.getValue(Boolean::class.java) == false) {
                    onDisconnected()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun markDisconnected() {
        db.child("active").setValue(false)
    }

    override fun destroy() {
        isDisposed = true
        db.removeValue()
    }
}

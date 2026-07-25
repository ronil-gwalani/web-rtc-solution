/**
 * Created by Ronil Gwalani
 * 
 */
package org.ron.webrtccall

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.firebase.database.FirebaseDatabase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.ron.webrtccall.manager.CallManager

class CallActionReceiver : BroadcastReceiver(), KoinComponent {
    
    private val callManager: CallManager by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "ACTION_REJECT") {
            val roomId = intent.getStringExtra("roomId")
            if (roomId != null) {
                try {
                    FirebaseDatabase.getInstance().getReference("rooms").child(roomId).child("rejected").setValue(true)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(1001)
        }
    }
}

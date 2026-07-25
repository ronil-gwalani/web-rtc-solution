/**
 * Created by Ronil Gwalani
 * WebRTC Solution - Firebase Messaging Service for Calls
 */
package org.ron.webrtccall

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.ron.webrtccall.data.PreferenceProvider
import org.ron.webrtccall.manager.CallManager
import org.ron.webrtccall.utils.AppVisibilityTracker

class CallMessagingService : FirebaseMessagingService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    
    private val callManager: CallManager by inject()
    private val preferenceProvider: PreferenceProvider by inject()

    private val activeListeners = mutableMapOf<String, ValueEventListener>()

    @Deprecated("Deprecated in Java")
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        scope.launch {
            preferenceProvider.saveFcmToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("CallMessagingService", "Message received: ${message.data}")
        val data = message.data

        val action = data["action"]
        if (action == "reject") {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(1001)
            return
        }

        if (data["type"] == "incoming_call") {
            val callerName = data["callerName"] ?: "Unknown"
            val roomId = data["roomId"] ?: ""
            val isAudioOnly = data["isAudioOnly"]?.toBoolean() ?: false

            observeCallActiveState(roomId)

            if (AppVisibilityTracker.isAppInForeground) {
                // When in foreground, we notify the manager directly
                // and skip showing a notification to avoid redundancy
                callManager.notifyIncomingCall(roomId, callerName, isAudioOnly)
            } else {
                showIncomingCallNotification(
                    callerName = callerName,
                    roomId = roomId,
                    isAudioOnly = isAudioOnly
                )
            }
        }
    }

    private fun observeCallActiveState(roomId: String) {
        if (activeListeners.containsKey(roomId)) return

        val dbRef =
            FirebaseDatabase.getInstance().getReference("rooms").child(roomId).child("active")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.getValue(Boolean::class.java) == false) {
                    dismissCallUI(roomId)
                    dbRef.removeEventListener(this)
                    activeListeners.remove(roomId)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        dbRef.addValueEventListener(listener)
        activeListeners[roomId] = listener
    }

    private fun dismissCallUI(roomId: String) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1001)
        callManager.notifyCancelCall(roomId)
    }

    private fun showIncomingCallNotification(
        callerName: String,
        roomId: String,
        isAudioOnly: Boolean
    ) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "calls_channel_v3"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                channelId,
                "Incoming Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notification for incoming calls"
                enableLights(true)
                enableVibration(false)
                setSound(ringtoneUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val callType = if (isAudioOnly) "Voice Call" else "Video Call"
        val notificationTitle = "Incoming $callType"
        val notificationText = "from $callerName"

        val baseIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            putExtra("roomId", roomId)
            putExtra("isAudioOnly", isAudioOnly)
            putExtra("callerName", callerName)
            putExtra("isIncomingCall", true)
        } ?: Intent()

        val answerIntent = Intent(baseIntent).apply {
            putExtra("action", "answer")
        }
        val answerPendingIntent = PendingIntent.getActivity(
            this,
            101,
            answerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val rejectIntent = Intent(this, CallActionReceiver::class.java).apply {
            action = "ACTION_REJECT"
            putExtra("roomId", roomId)
        }
        val rejectPendingIntent = PendingIntent.getBroadcast(
            this,
            102,
            rejectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val fullScreenIntent = Intent(baseIntent).apply {
            putExtra("action", "show_incoming_ui")
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            103,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.sym_def_app_icon)
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVibrate(longArrayOf(0))
            .setOngoing(true)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(fullScreenPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE))
            .addAction(
                android.R.drawable.ic_menu_call,
                "Accept",
                answerPendingIntent
            ).addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Reject",
                rejectPendingIntent
            )
            .build()
            .apply {
                flags = flags or Notification.FLAG_INSISTENT
            }

        notificationManager.notify(1001, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}

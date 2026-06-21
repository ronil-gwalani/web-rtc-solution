package org.ron.webrtccall

import android.content.Context
import android.os.PowerManager
import android.util.Log

internal class ProximityManager(context: Context) {
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private var wakeLock: PowerManager.WakeLock? = null

    init {
        // Check if proximity screen off is supported
        try {
            if (powerManager.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)) {
                wakeLock = powerManager.newWakeLock(
                    PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                    "WebRTCSolution:ProximityWakeLock"
                )
            }
        } catch (e: Exception) {
            Log.e("ProximityManager", "Error initializing proximity wake lock", e)
        }
    }

    fun activate() {
        try {
            wakeLock?.let {
                if (!it.isHeld) {
                    it.acquire()
                    Log.d("ProximityManager", "Proximity wake lock acquired")
                }
            }
        } catch (e: Exception) {
            Log.e("ProximityManager", "Error acquiring proximity wake lock", e)
        }
    }

    fun deactivate() {
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                    Log.d("ProximityManager", "Proximity wake lock released")
                }
            }
        } catch (e: Exception) {
            Log.e("ProximityManager", "Error releasing proximity wake lock", e)
        }
    }
}

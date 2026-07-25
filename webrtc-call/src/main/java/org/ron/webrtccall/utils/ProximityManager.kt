/**
 * Created by Ronil Gwalani
 * 
 */
package org.ron.webrtccall.utils

import android.content.Context
import android.os.PowerManager
import android.util.Log

class ProximityManager(context: Context) : ProximitySensor {
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private var wakeLock: PowerManager.WakeLock? = null

    init {
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

    override fun activate() {
        try {
            wakeLock?.let {
                if (!it.isHeld) {
                    it.acquire()
                }
            }
        } catch (e: Exception) {
            Log.e("ProximityManager", "Error acquiring proximity wake lock", e)
        }
    }

    override fun deactivate() {
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
        } catch (e: Exception) {
            Log.e("ProximityManager", "Error releasing proximity wake lock", e)
        }
    }
}

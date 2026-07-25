/**
 * Created by Ronil Gwalani
 * WebRTC Solution - Application Visibility Tracker
 */
package org.ron.webrtccall.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle

object AppVisibilityTracker : Application.ActivityLifecycleCallbacks {
    private var activeActivities = 0
    val isAppInForeground: Boolean
        get() = activeActivities > 0

    override fun onActivityStarted(activity: Activity) {
        activeActivities++
    }

    override fun onActivityStopped(activity: Activity) {
        activeActivities--
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}

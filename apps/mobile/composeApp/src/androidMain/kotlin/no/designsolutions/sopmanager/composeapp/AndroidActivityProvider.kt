package no.designsolutions.sopmanager.composeapp

import android.app.Activity
import java.lang.ref.WeakReference

object AndroidActivityProvider {
    private var activityRef: WeakReference<Activity>? = null

    fun setCurrentActivity(activity: Activity) {
        activityRef = WeakReference(activity)
    }

    fun clearActivity(activity: Activity) {
        if (activityRef?.get() === activity) {
            activityRef = null
        }
    }

    fun currentActivity(): Activity? = activityRef?.get()
}

package com.github.multimatum_team.multimatum

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import com.github.multimatum_team.multimatum.LogUtil.debugLog
import com.github.multimatum_team.multimatum.LogUtil.logFunctionCall
import com.github.multimatum_team.multimatum.activity.MainSettingsActivity.Companion.PROCRASTINATION_FIGHTER_ENABLED_PREF_KEY
import com.github.multimatum_team.multimatum.service.ProcrastinationDetectorService
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.search.MapboxSearchSdk
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MultimatumApp : Application(), Application.ActivityLifecycleCallbacks {

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    // This class is required by Hilt
    // It is also useful to perform actions when the app is started

    override fun onCreate() {
        logFunctionCall()
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
        initializeMapboxSearch()
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        logFunctionCall("created ${activity.localClassName}")
    }

    override fun onActivityStarted(activity: Activity) {
        logFunctionCall("started ${activity.localClassName}")
    }

    override fun onActivityResumed(activity: Activity) {
        logFunctionCall("resumed ${activity.localClassName}")
        val onForeground = isAppOnForeground()
        debugLog(if (onForeground) "app is on foreground" else "app is on background")
        if (onForeground){
            stopProcrastinationDetectorIfActive()
        }
    }

    override fun onActivityPaused(activity: Activity) {
        logFunctionCall("paused ${activity.localClassName}")
    }

    override fun onActivityStopped(activity: Activity) {
        logFunctionCall("stopped ${activity.localClassName}")
        val onForeground = isAppOnForeground()
        debugLog(if (onForeground) "app is on foreground" else "app is on background")
        if (!onForeground){
            startProcrastinationDetectorIfNeeded()
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        logFunctionCall()
    }

    override fun onActivityDestroyed(activity: Activity) {
        logFunctionCall("destroyed ${activity.localClassName}")
    }

    private fun initializeMapboxSearch() {
        MapboxSearchSdk.initialize(
            application = this,
            accessToken = getString(R.string.mapbox_access_token),
            locationEngine = LocationEngineProvider.getBestLocationEngine(this)
        )
    }

    private fun startProcrastinationDetectorIfNeeded() {
        logFunctionCall()
        if (sharedPreferences.getBoolean(
                PROCRASTINATION_FIGHTER_ENABLED_PREF_KEY,
                false
            )
        ) {
            debugLog("launching ${ProcrastinationDetectorService::class.simpleName}")
            ProcrastinationDetectorService.launch(this)
        }
    }

    private fun stopProcrastinationDetectorIfActive() {
        logFunctionCall()
        if (sharedPreferences.getBoolean(
                PROCRASTINATION_FIGHTER_ENABLED_PREF_KEY,
                false
            )
        ) {
            debugLog("stopping ${ProcrastinationDetectorService::class.simpleName}")
            ProcrastinationDetectorService.stop(this)
        }
    }

    // Strongly inspired from https://localcoder.org/run-code-when-android-app-is-closed-sent-to-background
    private fun isAppOnForeground(): Boolean {
        val activityManager =
            applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses
        return if (appProcesses == null) {
            false
        } else {
            val packageName = applicationContext.packageName
            appProcesses.any { appProcess ->
                appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                        && appProcess.processName == packageName
            }
        }
    }

}

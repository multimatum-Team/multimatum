package com.github.multimatum_team.multimatum.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import com.github.multimatum_team.multimatum.LogUtil.debugLog
import com.github.multimatum_team.multimatum.LogUtil.logFunctionCall
import com.github.multimatum_team.multimatum.R
import com.github.multimatum_team.multimatum.activity.MainSettingsActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * This service detects the movements of the phone to deduce whether the user is
 * working or not, and can display a reminder if necessary
 *
 * Adapted from https://robertohuertas.com/2019/06/29/android_foreground_services/
 */
@AndroidEntryPoint
class ProcrastinationDetectorService : Service(), SensorEventListener {
    @Inject
    lateinit var sensorManager: SensorManager

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private var isServiceStarted = false
    private var wakeLock: PowerManager.WakeLock? = null

    private val binder = PdsBinder()

    private lateinit var sensorListener: ProcrastinationDetectorSensorListener

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) =
        sensorListener.onAccuracyChanged(sensor, accuracy)

    override fun onSensorChanged(event: SensorEvent?) = sensorListener.onSensorChanged(event)

    override fun onBind(intent: Intent): IBinder {
        initListenerIfNeeded()
        return binder
    }

    // when intent.action is START_ACTION, starts the service
    // when intent.action is STOP_ACTION, stops the service
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logFunctionCall("intent = $intent")
        initListenerIfNeeded()
        if (intent != null) {
            when (val action = intent.action) {
                START_ACTION -> {
                    if (!isServiceStarted) {
                        startProcrastinationDetectorService()
                        isInstanceRunning = true
                    }
                }
                STOP_ACTION -> {
                    stopProcrastinationDetectorService()
                    isInstanceRunning = false
                }
                else -> throw IllegalArgumentException("unexpected action: $action")
            }
        }
        return START_STICKY  // service must restart as soon as possible if preempted
    }

    private fun initListenerIfNeeded() {
        if (!this::sensorListener.isInitialized) {
            sensorListener =
                ProcrastinationDetectorSensorListener(applicationContext, sharedPreferences)
        }
    }

    override fun onCreate() {
        super.onCreate()
        debugLog("service created")
        startForeground(FOREGROUND_SERVICE_NOTIF_ID, createForegroundServiceNotification())
    }

    override fun onDestroy() {
        super.onDestroy()
        val stopIntent = Intent(applicationContext, ProcrastinationDetectorService::class.java)
        stopIntent.action = STOP_ACTION
        onStartCommand(stopIntent, 0, 0)
        debugLog("Service destroyed")
    }

    private fun startProcrastinationDetectorService() {
        logFunctionCall()
        isServiceStarted = true
        acquireWakeLock()
        registerServiceAsSensorListener()
        sensorListener.reloadSensitivity()
    }

    private fun registerServiceAsSensorListener() {
        val refSensor = sensorManager.getDefaultSensor(REF_SENSOR)
            ?: throw IllegalStateException("missing sensor")
        sensorManager.registerListener(this, refSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    private fun acquireWakeLock() {
        wakeLock = (getSystemService(POWER_SERVICE) as PowerManager).run {
            newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                WAKE_LOCK_TAG
            ).apply {
                acquire(WAKE_LOCK_ACQUIRE_TIMEOUT_MILLIS)
            }
        }
    }

    private fun stopProcrastinationDetectorService() {
        logFunctionCall()
        unregisterServiceFromSensorListeners()
        releaseWakeLock()
        stopForeground(true)
        stopSelf()
        isServiceStarted = false
    }

    private fun unregisterServiceFromSensorListeners() {
        val refSensor = sensorManager.getDefaultSensor(REF_SENSOR)
        sensorManager.unregisterListener(this, refSensor)
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
    }

    // creates the notification that is displayed when the foreground service is executing
    private fun createForegroundServiceNotification(): Notification {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(createNotificationChannel())
        return Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.procrastination_fighter_permanent_notif_title))
            .setContentText(getString(R.string.procrastination_fighter_permanent_notif_content_text))
            .setContentIntent(createNotificationPendingIntent())
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    // creates a pending intent that should be added to the foreground service
    // notification to start MainSettingsActivity on pressing the notification
    private fun createNotificationPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainSettingsActivity::class.java),
        PendingIntent.FLAG_IMMUTABLE
    )

    private fun createNotificationChannel(): NotificationChannel {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Procrastination detector notifications channel",
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.description = "Procrastination detector channel"
        channel.enableLights(true)
        channel.lightColor = Color.RED
        return channel
    }

    companion object {

        /**
         * The sensor used by this service to detect movements
         */
        const val REF_SENSOR = Sensor.TYPE_GRAVITY

        const val MAX_SENSITIVITY = 10
        const val DEFAULT_SENSITIVITY = 5

        private const val NOTIFICATION_CHANNEL_ID =
            "com.github.multimatum_team-mutlimatum.ProcrastinationDetectorServiceChannel"

        const val START_ACTION =
            "com.github.multimatum_team.multimatum.StartProcrastinationDetectorServiceAction"
        const val STOP_ACTION =
            "com.github.multimatum_team.multimatum.StopProcrastinationDetectorServiceAction"

        private const val FOREGROUND_SERVICE_NOTIF_ID = 1
        private const val WAKE_LOCK_TAG = "ProcrastinationDetectorService::lock"

        private const val WAKE_LOCK_ACQUIRE_TIMEOUT_MILLIS = 10 * 60 * 1000L  // 10 minutes

        /**
         * Launches ProcrastinationDetectorService
         * @param caller should be 'this' in the calling activity
         */
        fun launch(caller: Context) {
            if (!isInstanceRunning) {
                performStartStopAction(caller, START_ACTION)
            }
        }

        /**
         * Stops ProcrastinationDetectorService
         * @param caller should be 'this' in the calling activity
         */
        fun stop(caller: Context) {
            if (isInstanceRunning) {
                performStartStopAction(caller, STOP_ACTION)
            }
        }

        private fun performStartStopAction(caller: Context, action: String) {
            val intent = Intent(caller, ProcrastinationDetectorService::class.java)
            intent.action = action
            caller.startForegroundService(intent)
        }

        private var isInstanceRunning = false

    }

    inner class PdsBinder : Binder() {
        fun getService(): ProcrastinationDetectorService = this@ProcrastinationDetectorService
    }

}
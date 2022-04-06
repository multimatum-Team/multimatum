package com.github.multimatum_team.multimatum.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import android.widget.Toast
import com.github.multimatum_team.multimatum.MainSettingsActivity
import com.github.multimatum_team.multimatum.R
import dagger.hilt.android.AndroidEntryPoint
import java.lang.IllegalArgumentException
import javax.inject.Inject
import kotlin.math.abs

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

    private var isServiceStarted = false
    private var wakeLock: PowerManager.WakeLock? = null

    // data relative to the last time a movement was detected
    private var lastDetectionTimestamp: Long = 0L
    private var lastPosition: Array<Float>? = null  // null only at initialization

    private val binder = PdsBinder()

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    // also used to stop the service, when action is STOP_ACTION
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            when (val action = intent.action) {
                START_ACTION -> if (!isServiceStarted) startProcrastinationDetectorService()
                STOP_ACTION -> stopProcrastinationDetectorService()
                else -> throw IllegalArgumentException("unexpected action: $action")
            }
        }
        return START_STICKY  // service must restart as soon as possible if preempted
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(LOG_TAG, "Service created")
        startForeground(FOREGROUND_SERVICE_NOTIF_ID, createForegroundServiceNotification())
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(LOG_TAG, "Service destroyed")
    }

    private fun startProcrastinationDetectorService() {
        toast(getString(R.string.procrastination_fighter_enable_msg))
        isServiceStarted = true
        acquireWakeLock()
        registerServiceAsSensorListener()
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

    fun stopProcrastinationDetectorService() {
        unregisterServiceFromSensorListeners()
        toast(getString(R.string.procrastination_fighter_disabled_msg))
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

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        /* Nothing to do */
    }

    override fun onSensorChanged(event: SensorEvent?) {
        requireNotNull(event)
        Log.d(LOG_TAG, "sensor change detected")
        val currentTime = event.timestamp
        // check whether enough time has passed since the last detection (o.w. do nothing)
        if (currentTime >= lastDetectionTimestamp + MIN_PERIOD_BETWEEN_NOTIF_NANOSEC) {
            val currentPosition = event.values.toTypedArray()  // values measured by the sensor
            // check whether there was a sufficient move to trigger the toast
            if (lastPosition != null && l1Distance(
                    currentPosition,
                    lastPosition!!
                ) > MOVE_DETECTION_THRESHOLD
            ) {
                toast(applicationContext.getString(R.string.stop_procrastinating_msg))
            }
            lastPosition = currentPosition
            lastDetectionTimestamp = currentTime
        }
    }

    private fun l1Distance(p1: Array<Float>, p2: Array<Float>): Float {
        require(p1.size == 3 && p2.size == 3)
        val dx = abs(p1[0] - p2[0])
        val dy = abs(p1[1] - p2[1])
        val dz = abs(p1[2] - p2[2])
        return dx + dy + dz
    }

    // Displays a toast with the given text
    private fun toast(text: String) {
        Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()
    }

    companion object {

        /**
         * The sensor used by this service to detect movements
         */
        const val REF_SENSOR = Sensor.TYPE_GRAVITY

        private const val MIN_PERIOD_BETWEEN_NOTIF_NANOSEC = 2_000_000_000L  // 2 seconds

        /**
         * Experimentally obtained threshold. It has not unit because it refers to the output of
         * the sensors, which have no unit themselves
         */
        private const val MOVE_DETECTION_THRESHOLD = 0.1

        // for logging
        private const val LOG_TAG = "ProcrastinationDetectorService"

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
        fun launch(caller: Context) = performStartStopAction(caller, START_ACTION)

        /**
         * Stops ProcrastinationDetectorService
         * @param caller should be 'this' in the calling activity
         */
        fun stop(caller: Context) = performStartStopAction(caller, STOP_ACTION)

        private fun performStartStopAction(caller: Context, action: String){
            val intent = Intent(caller, ProcrastinationDetectorService::class.java)
            intent.action = action
            caller.startForegroundService(intent)
        }

    }

    inner class PdsBinder : Binder() {
        fun getService(): ProcrastinationDetectorService = this@ProcrastinationDetectorService
    }

}
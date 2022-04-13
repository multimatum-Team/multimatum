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
import android.widget.Toast
import com.github.multimatum_team.multimatum.LogUtil.debugLog
import com.github.multimatum_team.multimatum.LogUtil.logFunctionCall
import com.github.multimatum_team.multimatum.MainSettingsActivity
import com.github.multimatum_team.multimatum.R
import dagger.hilt.android.AndroidEntryPoint
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
    private var lastDetectionTimestampNanos: Long = LAST_DETECTION_TIMESTAMP_NONINIT_CODE
    private var lastPosition: Array<Float>? = null  // null only at initialization

    // the service will wait for this number of detections before starting to display toasts
    private var nonReportedDetectionsCnt = NON_REPORTED_DETECTIONS_CNT_INIT

    private val binder = PdsBinder()

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    // when intent.action is START_ACTION, starts the service
    // when intent.action is STOP_ACTION, stops the service
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logFunctionCall("intent = $intent")
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

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        /* Nothing to do */
    }

    override fun onSensorChanged(event: SensorEvent?) {
        requireNotNull(event)
        val currentTimeNanos = event.timestamp
        initLastDetectionIfNotYetInitialized(currentTimeNanos)
        initLastPositionWithCurrentIfNull(event)
        // check whether enough time has passed since the last detection (o.w. do nothing)
        if (currentTimeNanos >= lastDetectionTimestampNanos + MIN_PERIOD_BETWEEN_NOTIF_NANOSEC) {
            val currentPosition = event.values.toTypedArray()  // values measured by the sensor
            // check whether there was a sufficient move to trigger the toast
            if (l1Distance(currentPosition, lastPosition!!) > MOVE_DETECTION_THRESHOLD) {
                reactToSignificantMove()
            }
            else {
                reactToAbsenceOfMove()
            }
            lastPosition = currentPosition
            lastDetectionTimestampNanos = currentTimeNanos
        }
    }

    private fun initLastDetectionIfNotYetInitialized(currentTimeNanos: Long) {
        if (lastDetectionTimestampNanos == LAST_DETECTION_TIMESTAMP_NONINIT_CODE) {
            /* trick: lastDetectionTimestampNanos is set DELAY_BEFORE_CHECK_START_NANOS in the future
             * so that the service only starts checking DELAY_BEFORE_CHECK_START_NANOS after it
             * was started   */
            lastDetectionTimestampNanos =
                currentTimeNanos + DELAY_BEFORE_CHECK_START_NANOS - MIN_PERIOD_BETWEEN_NOTIF_NANOSEC
        }
    }

    private fun initLastPositionWithCurrentIfNull(event: SensorEvent) {
        if (lastPosition == null) {
            val currentPosition = event.values.toTypedArray()  // values measured by the sensor
            lastPosition = currentPosition
        }
    }

    private fun reactToSignificantMove() {
        if (nonReportedDetectionsCnt > 0) {
            nonReportedDetectionsCnt -= 1
        } else {
            toast(applicationContext.getString(R.string.stop_procrastinating_msg))
        }
    }

    private fun reactToAbsenceOfMove(){
        nonReportedDetectionsCnt = NON_REPORTED_DETECTIONS_CNT_INIT
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

        private const val NOTIFICATION_CHANNEL_ID =
            "com.github.multimatum_team-mutlimatum.ProcrastinationDetectorServiceChannel"

        const val START_ACTION =
            "com.github.multimatum_team.multimatum.StartProcrastinationDetectorServiceAction"
        const val STOP_ACTION =
            "com.github.multimatum_team.multimatum.StopProcrastinationDetectorServiceAction"

        private const val FOREGROUND_SERVICE_NOTIF_ID = 1
        private const val WAKE_LOCK_TAG = "ProcrastinationDetectorService::lock"

        private const val WAKE_LOCK_ACQUIRE_TIMEOUT_MILLIS = 10 * 60 * 1000L  // 10 minutes
        private const val DELAY_BEFORE_CHECK_START_NANOS = 15 * 1_000_000_000L // 15 seconds

        // value that lastDetectionTimestampNanos takes when it is not initialized
        private const val LAST_DETECTION_TIMESTAMP_NONINIT_CODE = -1L

        // the first NON_REPORTED_DETECTIONS_CNT_INIT detected moves will be ignored
        private const val NON_REPORTED_DETECTIONS_CNT_INIT = 2

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
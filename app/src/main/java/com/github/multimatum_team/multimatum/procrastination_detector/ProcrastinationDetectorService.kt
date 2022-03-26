package com.github.multimatum_team.multimatum.procrastination_detector

import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.IBinder
import android.widget.Toast
import com.github.multimatum_team.multimatum.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.abs

/**
 * This service detects the movements of the phone to deduce whether the user is
 * working or not, and can display a reminder if necessary
 */
@AndroidEntryPoint
class ProcrastinationDetectorService : Service(), SensorEventListener {
    @Inject lateinit var sensorManager: SensorManager

    // TODO make it remain enabled when app is stopped

    private var lastDetection: Long = 0L
    private var lastPosition: Array<Float>? = null  // null only at initialization

    private val binder = PdsBinder()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val refSensor = sensorManager.getDefaultSensor(REF_SENSOR)
            ?: throw IllegalStateException("missing sensor")
        sensorManager.registerListener(this, refSensor, SensorManager.SENSOR_DELAY_NORMAL)
        toast(getString(R.string.procrastination_fighter_enable_msg))
        return START_STICKY  // service must restart as soon as possible if preempted
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        val refSensor = sensorManager.getDefaultSensor(REF_SENSOR)
        sensorManager.unregisterListener(this, refSensor)
        toast(getString(R.string.procrastination_fighter_disabled_msg))
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { /* Nothing to do */ }

    override fun onSensorChanged(event: SensorEvent?) {
        val currentTime = System.currentTimeMillis()
        if (currentTime >= lastDetection + MIN_PERIOD_BETWEEN_NOTIF){
            val currentPosition = event!!.values.toTypedArray()
            if (lastPosition != null && l1Distance(currentPosition, lastPosition!!) > MOVE_DETECTION_THRESHOLD) {
                toast(applicationContext.getString(R.string.stop_procrastinating_msg))
            }
            lastPosition = currentPosition
            lastDetection = currentTime
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

        private const val MIN_PERIOD_BETWEEN_NOTIF = 2000L

        /**
         * Experimentally obtained threshold. It has not unit because it refers to the output of
         * the sensors, which have no unit themselves
         */
        private const val MOVE_DETECTION_THRESHOLD = 0.1
    }

    inner class PdsBinder: Binder() {
        fun getService(): ProcrastinationDetectorService = this@ProcrastinationDetectorService
    }

}
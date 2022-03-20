package com.github.multimatum_team.multimatum.procrastination_detector

import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.widget.Toast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.abs

@AndroidEntryPoint
class ProcrastinationDetectorService : Service(), SensorEventListener {
    @Inject lateinit var sensorManager: SensorManager

    // TODO make it remain enabled when app is stopped

    private var lastDetection: Long = 0L
    private var lastPosition: Array<Float>? = null  // null only at initialization

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val refSensor = sensorManager.getDefaultSensor(REF_SENSOR)
            ?: throw IllegalStateException("missing sensor")
        super.onCreate()
        sensorManager.registerListener(this, refSensor, SensorManager.SENSOR_DELAY_NORMAL)
        toast("Procrastination fighter is enabled")
        return START_STICKY  // service must restart as soon as possible if preempted
    }

    override fun onBind(intent: Intent): IBinder {
        // TODO see whether we need it
        throw UnsupportedOperationException("cannot bind procrastinationDetectorService")
    }

    override fun onDestroy() {
        super.onDestroy()
        val refSensor = sensorManager.getDefaultSensor(REF_SENSOR)
        sensorManager.unregisterListener(this, refSensor)
        toast("Procrastination fighter is disabled")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { /* Nothing to do */ }

    override fun onSensorChanged(event: SensorEvent?) {
        val currentTime = System.currentTimeMillis()
        if (currentTime >= lastDetection + MIN_PERIOD_BETWEEN_NOTIF){
            val currentPosition = event!!.values.toTypedArray()    // TODO check if really want to crash if null
            if (lastPosition != null && l1Distance(currentPosition, lastPosition!!) > MOVE_DETECTION_THRESHOLD) {
                toast("Hey! Why is this phone moving? You should be working!")
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

    private fun toast(text: String) {
        Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val MIN_PERIOD_BETWEEN_NOTIF = 2000L
        const val REF_SENSOR = Sensor.TYPE_GRAVITY
        private const val MOVE_DETECTION_THRESHOLD = 0.1
    }

}
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

@AndroidEntryPoint
class ProcrastinationDetectorService : Service(), SensorEventListener {
    @Inject lateinit var sensorManager: SensorManager

    private var lastDetection: Long = 0L

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            ?: throw IllegalStateException("missing accelerometer")
        super.onCreate()
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        toast("Procrastination fighter is enabled")
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        // TODO see whether we need it
        throw UnsupportedOperationException("cannot bind procrastinationDetectorService")
    }

    override fun onDestroy() {
        super.onDestroy()
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.unregisterListener(this, accelerometer)
        toast("Procrastination fighter is disabled")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { /* Nothing to do */ }

    override fun onSensorChanged(event: SensorEvent?) {
        val currentTime = System.currentTimeMillis()
        if (currentTime >= lastDetection + MIN_PERIOD_BETWEEN_NOTIF){
            toast(event?.values?.joinToString()?.plus(" $currentTime")?:"error") // FIXME
            lastDetection = currentTime
        }
    }

    private fun toast(text: String) {
        Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val MIN_PERIOD_BETWEEN_NOTIF = 10000L
    }

}
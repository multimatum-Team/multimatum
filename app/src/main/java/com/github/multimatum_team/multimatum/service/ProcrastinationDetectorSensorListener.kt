package com.github.multimatum_team.multimatum.service

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.widget.Toast
import com.github.multimatum_team.multimatum.LogUtil
import com.github.multimatum_team.multimatum.R
import com.github.multimatum_team.multimatum.activity.MainSettingsActivity.Companion.PROCRASTINATION_FIGHTER_SENSITIVITY_PREF_KEY
import com.github.multimatum_team.multimatum.service.ProcrastinationDetectorService.Companion.DEFAULT_SENSITIVITY
import com.github.multimatum_team.multimatum.service.ProcrastinationDetectorService.Companion.MAX_SENSITIVITY
import kotlin.math.abs

class ProcrastinationDetectorSensorListener(
    private val applicationContext: Context,
    private val sharedPreferences: SharedPreferences
) :
    SensorEventListener {

    // the first nonReportedDetectionsCntInit detected moves will be ignored
    private var nonReportedDetectionsCntInit = 0

    // the service will wait for this number of detections before starting to display toasts
    private var nonReportedDetectionsCnt = nonReportedDetectionsCntInit

    // data relative to the last time a movement was detected
    private var lastDetectionTimestampNanos: Long = LAST_DETECTION_TIMESTAMP_NONINIT_CODE
    private var lastPosition: Array<Float>? = null  // null only at initialization

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
            } else {
                reactToAbsenceOfMove()
            }
            lastPosition = currentPosition
            lastDetectionTimestampNanos = currentTimeNanos
        }
    }

    /**
     * Load the sensitivity from the SharedPreferences and recompute the number of ignored detections
     */
    fun reloadSensitivity() {
        val sensitivity = sharedPreferences.getInt(
            PROCRASTINATION_FIGHTER_SENSITIVITY_PREF_KEY,
            DEFAULT_SENSITIVITY
        )
        LogUtil.logFunctionCall("load sensitivity: $sensitivity")
        nonReportedDetectionsCntInit = MAX_SENSITIVITY - sensitivity
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
            Toast.makeText(
                applicationContext,
                applicationContext.getString(R.string.stop_procrastinating_msg),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun reactToAbsenceOfMove() {
        nonReportedDetectionsCnt = nonReportedDetectionsCntInit
    }

    private fun l1Distance(p1: Array<Float>, p2: Array<Float>): Float {
        require(p1.size == 3 && p2.size == 3)
        val dx = abs(p1[0] - p2[0])
        val dy = abs(p1[1] - p2[1])
        val dz = abs(p1[2] - p2[2])
        return dx + dy + dz
    }

    companion object {
        private const val MIN_PERIOD_BETWEEN_NOTIF_NANOSEC = 2_000_000_000L  // 2 seconds

        private const val DELAY_BEFORE_CHECK_START_NANOS = 15 * 1_000_000_000L // 15 seconds

        /**
         * Experimentally obtained threshold. It has not unit because it refers to the output of
         * the sensors, which have no unit themselves
         */
        private const val MOVE_DETECTION_THRESHOLD = 0.1

        // value that lastDetectionTimestampNanos takes when it is not initialized
        private const val LAST_DETECTION_TIMESTAMP_NONINIT_CODE = -1L

    }

}
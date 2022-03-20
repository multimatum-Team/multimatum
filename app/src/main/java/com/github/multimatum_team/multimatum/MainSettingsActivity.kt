package com.github.multimatum_team.multimatum

import android.content.Intent
import android.content.SharedPreferences
import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.github.multimatum_team.multimatum.procrastination_detector.ProcrastinationDetectorService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainSettingsActivity : AppCompatActivity() {
    private lateinit var darkModeEnabledButton: SwitchCompat
    private lateinit var notifEnabledButton: SwitchCompat
    private lateinit var procrastinationDetectEnabledButton: SwitchCompat

    // TODO constants

    @Inject lateinit var preferences: SharedPreferences

    // to be able to check if a sensor is available
    @Inject lateinit var sensorManager: SensorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_settings)
        darkModeEnabledButton = findViewById(R.id.main_settings_dark_mode_button)
        notifEnabledButton = findViewById(R.id.main_settings_enable_notif_button)
        procrastinationDetectEnabledButton = findViewById(R.id.main_settings_enable_procrastination_fighter_button)
        darkModeEnabledButton.isChecked = preferences.getBoolean(DARK_MODE_PREF_KEY, false)
        notifEnabledButton.isChecked = preferences.getBoolean(NOTIF_ENABLED_PREF_KEY, true)
        procrastinationDetectEnabledButton.isChecked = preferences.getBoolean(
            PROCRASTINATION_FIGHTER_ENABLED_PREF_KEY, false)
        darkModeEnabledButton.setOnCheckedChangeListener { _, newState ->
            writeNewState(DARK_MODE_PREF_KEY, newState)
        }
        notifEnabledButton.setOnCheckedChangeListener { _, newState ->
            writeNewState(NOTIF_ENABLED_PREF_KEY, newState)
        }
        procrastinationDetectEnabledButton.setOnCheckedChangeListener {_, newState ->
            when(newState){
                true -> {
                    startService(Intent(applicationContext, ProcrastinationDetectorService::class.java))
                }
                false -> stopService(Intent(applicationContext, ProcrastinationDetectorService::class.java))
            }
            writeNewState(PROCRASTINATION_FIGHTER_ENABLED_PREF_KEY, newState)
        }
        val sensorPresent = sensorFound()
        procrastinationDetectEnabledButton.alpha = if (sensorPresent) 1.0F else 0.5F
        if (!sensorPresent){
            procrastinationDetectEnabledButton.text =
                procrastinationDetectEnabledButton.text.toString() + " (disabled: missing sensor on device)"
        }
        procrastinationDetectEnabledButton.isClickable = sensorPresent
    }

    private fun writeNewState(key: String, currState: Boolean) {
        val edit = preferences.edit()
        edit.putBoolean(key, currState)
        edit.apply()
    }

    private fun sensorFound(): Boolean =
        sensorManager.getDefaultSensor(ProcrastinationDetectorService.REF_SENSOR) != null

    companion object {

        /**
         * Key for the storage of whether dark mode is enabled in SharedPreferences
         */
        const val DARK_MODE_PREF_KEY =
            "com.github.multimatum_team.multimatum.MainSettingsActivity.DarkMode"

        /**
         * Key for the storage of whether notifications are enabled in SharedPreferences
         */
        const val NOTIF_ENABLED_PREF_KEY =
            "com.github.multimatum_team.multimatum.MainSettingsActivity.NotifEnabled"

        /**
         * Key for the storage of whether procrastination fighter is enabled
         */
        const val PROCRASTINATION_FIGHTER_ENABLED_PREF_KEY =
            "com.github.multimatum_team.multimatum.MainSettingsActivity.ProcrastinationFighterEnabled"
    }

}


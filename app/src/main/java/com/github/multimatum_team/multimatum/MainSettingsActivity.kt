package com.github.multimatum_team.multimatum

import android.content.Intent
import android.content.SharedPreferences
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.github.multimatum_team.multimatum.model.AnonymousUser
import com.github.multimatum_team.multimatum.model.SignedInUser
import com.github.multimatum_team.multimatum.service.ProcrastinationDetectorService
import com.github.multimatum_team.multimatum.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainSettingsActivity : AppCompatActivity() {
    private lateinit var darkModeEnabledButton: SwitchCompat
    private lateinit var notifEnabledButton: SwitchCompat
    private lateinit var procrastinationDetectEnabledButton: SwitchCompat

    private val userViewModel: UserViewModel by viewModels()

    @Inject
    lateinit var preferences: SharedPreferences

    // to be able to check if a sensor is available
    @Inject
    lateinit var sensorManager: SensorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_settings)
        assignWidgets()
        loadButtonsState()
        setWidgetListeners()
        disableProcrastinationFighterButtonIfSensorNotFound()
    }

    /*
        If the sensor used by ProcrastinationFighterService was not found,
        the button to start it is blocked and a message is displayed.
        This can happen mainly because the phone is not equipped with such
        a sensor
     */
    private fun disableProcrastinationFighterButtonIfSensorNotFound() {
        val sensorPresent = sensorFound()
        // if no sensor available then button should be gray to show that it is disabled
        procrastinationDetectEnabledButton.alpha =
            if (sensorPresent) BUTTON_ALPHA_NORMAL else BUTTON_ALPHA_DISABLED
        if (!sensorPresent) {
            procrastinationDetectEnabledButton.text = applicationContext.getString(
                R.string.missing_sensor_msg, procrastinationDetectEnabledButton.text
            )
        }
        procrastinationDetectEnabledButton.isClickable = sensorPresent
    }

    // sets all the necessary listeners on the UI widgets
    private fun setWidgetListeners() {
        darkModeEnabledButton.setOnCheckedChangeListener { _, newState ->
            writeNewState(DARK_MODE_PREF_KEY, newState)
        }
        notifEnabledButton.setOnCheckedChangeListener { _, newState ->
            writeNewState(NOTIF_ENABLED_PREF_KEY, newState)
        }
        procrastinationDetectEnabledButton.setOnCheckedChangeListener { _, newState ->
            when (newState) {
                true -> startService(
                    Intent(
                        applicationContext,
                        ProcrastinationDetectorService::class.java
                    )
                )
                false -> stopService(
                    Intent(
                        applicationContext,
                        ProcrastinationDetectorService::class.java
                    )
                )
            }
            writeNewState(PROCRASTINATION_FIGHTER_ENABLED_PREF_KEY, newState)
        }
    }

    // sets the buttons to the state they had last time the app was stopped
    private fun loadButtonsState() {
        darkModeEnabledButton.isChecked = preferences.getBoolean(DARK_MODE_PREF_KEY, false)
        notifEnabledButton.isChecked = preferences.getBoolean(NOTIF_ENABLED_PREF_KEY, true)
        procrastinationDetectEnabledButton.isChecked = preferences.getBoolean(
            PROCRASTINATION_FIGHTER_ENABLED_PREF_KEY, false
        )
    }

    // assigns the widget attributes using findViewById
    private fun assignWidgets() {
        darkModeEnabledButton = findViewById(R.id.main_settings_dark_mode_button)
        notifEnabledButton = findViewById(R.id.main_settings_enable_notif_button)
        procrastinationDetectEnabledButton =
            findViewById(R.id.main_settings_enable_procrastination_fighter_button)
    }

    // writes the key/state pair to SharedPreferences
    private fun writeNewState(key: String, currState: Boolean) {
        val edit = preferences.edit()
        edit.putBoolean(key, currState)
        edit.apply()
    }

    // returns true iff the sensor that ProcrastinationDetectionService will use has been found
    private fun sensorFound(): Boolean =
        sensorManager.getDefaultSensor(ProcrastinationDetectorService.REF_SENSOR) != null

    companion object {
        private const val TAG = "MainSettingsActivity"

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

        private const val BUTTON_ALPHA_NORMAL = 1F
        private const val BUTTON_ALPHA_DISABLED = 0.5F
    }

    fun goToAccountSettings(view: View) {
        Log.d(TAG, "goToAccountSettings: ${userViewModel.getUser().value}")
        when (userViewModel.getUser().value!!) {
            is AnonymousUser -> {
                val intent = Intent(this, AccountActivity::class.java)
                startActivity(intent)
            }
            is SignedInUser -> {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            }
        }
    }
}


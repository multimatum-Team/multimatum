package com.github.multimatum_team.multimatum.activity

import android.content.Intent
import android.content.SharedPreferences
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import com.github.multimatum_team.multimatum.LogUtil
import com.github.multimatum_team.multimatum.R
import com.github.multimatum_team.multimatum.model.AnonymousUser
import com.github.multimatum_team.multimatum.model.SignedInUser
import com.github.multimatum_team.multimatum.service.ProcrastinationDetectorService
import com.github.multimatum_team.multimatum.viewmodel.AuthViewModel
import com.google.android.material.slider.Slider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainSettingsActivity : AppCompatActivity() {
    private lateinit var darkModeEnabledButton: SwitchCompat
    private lateinit var notifEnabledButton: SwitchCompat
    private lateinit var procrastinationDetectEnabledButton: SwitchCompat
    private lateinit var procrastinationDetectorSlider: Slider
    private lateinit var procrastinationDetectorSliderBar: LinearLayout

    private val userViewModel: AuthViewModel by viewModels()

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
        initializeProcrastinationDetectorSensitivitySlider()
        setWidgetListeners()
        disableProcrastinationFighterButtonIfSensorNotFound()
    }

    private fun initializeProcrastinationDetectorSensitivitySlider() {
        // slider can be moved iff procrastination fighter is enabled
        procrastinationDetectorSlider.isEnabled = procrastinationDetectEnabledButton.isChecked
        procrastinationDetectorSlider.valueFrom = 0f
        procrastinationDetectorSlider.valueTo =
            ProcrastinationDetectorService.MAX_SENSITIVITY.toFloat()
        procrastinationDetectorSlider.stepSize = 1f
        // progress stands for position on the bar
        procrastinationDetectorSlider.value = preferences.getInt(
            PROCRASTINATION_FIGHTER_SENSITIVITY_PREF_KEY,
            ProcrastinationDetectorService.DEFAULT_SENSITIVITY
        ).toFloat()
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
            if (sensorPresent) ALPHA_NORMAL else ALPHA_DISABLED
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
            when (newState) {
                true -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                false -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            writeNewBooleanState(DARK_MODE_PREF_KEY, newState)
        }
        notifEnabledButton.setOnCheckedChangeListener { _, newState ->
            writeNewBooleanState(NOTIF_ENABLED_PREF_KEY, newState)
        }
        procrastinationDetectEnabledButton.setOnCheckedChangeListener { _, newState ->
            writeNewBooleanState(PROCRASTINATION_FIGHTER_ENABLED_PREF_KEY, newState)
            // slider can be moved iff procrastination detector is enabled
            procrastinationDetectorSlider.isEnabled = newState
        }
        procrastinationDetectorSlider.addOnChangeListener(Slider.OnChangeListener { _, value, _ ->
            writeNewSensitivityValue(value.toInt())
        })
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
        procrastinationDetectorSlider =
            findViewById(R.id.main_settings_procrastination_detector_sensibility_slider)
        procrastinationDetectorSliderBar =
            findViewById(R.id.main_settings_procrastination_detector_sensibility_bar)
    }

    // writes the key/state pair to SharedPreferences
    private fun writeNewBooleanState(key: String, currState: Boolean) {
        val edit = preferences.edit()
        edit.putBoolean(key, currState)
        edit.apply()
    }

    // write sensitivity to SharedPreferences
    private fun writeNewSensitivityValue(sensitivityValue: Int) {
        LogUtil.logFunctionCall("new sensitivity value: $sensitivityValue")
        val edit = preferences.edit()
        edit.putInt(PROCRASTINATION_FIGHTER_SENSITIVITY_PREF_KEY, sensitivityValue)
        edit.apply()
    }

    // returns true iff the sensor that ProcrastinationDetectionService will use has been found
    private fun sensorFound(): Boolean =
        sensorManager.getDefaultSensor(ProcrastinationDetectorService.REF_SENSOR) != null

    companion object {
        private const val TAG = "MainSettingsActivity"
        private const val ALPHA_NORMAL = 1F
        private const val ALPHA_DISABLED = 0.5F

        /**
         * Key for the storage of whether dark mode is enabled in SharedPreferences
         */
        const val DARK_MODE_PREF_KEY =
            "com.github.multimatum_team.multimatum.activity.MainSettingsActivity.DarkMode"

        /**
         * Key for the storage of whether notifications are enabled in SharedPreferences
         */
        const val NOTIF_ENABLED_PREF_KEY =
            "com.github.multimatum_team.multimatum.activity.MainSettingsActivity.NotifEnabled"

        /**
         * Key for the storage of whether procrastination fighter is enabled
         */
        const val PROCRASTINATION_FIGHTER_ENABLED_PREF_KEY =
            "com.github.multimatum_team.multimatum.activity.MainSettingsActivity.ProcrastinationFighterEnabled"

        /**
         * Key for the storage of the sensitivity of the procrastination detector
         */
        const val PROCRASTINATION_FIGHTER_SENSITIVITY_PREF_KEY =
            "com.github.multimatum_team.multimatum.activity.MainSettingsActivity.ProcrastinationFighterSensitivity"
    }

    @Suppress("UNUSED_PARAMETER")
    fun goToAccountSettings(view: View) {
        Log.d(TAG, "goToAccountSettings: ${userViewModel.getUser().value}")
        val intent = when (userViewModel.getUser().value!!) {
            is AnonymousUser ->
                Intent(this, SignInActivity::class.java)
            is SignedInUser ->
                Intent(this, ProfileActivity::class.java)
        }
        startActivity(intent)
    }
}


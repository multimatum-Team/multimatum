package com.github.multimatum_team.multimatum

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.SwitchCompat

class MainSettingsActivity : AppCompatActivity() {
    private lateinit var darkModeEnabledButton: SwitchCompat
    private lateinit var notifEnabledButton: SwitchCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_settings)
        darkModeEnabledButton = findViewById(R.id.main_settings_dark_mode_button)
        notifEnabledButton = findViewById(R.id.main_settings_enable_notif_button)
        val preferences = getSharedPreferences(MAIN_SETTINGS_ACTIVITY_SHARED_PREF_ID, MODE_PRIVATE)
        darkModeEnabledButton.isChecked = preferences.getBoolean(DARK_MODE_PREF_KEY, false)
        notifEnabledButton.isChecked = preferences.getBoolean(NOTIF_ENABLED_PREF_KEY, true)
        darkModeEnabledButton.setOnCheckedChangeListener { _, newState ->
            onSwitchStateChanged(DARK_MODE_PREF_KEY, newState)
        }
        notifEnabledButton.setOnCheckedChangeListener { _, newState ->
            onSwitchStateChanged(NOTIF_ENABLED_PREF_KEY, newState)
        }
    }

    private fun onSwitchStateChanged(key: String, currState: Boolean){
        val prefEdit = getSharedPreferences(MAIN_SETTINGS_ACTIVITY_SHARED_PREF_ID, MODE_PRIVATE).edit()
        prefEdit.putBoolean(key, currState)
        prefEdit.apply()
    }

    companion object {

        /**
         * Identifier for the SharedPreferences of this activity
         */
        const val MAIN_SETTINGS_ACTIVITY_SHARED_PREF_ID =
            "com.github.multimatum_team.multimatum.MainSettingsActivity.SharedPrefId"

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
    }

}


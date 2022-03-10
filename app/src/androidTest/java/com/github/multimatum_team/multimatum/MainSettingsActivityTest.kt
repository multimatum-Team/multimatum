package com.github.multimatum_team.multimatum

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainSettingsActivityTest {

    @Before fun initIntents(){ Intents.init() }
    @After fun releaseIntents(){ Intents.release() }

    @Test
    fun disabling_notifications_on_button_disables_them_in_preferences(){
        runWriteToPrefTestScenario(
            initNotifEnabled = true, initDarkModeEnabled = false,
            clickedButtonId = R.id.main_settings_enable_notif_button,
            expectedFinalNotifEnabled = false, expectedFinalDarkModeEnabled = false
        )
    }

    @Test
    fun enabling_notifications_on_button_enables_them_in_preferences(){
        runWriteToPrefTestScenario(
            initNotifEnabled = false, initDarkModeEnabled = false,
            clickedButtonId = R.id.main_settings_enable_notif_button,
            expectedFinalNotifEnabled = true, expectedFinalDarkModeEnabled = false
        )
    }

    @Test
    fun disabling_dark_mode_on_button_disables_them_in_preferences(){
        runWriteToPrefTestScenario(
            initNotifEnabled = true, initDarkModeEnabled = true,
            clickedButtonId = R.id.main_settings_dark_mode_button,
            expectedFinalNotifEnabled = true, expectedFinalDarkModeEnabled = false
        )
    }

    @Test
    fun enabling_dark_mode_on_button_enables_them_in_preferences(){
        runWriteToPrefTestScenario(
            initNotifEnabled = false, initDarkModeEnabled = false,
            clickedButtonId = R.id.main_settings_dark_mode_button,
            expectedFinalNotifEnabled = false, expectedFinalDarkModeEnabled = true
        )
    }

    private fun runWriteToPrefTestScenario(initNotifEnabled: Boolean, initDarkModeEnabled: Boolean,
                                           clickedButtonId: Int,
                                           expectedFinalNotifEnabled: Boolean, expectedFinalDarkModeEnabled: Boolean
    ){
        val applicationContext = ApplicationProvider.getApplicationContext<Context>()
        val pref = applicationContext.getSharedPreferences(
            MainSettingsActivity.MAIN_SETTINGS_ACTIVITY_SHARED_PREF_ID,
            Context.MODE_PRIVATE
        )
        val edit = pref.edit()
        edit.clear()
        edit.putBoolean(MainSettingsActivity.NOTIF_ENABLED_PREF_KEY, initNotifEnabled)
        edit.putBoolean(MainSettingsActivity.DARK_MODE_PREF_KEY, initDarkModeEnabled)
        edit.apply()
        val intent = Intent(applicationContext, MainSettingsActivity::class.java)
        val activityScenario: ActivityScenario<MainSettingsActivity> = ActivityScenario.launch(intent)
        activityScenario.use {
            onView(withId(clickedButtonId)).perform(click())
            onView(withId(R.id.main_settings_enable_notif_button)).check(matches(if (expectedFinalNotifEnabled) isChecked() else isNotChecked()))
            onView(withId(R.id.main_settings_dark_mode_button)).check(matches(if (expectedFinalDarkModeEnabled) isChecked() else isNotChecked()))
        }
        assertTrue(pref.contains(MainSettingsActivity.NOTIF_ENABLED_PREF_KEY))
        assertTrue(pref.contains(MainSettingsActivity.DARK_MODE_PREF_KEY))
        // Default values are set so that test fails if those are returned (but this should not happen!)
        assertEquals(expectedFinalNotifEnabled, pref.getBoolean(MainSettingsActivity.NOTIF_ENABLED_PREF_KEY, !expectedFinalNotifEnabled))
        assertEquals(expectedFinalDarkModeEnabled, pref.getBoolean(MainSettingsActivity.DARK_MODE_PREF_KEY, !expectedFinalDarkModeEnabled))
    }

}
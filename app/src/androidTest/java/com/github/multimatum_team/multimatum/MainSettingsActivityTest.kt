package com.github.multimatum_team.multimatum

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.SensorManager
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.*

@UninstallModules(DependenciesProvider::class)
@HiltAndroidTest
class MainSettingsActivityTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
        Intents.init()
    }

    @After
    fun release(){
        Intents.release()
    }

    @Test
    fun disabling_notifications_on_button_disables_them_in_preferences() {
        testScenario(
            initNotifEnabled = true, initDarkModeEnabled = false,
            clickedButtonId = R.id.main_settings_enable_notif_button,
            expectedFinalNotifEnabled = false, expectedFinalDarkModeEnabled = false
        )
    }

    @Test
    fun enabling_notifications_on_button_enables_them_in_preferences() {
        testScenario(
            initNotifEnabled = false, initDarkModeEnabled = false,
            clickedButtonId = R.id.main_settings_enable_notif_button,
            expectedFinalNotifEnabled = true, expectedFinalDarkModeEnabled = false
        )
    }

    @Test
    fun disabling_dark_mode_on_button_disables_them_in_preferences() {
        testScenario(
            initNotifEnabled = true, initDarkModeEnabled = true,
            clickedButtonId = R.id.main_settings_dark_mode_button,
            expectedFinalNotifEnabled = true, expectedFinalDarkModeEnabled = false
        )
    }

    @Test
    fun enabling_dark_mode_on_button_enables_them_in_preferences() {
        testScenario(
            initNotifEnabled = false, initDarkModeEnabled = false,
            clickedButtonId = R.id.main_settings_dark_mode_button,
            expectedFinalNotifEnabled = false, expectedFinalDarkModeEnabled = true
        )
    }

    // The parameters are the initial state of the buttons, the button whose state will be changed
    // and the expected final states of the buttons
    // The method checks that settings values are correctly written to SharedPreferences and
    // that the buttons are at the expected position at the end of the scenario
    private fun testScenario(
        initNotifEnabled: Boolean, initDarkModeEnabled: Boolean,
        clickedButtonId: Int,
        expectedFinalNotifEnabled: Boolean, expectedFinalDarkModeEnabled: Boolean
    ){
        `when`(mockSharedPreferences.getBoolean(eq(MainSettingsActivity.NOTIF_ENABLED_PREF_KEY), any()))
            .thenReturn(initNotifEnabled)
        `when`(mockSharedPreferences.getBoolean(eq(MainSettingsActivity.DARK_MODE_PREF_KEY), any()))
            .thenReturn(initDarkModeEnabled)
        val mockEditor: SharedPreferences.Editor = mock()
        `when`(mockEditor.putBoolean(eq(MainSettingsActivity.NOTIF_ENABLED_PREF_KEY), any())).then {
            assertEquals(expectedFinalNotifEnabled, it.getArgument(1))
            mockEditor
        }
        `when`(mockEditor.putBoolean(eq(MainSettingsActivity.DARK_MODE_PREF_KEY), any())).then {
            assertEquals(expectedFinalDarkModeEnabled, it.getArgument(1))
            mockEditor
        }
        `when`(mockEditor.apply()).then { /* do nothing */ }
        `when`(mockSharedPreferences.edit()).thenReturn(mockEditor)
        val applicationContext = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(applicationContext, MainSettingsActivity::class.java)
        val activityScenario: ActivityScenario<MainSettingsActivity> =
            ActivityScenario.launch(intent)
        activityScenario.use {
            checkButtonPosition(R.id.main_settings_enable_notif_button, initNotifEnabled)
            checkButtonPosition(R.id.main_settings_dark_mode_button, initDarkModeEnabled)
            onView(withId(clickedButtonId)).perform(click())
            checkButtonPosition(R.id.main_settings_enable_notif_button, expectedFinalNotifEnabled)
            checkButtonPosition(R.id.main_settings_dark_mode_button, expectedFinalDarkModeEnabled)
        }
    }

    private fun checkButtonPosition(buttonId: Int, expectedEnabled: Boolean) {
        val matcher = if (expectedEnabled) isChecked() else isNotChecked()
        onView(withId(buttonId)).check(matches(matcher))
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestDependenciesProvider {

        @Provides
        fun provideSharedPreferences(): SharedPreferences = mockSharedPreferences

        @Provides
        fun provideSensorManager(@ApplicationContext applicationContext: Context): SensorManager =
            DependenciesProvider.provideSensorManager(applicationContext)

    }

    companion object {
        val mockSharedPreferences: SharedPreferences = mock()
    }

}
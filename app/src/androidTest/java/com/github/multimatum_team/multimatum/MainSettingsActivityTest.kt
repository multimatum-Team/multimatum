package com.github.multimatum_team.multimatum

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

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

    private fun testScenario(
        initNotifEnabled: Boolean, initDarkModeEnabled: Boolean,
        clickedButtonId: Int,
        expectedFinalNotifEnabled: Boolean, expectedFinalDarkModeEnabled: Boolean
    ){
        fakeStorage.clear()
        fakeStorage[MainSettingsActivity.NOTIF_ENABLED_PREF_KEY] = initNotifEnabled
        fakeStorage[MainSettingsActivity.DARK_MODE_PREF_KEY] = initDarkModeEnabled
        val applicationContext = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(applicationContext, MainSettingsActivity::class.java)
        val activityScenario: ActivityScenario<MainSettingsActivity> =
            ActivityScenario.launch(intent)
        activityScenario.use {
            onView(withId(clickedButtonId)).perform(click())
            onView(withId(R.id.main_settings_enable_notif_button)).check(matches(if (expectedFinalNotifEnabled) isChecked() else isNotChecked()))
            onView(withId(R.id.main_settings_dark_mode_button)).check(matches(if (expectedFinalDarkModeEnabled) isChecked() else isNotChecked()))
        }
        assertTrue(fakeStorage.contains(MainSettingsActivity.NOTIF_ENABLED_PREF_KEY))
        assertTrue(fakeStorage.contains(MainSettingsActivity.DARK_MODE_PREF_KEY))
        assertEquals(
            expectedFinalNotifEnabled,
            fakeStorage[MainSettingsActivity.NOTIF_ENABLED_PREF_KEY]
        )
        assertEquals(
            expectedFinalDarkModeEnabled,
            fakeStorage[MainSettingsActivity.DARK_MODE_PREF_KEY]
        )
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestDependenciesProvider {

        @Provides
        fun provideSharedPreferences(): SharedPreferences =
            MockSharedPreferencesForMainSettingsActivityTests()

    }

    companion object {
        val fakeStorage = mutableMapOf<String, Boolean>()
    }

    class MockSharedPreferencesForMainSettingsActivityTests(): MockSharedPreferencesBase {

        override fun getBoolean(key: String?, defValue: Boolean): Boolean {
            assertTrue(fakeStorage.containsKey(key))
            return fakeStorage[key]!!
        }

        override fun edit(): SharedPreferences.Editor = Editor()

        class Editor: MockSharedPreferencesBase.Editor {

            override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
                fakeStorage[key!!] = value
                return this
            }

            override fun apply() {  /* Do nothing */  }

        }

    }

}
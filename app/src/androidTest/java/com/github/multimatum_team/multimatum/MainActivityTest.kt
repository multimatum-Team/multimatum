package com.github.multimatum_team.multimatum

import android.content.Context
import android.content.SharedPreferences
import android.hardware.SensorManager
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.rule.GrantPermissionRule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@UninstallModules(DependenciesProvider::class)
@HiltAndroidTest
class MainActivityTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule(order = 2)
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.CAMERA
    )

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun goToQRTest() {
        Intents.init()
        onView(withId(R.id.goToQR)).perform(ViewActions.click())
        Intents.intended(
            allOf(
                hasComponent(QRGenerator::class.java.name),
                toPackage("com.github.multimatum_team.multimatum")
            )
        )
        Intents.release()
    }

    @Test
    fun goToSetting() {
        Intents.init()
        onView(withId(R.id.main_open_settings_but)).perform(ViewActions.click())
        Intents.intended(
            allOf(
                hasComponent(MainSettingsActivity::class.java.name),
                toPackage("com.github.multimatum_team.multimatum")
            )
        )
        Intents.release()
    }

    @Test
    fun launchAccountActivityIntent(){
        Intents.init()
        onView(withId(R.id.logInButton)).perform(ViewActions.click())
        Intents.intended(toPackage("com.github.multimatum_team.multimatum"))
        Intents.release()
    }

    fun buttonOpensQrCodeReader() {
        Intents.init()
        onView(withId(R.id.goToQrCodeReader)).perform(ViewActions.click())
        Intents.intended(
            allOf(
                hasComponent(QRCodeReaderActivity::class.java.name),
                toPackage("com.github.multimatum_team.multimatum")
            )
        )
        Intents.release()
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestDependenciesProvider {

        @Provides
        fun provideSharedPreferences(): SharedPreferences =
            MainSettingsActivityTest.mockSharedPreferences

        @Provides
        fun provideSensorManager(@ApplicationContext applicationContext: Context): SensorManager =
            DependenciesProvider.provideSensorManager(applicationContext)

    }

}
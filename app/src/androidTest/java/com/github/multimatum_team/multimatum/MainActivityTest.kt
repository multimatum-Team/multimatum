package com.github.multimatum_team.multimatum

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.CAMERA
    )

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

}
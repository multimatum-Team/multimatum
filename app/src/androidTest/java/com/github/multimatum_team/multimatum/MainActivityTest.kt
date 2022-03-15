package com.github.multimatum_team.multimatum

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.hamcrest.Matchers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun initDisplayTest() {
        Intents.init()
        onView(withId(R.id.mainGoButton))
            .noActivity()
            .check(matches(isDisplayed()))
        Intents.release()
    }

    @Test
    fun scannerButtonRequestsPermissionWhenNeeded(){
        Intents.init()
        onView(withId(R.id.mainGoButton)).perform(click())
        //onView(withText(arrayOf(android.Manifest.permission.CAMERA)[0])).check(matches(isDisplayed()))
        intended(
            Matchers.allOf(
                hasComponent(MainActivity::class.java.name),
                IntentMatchers.toPackage("com.github.multimatum_team.multimatum")
            )
        )
    }

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.CAMERA
    )

    @Test
    fun goToQrCodeReader() {
        Intents.init()
        onView(withId(R.id.mainGoButton)).perform(click())
        intended(
            Matchers.allOf(
                hasComponent(QRCodeReaderActivity::class.java.name),
                IntentMatchers.toPackage("com.github.multimatum_team.multimatum")
            )
        )
        Intents.release()
    }
}
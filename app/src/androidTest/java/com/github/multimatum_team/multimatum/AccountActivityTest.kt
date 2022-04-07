package com.github.multimatum_team.multimatum

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class SignInActivityTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(SignInActivity::class.java)

    @Test
    fun launchSignInIntentWhenClickingButton() {
        Intents.init()
        onView(withId(R.id.sign_in_button)).perform(ViewActions.click())
        Intents.intended(IntentMatchers.toPackage("com.github.multimatum_team.multimatum"))
        Intents.release()
    }
}
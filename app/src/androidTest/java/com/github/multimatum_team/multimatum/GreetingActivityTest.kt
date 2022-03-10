package com.github.multimatum_team.multimatum

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GreetingActivityTest {

    @Test
    fun helloMessageTest() {
        Intents.init()
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), GreetingActivity::class.java)
        intent.putExtra(EXTRA_NAME, TEST_NAME)
        val activityScenario: ActivityScenario<GreetingActivity> = ActivityScenario.launch(intent)
        activityScenario.use {
            onView(withId(R.id.greetingGreetingDisplay))
                .check(matches(withText("Hello $TEST_NAME!")))
        }
        Intents.release()
    }

    companion object {
        private const val TEST_NAME = "Jack"
    }

}
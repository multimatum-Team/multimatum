package com.github.multimatum_team.multimatum

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)

class FragmentProfileTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(AccountActivity::class.java)

    @Test
    fun `lauch intent when click logout button`(){
        Intents.init()
        onView(withId(R.id.log_out_button)).perform(ViewActions.click())
        Intents.intended(IntentMatchers.toPackage("com.github.multimatum_team.multimatum"))
        Intents.release()
    }
}
package com.github.multimatum_team.multimatum

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.firebase.auth.FirebaseAuth
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class AccountActivityTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(AccountActivity::class.java)

    @Test
    fun launchSignInIntentWhenClickingButton(){
        Intents.init()
        if (FirebaseAuth.getInstance().currentUser != null){
            onView(withId(R.id.log_out_button)).perform(click())
        }
        onView(withId(R.id.sign_in_button)).perform(click())
        Intents.intended(toPackage("com.github.multimatum_team.multimatum"))
        Intents.release()
    }

    /*@Test
    fun checkFragmentCreation(){
        if (FirebaseAuth.getInstance().currentUser != null){
            onView(withId(R.id.log_out_button)).perform(ViewActions.click())
        }
        onView(withId(R.id.sign_in_button)).perform(ViewActions.click())
        Thread.sleep(2000)
        assertNotNull(onView(withId(R.id.profile_frag)))
    }*/
}
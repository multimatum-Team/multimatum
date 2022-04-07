package com.github.multimatum_team.multimatum

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.repository.DeadlineRepository
import com.github.multimatum_team.multimatum.service.ClockService
import com.github.multimatum_team.multimatum.util.MockClockService
import com.github.multimatum_team.multimatum.util.MockDeadlineRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import io.mockk.InternalPlatformDsl.toArray
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.shadows.ShadowDatePickerDialog
import org.robolectric.shadows.ShadowTimePickerDialog
import org.robolectric.shadows.ShadowToast
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Class test for AddDeadlineActivity
 */
@UninstallModules(RepositoryModule::class, ClockModule::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AddDeadlineTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val activityRule = ActivityScenarioRule(AddDeadlineActivity::class.java)

    @Inject
    lateinit var deadlineRepository: DeadlineRepository

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun `The button should send a Toast if there is no title for the deadline`() {
        onView(withId(R.id.add_deadline_select_title))
            .perform(ViewActions.replaceText(""))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.add_deadline_button)).perform(ViewActions.click())
        MatcherAssert.assertThat(ShadowToast.getTextOfLatestToast(),
            CoreMatchers.equalTo(RuntimeEnvironment.getApplication().applicationContext.getString(R.string.enter_a_title)))
    }

    @Test
    fun `The button should add a deadline given a title, a date and a time`() {

        // Select Title
        onView(withId(R.id.add_deadline_select_title))
            .perform(ViewActions.replaceText("Test 1"))
        Espresso.closeSoftKeyboard()

        // Select Date
        onView(withId(R.id.add_deadline_select_date))
            .perform(ViewActions.click())
        val dateDialog = ShadowDatePickerDialog.getLatestDialog() as DatePickerDialog
        dateDialog.updateDate(2013, 10, 23)
        dateDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).performClick()

        // Check if date is correctly selected
        assertEquals("2013-11-23", getText(withId(R.id.add_deadline_text_date)))

        // Select Time
        onView(withId(R.id.add_deadline_select_time))
            .perform(ViewActions.click())
        val timeDialog = ShadowTimePickerDialog.getLatestDialog() as TimePickerDialog
        timeDialog.updateTime(10,10)
        timeDialog.getButton(TimePickerDialog.BUTTON_POSITIVE).performClick()

        // Check if time is correctly selected
        assertEquals("10:10", getText(withId(R.id.add_deadline_text_time)))

        onView(withId(R.id.radio_notification_1h)).perform()

        // Check if Toast correctly appear
        onView(withId(R.id.add_deadline_button)).perform(ViewActions.click())
        MatcherAssert.assertThat(ShadowToast.getTextOfLatestToast(),
            CoreMatchers.equalTo(RuntimeEnvironment.getApplication().applicationContext.getString(R.string.deadline_created)))
    }

    @Test
    fun `add deadline should redirect to main after having add a deadline`(){
        // Select Title
        Intents.init()
        onView(withId(R.id.add_deadline_select_title))
            .perform(ViewActions.replaceText("Test redirect"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.add_deadline_button)).perform(ViewActions.click())
        Intents.intended(
            Matchers.allOf(
                IntentMatchers.hasComponent(MainActivity::class.java.name),
                IntentMatchers.toPackage("com.github.multimatum_team.multimatum")
            )
        )
        Intents.release()
    }

    /*
    Matcher to recuperate text from TextView based on:
    https://stackoverflow.com/questions/23381459/how-to-get-text-from-textview-using-espresso
     */
    private fun getText(matcher: Matcher<View?>?): String? {
        val stringHolder = arrayOf<String?>(null)
        onView(matcher).perform(object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return isAssignableFrom(TextView::class.java)
            }

            override fun getDescription(): String {
                return "getting text from a TextView"
            }

            override fun perform(uiController: UiController?, view: View) {
                val tv = view as TextView //Save, because of check in getConstraints()
                stringHolder[0] = tv.text.toString()
            }
        })
        return stringHolder[0]
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestDeadlineRepositoryModule {
        @Singleton
        @Provides
        fun provideDeadlineRepository(): DeadlineRepository =
            MockDeadlineRepository(listOf())
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestClockModule {
        @Provides
        fun provideClockService(): ClockService =
            MockClockService(LocalDateTime.of(2022, 3, 12, 0, 0))
    }


}
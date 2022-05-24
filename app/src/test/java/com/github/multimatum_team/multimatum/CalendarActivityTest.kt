package com.github.multimatum_team.multimatum

import android.view.KeyEvent
import android.view.View
import android.widget.ListView
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.events.calendar.views.EventsCalendar
import com.github.multimatum_team.multimatum.activity.CalendarActivity
import com.github.multimatum_team.multimatum.activity.DeadlineDetailsActivity
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.github.multimatum_team.multimatum.repository.*
import com.github.multimatum_team.multimatum.service.ClockService
import com.github.multimatum_team.multimatum.util.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import java.util.*
import javax.inject.Singleton

@UninstallModules(FirebaseRepositoryModule::class, ClockModule::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CalendarActivityTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val activityRule = ActivityScenarioRule(CalendarActivity::class.java)

    @Before
    fun setUp() {
        Intents.init()
        hiltRule.inject()
    }

    @After
    fun teardown() {
        Intents.release()
    }

    @Test
    fun `we should be able to select a date, show the deadline from this date and go to the details of the deadline`(){
        activityRule.scenario.onActivity { activity ->
            Espresso.onView(withId(R.id.calendar_view_listView))
                .check(matches(withListSize(0)))
            val selectDate = Calendar.getInstance()
            selectDate.set(2022,2,13)
            activity
                .findViewById<EventsCalendar>(R.id.calendar_view)
                .setCurrentSelectedDate(selectDate)

            Espresso.onView(withId(R.id.calendar_view_listView))
                .check(matches(withListSize(1)))

            Espresso.onData(Matchers.anything()).inAdapterView(withId(R.id.calendar_view_listView)).atPosition(0)
                .perform(longClick())

            Intents.intended(
                Matchers.allOf(
                    IntentMatchers.hasComponent(DeadlineDetailsActivity::class.java.name),
                    IntentMatchers.hasExtra(
                        "com.github.multimatum_team.deadline.details.id",
                        "0"
                    )
                )
            )
        }

    }

    @Test
    fun `text input field should be clickable`() {
        Espresso.onView(withId(R.id.textInputEditCalendar))
            .check(matches(isClickable()))
    }

    @Test
    fun `text input screen should be released after adding deadline with button`() {
        Espresso.onView(withId(R.id.textInputEditCalendar))
            .perform(click())
            .perform(typeText("deadlineTestCase"))
        Espresso.onView(withId(R.id.calendar_add_deadline_button))
            .perform(click())
        Espresso.onView(withId(R.id.textInputEditCalendar))
            .check(matches(isClickable()))
    }

    @Test
    fun `text input screen should be released after adding deadline with enter key`() {
        Espresso.onView(withId(R.id.textInputEditCalendar))
            .perform(click())
            .perform(typeText("deadlineTestCase2"))
            .perform(pressKey(KeyEvent.KEYCODE_ENTER))
        Espresso.onView(withId(R.id.textInputEditCalendar))
            .check(matches(isClickable()))
    }

    @Test
    fun `text input screen should be released after adding deadline with done on SoftKeyboard`() {
        Espresso.onView(withId(R.id.textInputEditCalendar))
            .perform(click())
            .perform(typeText("deadlineTestCase3"))
            .perform(pressImeActionButton())
        Espresso.onView(withId(R.id.textInputEditCalendar))
            .check(matches(isClickable()))
    }

    /*
   ListView matcher for size found in:
  https://stackoverflow.com/questions/30361068/assert-proper-number-of-items-in-list-with-espresso
    */
    private fun withListSize(size: Int): Matcher<in View> {
        return object : TypeSafeMatcher<View?>() {
            override fun matchesSafely(view: View?): Boolean {
                return (view as ListView).count == size
            }

            override fun describeTo(description: Description) {
                description.appendText("ListView should have $size items")
            }

        }
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestRepositoryModule {
        @Singleton
        @Provides
        fun provideDeadlineRepository(): DeadlineRepository =
            MockDeadlineRepository(
                listOf(
                    Deadline(
                        "Test1",
                        DeadlineState.TODO,
                        LocalDateTime.of(2022, 3, 13, 0, 0)
                    )
                )
            )

        @Singleton
        @Provides
        fun provideGroupRepository(): GroupRepository =
            MockGroupRepository(listOf())

        @Singleton
        @Provides
        fun provideAuthRepository(): AuthRepository =
            MockAuthRepository()

        @Singleton
        @Provides
        fun provideUserRepository(): UserRepository =
            MockUserRepository(listOf())

        @Singleton
        @Provides
        fun providePdfRepository(): PdfRepository =
            MockPdfRepository()
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestClockModule {
        @Provides
        fun provideClockService(): ClockService =
            MockClockService(LocalDateTime.of(2022, 3, 12, 0, 0))
    }
}
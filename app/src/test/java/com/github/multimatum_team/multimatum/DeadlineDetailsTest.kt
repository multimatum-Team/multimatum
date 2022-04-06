package com.github.multimatum_team.multimatum

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.github.multimatum_team.multimatum.service.ClockService
import com.github.multimatum_team.multimatum.util.MockClockService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject


/**
 * Tests for the DeadlineDetailsActivity class
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@UninstallModules(ClockModule::class)
class DeadlineDetailsTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var clockService: ClockService

    @Before
    @Throws(Exception::class)
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun `Given a deadline not yet due or done, the activity should display it`() {
        val intent = DeadlineDetailsActivity.newIntent(
            ApplicationProvider.getApplicationContext(),
            "1",
            Deadline("Test 1", DeadlineState.TODO, clockService.now().plusDays(7))
        )
        ActivityScenario.launch<DeadlineDetailsActivity>(intent)
        onView(withId(R.id.deadline_details_activity_title)).check(matches(withText("Test 1")))
        onView(withId(R.id.deadline_details_activity_date))
            .check(matches(withText("Due the ${clockService.now().plusDays(7)}"))
        )
        onView(withId(R.id.deadline_details_activity_done_or_due)).check(matches(withText("Due in 7 Days")))
    }

    @Test
    fun `Given a deadline already done, the activity should display it`() {
        val intent = DeadlineDetailsActivity.newIntent(
            ApplicationProvider.getApplicationContext(),
            "2",
            Deadline("Test 2", DeadlineState.DONE, clockService.now().plusDays(7))
        )
        ActivityScenario.launch<DeadlineDetailsActivity>(intent)
        onView(withId(R.id.deadline_details_activity_title)).check(matches(withText("Test 2")))
        onView(withId(R.id.deadline_details_activity_date))
            .check(matches(withText("Due the ${clockService.now().plusDays(7)}")))
        onView(withId(R.id.deadline_details_activity_done_or_due)).check(matches(withText("Done")))
    }

    @Test
    fun `Given a deadline already due, the activity should display it`() {
        val intent = DeadlineDetailsActivity.newIntent(
            ApplicationProvider.getApplicationContext(),
            "3",
            Deadline("Test 3", DeadlineState.TODO, clockService.now().minusDays(2))
        )
        ActivityScenario.launch<DeadlineDetailsActivity>(intent)
        onView(withId(R.id.deadline_details_activity_title)).check(matches(withText("Test 3")))
        onView(withId(R.id.deadline_details_activity_date))
            .check(matches(withText("Due the ${clockService.now().minusDays(2)}")))
        onView(withId(R.id.deadline_details_activity_done_or_due)).check(matches(withText("Is already Due")))
    }

    @Test
    fun `Test launching intent to go to generator`(){
        val intent = DeadlineDetailsActivity.newIntent(
            ApplicationProvider.getApplicationContext(),
            "4",
            Deadline("Test 4", DeadlineState.TODO, clockService.now().minusDays(1))
        )
        ActivityScenario.launch<DeadlineDetailsActivity>(intent)
        Intents.init()
        onView(withId(R.id.QRCodeButton)).perform(click())
        Intents.intending(allOf(hasComponent(QRGeneratorActivity::class.java.name),
            hasExtra("com.github.multimatum_team.multimatum.deadline.details.id", "4"),
            toPackage("com.github.multimatum_team.multimatum")))
        Intents.release()
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestClockModule {
        @Provides
        fun provideClockService(): ClockService =
            MockClockService(LocalDateTime.of(2022, 3, 12, 0, 0))
    }
}
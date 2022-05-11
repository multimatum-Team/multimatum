package com.github.multimatum_team.multimatum

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.activity.DeadlineDetailsActivity
import com.github.multimatum_team.multimatum.activity.QRGeneratorActivity
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.github.multimatum_team.multimatum.repository.AuthRepository
import com.github.multimatum_team.multimatum.repository.DeadlineRepository
import com.github.multimatum_team.multimatum.repository.GroupRepository
import com.github.multimatum_team.multimatum.repository.UserRepository
import com.github.multimatum_team.multimatum.service.ClockService
import com.github.multimatum_team.multimatum.util.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.shadows.ShadowAlertDialog
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tests for the DeadlineDetailsActivity class
 */
@UninstallModules(FirebaseRepositoryModule::class, ClockModule::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class DeadlineDetailsTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var clockService: ClockService

    @Before
    @Throws(Exception::class)
    fun setUp() {
        Intents.init()
        hiltRule.inject()
    }

    @After
    fun teardown() {
        Intents.release()
    }

    @Test
    fun `Given a deadline not yet due or done, the activity should display it`() {
        val intent =
            DeadlineDetailsActivity.newIntent(ApplicationProvider.getApplicationContext(), "0")
        val scenario = ActivityScenario.launch<DeadlineDetailsActivity>(intent)
        scenario.use {
            onView(withId(R.id.deadline_details_activity_title)).check(matches(withText("Test 1")))
            onView(withId(R.id.deadline_details_activity_date))
                .check(
                    matches(
                        withText(
                            "Due the ${clockService.now().plusDays(7).toLocalDate()} " +
                                    "at ${clockService.now().plusDays(7).toLocalTime()}"
                        )
                    )
                )

            onView(withId(R.id.deadline_details_activity_done_or_due)).check(matches(withText("Due in 7 Days")))
        }
    }

    @Test
    fun `Given a deadline already done, the activity should display it`() {
        val intent =
            DeadlineDetailsActivity.newIntent(ApplicationProvider.getApplicationContext(), "1")
        val scenario = ActivityScenario.launch<DeadlineDetailsActivity>(intent)
        scenario.use {
            onView(withId(R.id.deadline_details_activity_title)).check(matches(withText("Test 2")))
            onView(withId(R.id.deadline_details_activity_date))
                .check(
                    matches(
                        withText(
                            "Due the ${clockService.now().plusDays(7).toLocalDate()} " +
                                    "at ${clockService.now().plusDays(7).toLocalTime()}"
                        )
                    )
                )

            onView(withId(R.id.deadline_details_activity_done_or_due)).check(matches(withText("Done")))
        }
    }

    @Test
    fun `Given a deadline already due, the activity should display it`() {
        val intent =
            DeadlineDetailsActivity.newIntent(ApplicationProvider.getApplicationContext(), "2")
        val scenario = ActivityScenario.launch<DeadlineDetailsActivity>(intent)
        scenario.use {
            onView(withId(R.id.deadline_details_activity_title)).check(matches(withText("Test 3")))
            onView(withId(R.id.deadline_details_activity_date))
                .check(
                    matches(
                        withText(
                            "Due the ${clockService.now().minusDays(2).toLocalDate()} " +
                                    "at ${clockService.now().minusDays(2).toLocalTime()}"
                        )
                    )
                )

            onView(withId(R.id.deadline_details_activity_done_or_due)).check(matches(withText("Is already Due")))
        }
    }

    @Test
    fun `Test launching intent to go to generator`() {
        val intent =
            DeadlineDetailsActivity.newIntent(ApplicationProvider.getApplicationContext(), "3")
        val scenario = ActivityScenario.launch<DeadlineDetailsActivity>(intent)
        scenario.use {
            onView(withId(R.id.QRCodeButton)).perform(click())
            Intents.intending(
                allOf(
                    hasComponent(QRGeneratorActivity::class.java.name),
                    hasExtra("com.github.multimatum_team.multimatum.deadline.details.id", "3"),
                    toPackage("com.github.multimatum_team.multimatum")
                )
            )
        }
    }

    @Test
    fun `Given a deadline with only a few hours left, the activity should display it`() {
        val intent =
            DeadlineDetailsActivity.newIntent(ApplicationProvider.getApplicationContext(), "3")
        val scenario = ActivityScenario.launch<DeadlineDetailsActivity>(intent)
        scenario.use {
            onView(withId(R.id.deadline_details_activity_title)).check(matches(withText("Test 4")))
            onView(withId(R.id.deadline_details_activity_date))
                .check(
                    matches(
                        withText(
                            "Due the ${clockService.now().plusHours(6).toLocalDate()} " +
                                    "at ${clockService.now().plusHours(6).toLocalTime()}"
                        )
                    )
                )
            onView(withId(R.id.deadline_details_activity_done_or_due)).check(matches(withText("Due in 6 Hours")))
            onView(withId(R.id.deadline_details_activity_description)).check(matches(withText("Do not panic, this is a test")))
        }
    }

    @Test
    fun `Given a deadline, we can modify it`() {
        val intent =
            DeadlineDetailsActivity.newIntent(ApplicationProvider.getApplicationContext(), "3")
        val scenario = ActivityScenario.launch<DeadlineDetailsActivity>(intent)
        scenario.use {
            // Go in Modify Mode
            onView(withId(R.id.deadline_details_activity_modify)).perform(click())

            // Modify the text
            onView(withId(R.id.deadline_details_activity_title)).perform(ViewActions.replaceText("Test 66"))
            Espresso.closeSoftKeyboard()
            onView(withId(R.id.deadline_details_activity_title)).check(matches(withText("Test 66")))

            // Modify the date
            onView(withId(R.id.deadline_details_activity_date))
                .perform(click())
            val dateDialog = ShadowAlertDialog.getLatestDialog() as DatePickerDialog
            dateDialog.updateDate(2022, 10, 23)
            dateDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).performClick()

            // An action is necessary to let the time to ShadowAlertDialog to find the TimeDialog
            onView(withId(R.id.deadline_details_activity_set_done)).perform(click())

            // Modify the time
            val timeDialog = ShadowAlertDialog.getLatestDialog() as TimePickerDialog
            timeDialog.updateTime(10, 10)
            timeDialog.getButton(TimePickerDialog.BUTTON_POSITIVE).performClick()

            // Check the date and the time
            onView(withId(R.id.deadline_details_activity_date))
                .check(matches(withText("Due the 2022-11-23 at 10:10")))

            // Modify the done
            onView(withId(R.id.deadline_details_activity_set_done)).perform(click())
            onView(withId(R.id.deadline_details_activity_done_or_due)).check(matches(withText("Done")))

            // Modify the description
            onView(withId(R.id.deadline_details_activity_description)).perform(
                ViewActions.replaceText(
                    "Test 66"
                )
            )
            Espresso.closeSoftKeyboard()
            onView(withId(R.id.deadline_details_activity_description)).check(matches(withText("Test 66")))

            //Go back to Normal Mode
            onView(withId(R.id.deadline_details_activity_modify)).perform(click())
        }
    }

    @Test
    fun `Changing the notifications change the displayed text`() {
        val intent =
            DeadlineDetailsActivity.newIntent(ApplicationProvider.getApplicationContext(), "3")
        val scenario = ActivityScenario.launch<DeadlineDetailsActivity>(intent)
        scenario.use {
            // Check that no alarm is planned
            onView(withId(R.id.deadline_details_activity_notifications)).check(matches(withText("No Alarm Planned")))

            // Go in Modify Mode
            onView(withId(R.id.deadline_details_activity_modify)).perform(click())

            // Add a notification and check the displayed information
            onView(withId(R.id.deadline_details_activity_notifications)).perform(click())
            var dialog = ShadowAlertDialog.getLatestAlertDialog()
            // Only way found to click on the correct area
            dialog.listView.performItemClick(dialog.listView.adapter.getView(0, null, null), 0, 0)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
            onView(withId(R.id.deadline_details_activity_notifications)).check(matches(withText("Alarm 1 hour before")))

            // Add another notification and check the displayed information
            onView(withId(R.id.deadline_details_activity_notifications)).perform(click())
            dialog = ShadowAlertDialog.getLatestAlertDialog()
            // Only way found to click on the correct area
            dialog.listView.performItemClick(dialog.listView.adapter.getView(1, null, null), 1, 0)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
            onView(withId(R.id.deadline_details_activity_notifications)).check(matches(withText("Alarm 1 hour and 5 hours before")))

            // Add another notification and check the displayed information
            onView(withId(R.id.deadline_details_activity_notifications)).perform(click())
            dialog = ShadowAlertDialog.getLatestAlertDialog()
            // Only way found to click on the correct area
            dialog.listView.performItemClick(dialog.listView.adapter.getView(2, null, null), 2, 0)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
            onView(withId(R.id.deadline_details_activity_notifications)).check(matches(withText("Alarm 1 hour, 5 hours and 1 day before")))

            // Add a last notification and check the displayed information
            onView(withId(R.id.deadline_details_activity_notifications)).perform(click())
            dialog = ShadowAlertDialog.getLatestAlertDialog()
            // Only way found to click on the correct area
            dialog.listView.performItemClick(dialog.listView.adapter.getView(3, null, null), 3, 0)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
            onView(withId(R.id.deadline_details_activity_notifications)).check(matches(withText("Alarm 1 hour, 5 hours, 1 day and 3 days before")))
        }
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestClockModule {
        @Provides
        fun provideClockService(): ClockService =
            MockClockService(LocalDateTime.of(2022, 3, 12, 0, 0))
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestRepositoryModule {
        @Singleton
        @Provides
        fun provideDeadlineRepository(): DeadlineRepository =
            MockDeadlineRepository(
                listOf(
                    Deadline("Test 1", DeadlineState.TODO, LocalDateTime.of(2022, 3, 19, 0, 0)),
                    Deadline("Test 2", DeadlineState.DONE, LocalDateTime.of(2022, 3, 19, 0, 0)),
                    Deadline("Test 3", DeadlineState.TODO, LocalDateTime.of(2022, 3, 10, 0, 0)),
                    Deadline(
                        "Test 4",
                        DeadlineState.TODO,
                        LocalDateTime.of(2022, 3, 12, 6, 0),
                        "Do not panic, this is a test"
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
    }
}
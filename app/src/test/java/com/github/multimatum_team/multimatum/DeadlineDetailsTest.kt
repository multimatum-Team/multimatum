package com.github.multimatum_team.multimatum

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.activity.DeadlineDetailsActivity
import com.github.multimatum_team.multimatum.activity.DisplayLocationActivity
import com.github.multimatum_team.multimatum.activity.QRGeneratorActivity
import com.github.multimatum_team.multimatum.model.*
import com.github.multimatum_team.multimatum.repository.*
import com.github.multimatum_team.multimatum.service.ClockService
import com.github.multimatum_team.multimatum.util.*
import com.github.multimatum_team.multimatum.util.DeadlineNotification
import com.google.firebase.firestore.GeoPoint
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import org.robolectric.shadows.ShadowAlertDialog
import java.time.Duration
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
            onView(withId(R.id.deadline_details_activity_group)).check(matches(withText("Not in any group")))
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

    @Test
    fun `a deadline in a group where the user is the owner should be modifiable`() {
        val intent =
            DeadlineDetailsActivity.newIntent(ApplicationProvider.getApplicationContext(), "4")
        val scenario = ActivityScenario.launch<DeadlineDetailsActivity>(intent)
        scenario.use {
            onView(withId(R.id.deadline_details_activity_group)).check(matches(withText("In the group: Group 1")))
            onView(withId(R.id.deadline_details_activity_modify)).check(matches(isDisplayed()))
        }

    }

    @Test
    fun `a deadline in a group where the user is not the owner should not be modifiable`() {
        val intent =
            DeadlineDetailsActivity.newIntent(ApplicationProvider.getApplicationContext(), "5")
        val scenario = ActivityScenario.launch<DeadlineDetailsActivity>(intent)
        scenario.use {
            onView(withId(R.id.deadline_details_activity_group)).check(matches(withText("In the group: Group 2")))
            onView(withId(R.id.deadline_details_activity_modify)).check(matches(not(isDisplayed())))
        }
    }

    @Test
    fun `The map is opened on request when a location is provided in the deadline`() {
        val intent =
            DeadlineDetailsActivity.newIntent(ApplicationProvider.getApplicationContext(), "6")
        val scenario = ActivityScenario.launch<DeadlineDetailsActivity>(intent)
        scenario.use {
            onView(withId(R.id.display_location_on_map)).perform(click())
            Intents.intended(
                allOf(
                    hasComponent(DisplayLocationActivity::class.java.name),
                    toPackage("com.github.multimatum_team.multimatum")
                )
            )
        }
    }

    @Test
    fun `a deadline with already defined notification should show them`() {
        // Setup 5 notifications
        DeadlineNotification.editNotification(
            "0",
            Deadline("Test 1", DeadlineState.TODO, LocalDateTime.of(2022, 3, 19, 0, 0)),
            listOf(
                Duration.ofHours(2).toMillis(),
                Duration.ofHours(3).toMillis(),
                Duration.ofHours(4).toMillis(),
                Duration.ofDays(2).toMillis(),
                Duration.ofDays(4).toMillis()
            ),
            ApplicationProvider.getApplicationContext()
        )
        val intent =
            DeadlineDetailsActivity.newIntent(ApplicationProvider.getApplicationContext(), "0")
        val scenario = ActivityScenario.launch<DeadlineDetailsActivity>(intent)
        scenario.use {
            // Check that no alarm is planned
            onView(withId(R.id.deadline_details_activity_notifications)).check(matches(withText("Multiple Alarms Planned")))

            // Go in Modify Mode
            onView(withId(R.id.deadline_details_activity_modify)).perform(click())

            // Add a notification and check the displayed information
            onView(withId(R.id.deadline_details_activity_notifications)).perform(click())
            val dialog = ShadowAlertDialog.getLatestAlertDialog()
            Assert.assertEquals(
                "2 hours before",
                (dialog.listView.adapter.getView(4, null, null) as TextView).text
            )
            Assert.assertEquals(
                "3 hours before",
                (dialog.listView.adapter.getView(5, null, null) as TextView).text
            )
            Assert.assertEquals(
                "4 hours before",
                (dialog.listView.adapter.getView(6, null, null) as TextView).text
            )
            Assert.assertEquals(
                "2 days before",
                (dialog.listView.adapter.getView(7, null, null) as TextView).text
            )
            Assert.assertEquals(
                "4 days before",
                (dialog.listView.adapter.getView(8, null, null) as TextView).text
            )
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
                    ),
                    Deadline(
                        "Test 5",
                        DeadlineState.TODO,
                        LocalDateTime.of(2022, 3, 12, 6, 0),
                        "Deadline for testing owned group",
                        GroupOwned("0")
                    ),

                    Deadline(
                        "Test 6",
                        DeadlineState.TODO,
                        LocalDateTime.of(2022, 3, 12, 6, 0),
                        "Deadline for testing not owned group",
                        GroupOwned("1")
                    ),
                    Deadline(
                        "Test 7",
                        DeadlineState.TODO,
                        LocalDateTime.of(2022, 9, 12, 6, 0),
                        "Deadline to test the map",
                        locationName = "EPFL",
                        location = GeoPoint(46.5191, 6.5668)
                    )
                )
            )

        @Singleton
        @Provides
        fun provideGroupRepository(): GroupRepository =
            MockGroupRepository(
                listOf(
                    UserGroup("0", "Group 1", "0"),
                    UserGroup("1", "Group 2", "1", setOf("0", "1"))
                )
            )

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
}
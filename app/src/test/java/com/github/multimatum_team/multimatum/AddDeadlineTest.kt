package com.github.multimatum_team.multimatum

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.view.View
import android.widget.TextView
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.activity.AddDeadlineActivity
import com.github.multimatum_team.multimatum.model.UserGroup
import com.github.multimatum_team.multimatum.repository.AuthRepository
import com.github.multimatum_team.multimatum.repository.DeadlineRepository
import com.github.multimatum_team.multimatum.repository.GroupRepository
import com.github.multimatum_team.multimatum.service.ClockService
import com.github.multimatum_team.multimatum.util.MockAuthRepository
import com.github.multimatum_team.multimatum.util.MockClockService
import com.github.multimatum_team.multimatum.util.MockDeadlineRepository
import com.github.multimatum_team.multimatum.util.MockGroupRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.shadows.ShadowAlertDialog
import org.robolectric.shadows.ShadowDatePickerDialog
import org.robolectric.shadows.ShadowTimePickerDialog
import org.robolectric.shadows.ShadowToast
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Class test for AddDeadlineActivity
 */
@UninstallModules(FirebaseRepositoryModule::class, ClockModule::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AddDeadlineTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val activityRule = ActivityScenarioRule(AddDeadlineActivity::class.java)

    @Inject
    lateinit var deadlineRepository: DeadlineRepository

    @Inject
    lateinit var groupRepository: GroupRepository

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
    fun `The button Add should send a Toast if there is no title for the deadline`() {
        onView(withId(R.id.add_deadline_select_title))
            .perform(replaceText(""))
        Espresso.closeSoftKeyboard()

        onView(withId(R.id.add_deadline_button)).perform(click())
        MatcherAssert.assertThat(
            ShadowToast.getTextOfLatestToast(),
            CoreMatchers.equalTo(RuntimeEnvironment.getApplication().applicationContext.getString(R.string.enter_a_title))
        )
    }

    @Test
    fun `The Button Select Group should show only owned group`() {
        onView(withId(R.id.add_deadline_select_group))
            .perform(click())

        val dialog = ShadowAlertDialog.getLatestAlertDialog()
        assertEquals("No group", (dialog.listView.adapter.getView(0, null, null) as TextView).text)

    }

    @Test
    fun `The button Add should add a deadline given a title, a date and a time`() {

        // Select Title
        onView(withId(R.id.add_deadline_select_title))
            .perform(replaceText("Test 1"))
        Espresso.closeSoftKeyboard()

        // Select Date
        onView(withId(R.id.add_deadline_select_date))
            .perform(click())
        val dateDialog = ShadowDatePickerDialog.getLatestDialog() as DatePickerDialog
        dateDialog.updateDate(2013, 10, 23)
        dateDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).performClick()

        // Check if date is correctly selected
        assertEquals("2013-11-23", getText(withId(R.id.add_deadline_text_date)))

        // Select Time
        onView(withId(R.id.add_deadline_select_time))
            .perform(click())
        val timeDialog = ShadowTimePickerDialog.getLatestDialog() as TimePickerDialog
        timeDialog.updateTime(10, 10)
        timeDialog.getButton(TimePickerDialog.BUTTON_POSITIVE).performClick()

        // Check if time is correctly selected
        assertEquals("10:10", getText(withId(R.id.add_deadline_text_time)))

        // Select Description
        onView(withId(R.id.add_deadline_select_description))
            .perform(replaceText("This is a test, do not panic."))
        Espresso.closeSoftKeyboard()

        // Select Notifications
        onView(withId(R.id.add_deadline_select_notification)).perform(click())
        val dialog = ShadowAlertDialog.getLatestAlertDialog()
        dialog.listView.performItemClick(dialog.listView.adapter.getView(0, null, null), 0, 0)
        dialog.listView.performItemClick(dialog.listView.adapter.getView(1, null, null), 1, 0)
        dialog.listView.performItemClick(dialog.listView.adapter.getView(2, null, null), 2, 0)
        dialog.listView.performItemClick(dialog.listView.adapter.getView(3, null, null), 3, 0)
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick()

        // Check if Toast correctly appear
        onView(withId(R.id.add_deadline_button)).perform(click())
        MatcherAssert.assertThat(
            ShadowToast.getTextOfLatestToast(),
            CoreMatchers.equalTo(RuntimeEnvironment.getApplication().applicationContext.getString(R.string.deadline_created))
        )

    }

    // TODO: This test was removed because I replaced the startIntent to the MainActivity with a
    //  call to finish() which cannot be tested
    /*
    @Test
    fun `add deadline should redirect to main after having add a deadline`() {
        // Select Title
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
    }
    */

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
    object TestRepositoryModule {
        @Singleton
        @Provides
        fun provideDeadlineRepository(): DeadlineRepository =
            MockDeadlineRepository(listOf())

        @Singleton
        @Provides
        fun provideGroupRepository(): GroupRepository =
            MockGroupRepository(
                listOf(
                    UserGroup("0", "Group 1", "0"),
                    UserGroup("1", "Group 2", "0")
                )
            )

        @Singleton
        @Provides
        fun provideAuthRepository(): AuthRepository =
            MockAuthRepository()
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestClockModule {
        @Provides
        fun provideClockService(): ClockService =
            MockClockService(LocalDateTime.of(2022, 3, 12, 0, 0))
    }
}
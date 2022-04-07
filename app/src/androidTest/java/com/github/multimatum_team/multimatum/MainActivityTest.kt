package com.github.multimatum_team.multimatum

import android.content.Context
import android.content.SharedPreferences
import android.hardware.SensorManager
import android.os.Build
import android.view.View
import android.widget.ListView
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.github.multimatum_team.multimatum.repository.AuthRepository
import com.github.multimatum_team.multimatum.repository.DeadlineRepository
import com.github.multimatum_team.multimatum.util.MockAuthRepository
import com.github.multimatum_team.multimatum.util.MockDeadlineRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.anything
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import java.time.LocalDateTime
import javax.inject.Singleton

@UninstallModules(DependenciesProvider::class, RepositoryModule::class)
@HiltAndroidTest
class MainActivityTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun init() {
        hiltRule.inject()
        Intents.init()
    }

    @After
    fun release() {
        Intents.release()
    }

    @Test
    fun goToQRTest() {
        onView(withId(R.id.goToQR)).perform(ViewActions.click())
        Intents.intended(
            allOf(
                hasComponent(QRGenerator::class.java.name),
                toPackage("com.github.multimatum_team.multimatum")
            )
        )
    }

    @Test
    fun goToSetting() {
        onView(withId(R.id.main_open_settings_but)).perform(ViewActions.click())
        Intents.intended(
            allOf(
                hasComponent(MainSettingsActivity::class.java.name),
                toPackage("com.github.multimatum_team.multimatum")
            )
        )
    }

    @Test
    fun goToDeadlineDetails() {
        onData(anything()).inAdapterView(withId(R.id.deadlineListView)).atPosition(0)
            .perform(longClick())

        Intents.intended(
            allOf(
                hasComponent(DeadlineDetailsActivity::class.java.name),
                hasExtra("com.github.multimatum_team.multimatum.deadline.details.title", "Test 1"),
                hasExtra(
                    "com.github.multimatum_team.multimatum.deadline.details.date",
                    LocalDateTime.of(2022, 3, 1, 0, 0)
                ),
                hasExtra(
                    "com.github.multimatum_team.multimatum.deadline.details.state",
                    DeadlineState.TODO
                ),
                toPackage("com.github.multimatum_team.multimatum")
            )
        )

    }

    @Test
    fun goToCalendar() {
        onView(withId(R.id.goToCalendarButton)).perform(ViewActions.click())
        Intents.intended(
            allOf(
                hasComponent(CalendarActivity::class.java.name),
                toPackage("com.github.multimatum_team.multimatum")
            )
        )
    }

    @Test
    fun goToAddDeadlineActivity() {
        onView(withId(R.id.main_go_to_add_deadline)).perform(ViewActions.click())
        Intents.intended(
            allOf(
                hasComponent(AddDeadlineActivity::class.java.name),
                toPackage("com.github.multimatum_team.multimatum")
            )
        )
    }

    @Test
    fun buttonOpensQrCodeReader() {
        onView(withId(R.id.goToQrCodeReader)).perform(ViewActions.click())
        grantPermission()
        Intents.intended(
            allOf(
                hasComponent(QRCodeReaderActivity::class.java.name),
                toPackage("com.github.multimatum_team.multimatum")
            )
        )
    }

    @Test
    fun buttonDoesNotOpenQrCodeReaderIfPermissionNotGranted() {
        onView(withId(R.id.goToQrCodeReader)).perform(ViewActions.click())
        denyPermission()
        onView(withId(R.id.goToQrCodeReader)).check(matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun swipeDeadlineTwiceShouldDelete() {
        onData(anything()).inAdapterView(withId(R.id.deadlineListView)).atPosition(0)
            .perform(swipeLeft())
        onData(anything()).inAdapterView(withId(R.id.deadlineListView)).atPosition(0)
            .perform(swipeLeft())
        onView(withId(R.id.deadlineListView)).check(matches(withListSize(2)))
    }

    @Test
    fun swipeDeadlineOnceAndClickUndoShouldUndo() {
        onData(anything()).inAdapterView(withId(R.id.deadlineListView)).atPosition(0)
            .perform(swipeLeft())
        onData(anything()).inAdapterView(withId(R.id.deadlineListView)).atPosition(0)
            .perform(ViewActions.click())
        onView(withId(R.id.deadlineListView)).check(matches(withListSize(3)))
    }

    /*
    ListView matcher for size found in:
   https://stackoverflow.com/questions/30361068/assert-proper-number-of-items-in-list-with-espresso
     */
    private fun withListSize(size: Int): Matcher<in View>? {
        return object : TypeSafeMatcher<View?>() {
            override fun matchesSafely(view: View?): Boolean {
                return (view as ListView).count == size
            }

            override fun describeTo(description: Description) {
                description.appendText("ListView should have $size items")
            }

        }
    }

    /*
    The two following helper functions have been found online at:
    https://alexzh.com/ui-testing-of-android-runtime-permissions/
     */
    private fun grantPermission() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val allowPermission = UiDevice.getInstance(instrumentation).findObject(
            UiSelector().text(
                when {
                    Build.VERSION.SDK_INT <= 28 -> "ALLOW"
                    Build.VERSION.SDK_INT == 29 -> "Allow only while using the app"
                    else -> "While using the app"
                }
            )
        )
        if (allowPermission.exists()) {
            allowPermission.click()
        }
    }

    private fun denyPermission() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val denyPermission = UiDevice.getInstance(instrumentation).findObject(
            UiSelector().text(
                when (Build.VERSION.SDK_INT) {
                    in 26..28 -> "DENY"
                    else -> "Deny"
                }
            )
        )
        if (denyPermission.exists()) {
            denyPermission.click()
        }
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestDependenciesProvider {

        @Provides
        fun provideSharedPreferences(): SharedPreferences =
            mockSharedPreferences

        @Provides
        fun provideSensorManager(@ApplicationContext applicationContext: Context): SensorManager =
            DependenciesProvider.provideSensorManager(applicationContext)

    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestDeadlineRepositoryModule {
        @Singleton
        @Provides
        fun provideDeadlineRepository(): DeadlineRepository =
            MockDeadlineRepository(
                listOf(
                    Deadline("Test 1", DeadlineState.TODO, LocalDateTime.of(2022, 3, 1, 0, 0)),
                    Deadline("Test 2", DeadlineState.DONE, LocalDateTime.of(2022, 3, 30, 0, 0)),
                    Deadline("Test 3", DeadlineState.TODO, LocalDateTime.of(2022, 3, 7, 0, 0))
                )
            )

        @Singleton
        @Provides
        fun provideAuthRepository(): AuthRepository =
            MockAuthRepository()
    }

    companion object {
        val mockSharedPreferences: SharedPreferences = mock()
    }
}
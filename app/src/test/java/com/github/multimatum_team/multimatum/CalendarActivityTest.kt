package com.github.multimatum_team.multimatum

import android.view.KeyEvent
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.activity.CalendarActivity
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
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
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
    fun `text input field should be clickable`() {
        Espresso.onView(ViewMatchers.withId(R.id.textInputEditCalendar))
            .check(ViewAssertions.matches(ViewMatchers.isClickable()))
    }

    @Test
    fun `text input screen should be released after adding deadline with button`() {
        Espresso.onView(ViewMatchers.withId(R.id.textInputEditCalendar))
            .perform(click())
            .perform(typeText("deadlineTestCase"))
        Espresso.onView(ViewMatchers.withId(R.id.calendar_add_deadline_button))
            .perform(click())
        Espresso.onView(ViewMatchers.withId(R.id.textInputEditCalendar))
            .check(ViewAssertions.matches(ViewMatchers.isClickable()))
    }

    @Test
    fun `text input screen should be released after adding deadline with enter key`() {
        Espresso.onView(ViewMatchers.withId(R.id.textInputEditCalendar))
            .perform(click())
            .perform(typeText("deadlineTestCase2"))
            .perform(pressKey(KeyEvent.KEYCODE_ENTER))
        Espresso.onView(ViewMatchers.withId(R.id.textInputEditCalendar))
            .check(ViewAssertions.matches(ViewMatchers.isClickable()))
    }

    @Test
    fun `text input screen should be released after adding deadline with done on SoftKeyboard`() {
        Espresso.onView(ViewMatchers.withId(R.id.textInputEditCalendar))
            .perform(click())
            .perform(typeText("deadlineTestCase3"))
            .perform(pressImeActionButton())
        Espresso.onView(ViewMatchers.withId(R.id.textInputEditCalendar))
            .check(ViewAssertions.matches(ViewMatchers.isClickable()))
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
                        LocalDateTime.of(2022, 3, 12, 0, 0)
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
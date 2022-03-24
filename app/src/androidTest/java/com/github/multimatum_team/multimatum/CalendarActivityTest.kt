package com.github.multimatum_team.multimatum

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.github.multimatum_team.multimatum.repository.DeadlineRepository
import com.github.multimatum_team.multimatum.util.MockDeadlineRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@UninstallModules(RepositoryModule::class)
@HiltAndroidTest
class CalendarActivityTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val activityRule = ActivityScenarioRule(CalendarActivity::class.java)

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun textInputFieldCanBeClicked() {
        Espresso.onView(ViewMatchers.withId(R.id.textInputEditCalendar))
            .check(ViewAssertions.matches(ViewMatchers.isClickable()))
    }

    @Test
    fun addingDeadlineToListviewDisplaysIt() {
        Espresso.onView(ViewMatchers.withId(R.id.textInputEditCalendar))
            .perform(ViewActions.typeText("Test"))
        Espresso.onView(ViewMatchers.withId(R.id.calendar_add_deadline_button))
            .perform(ViewActions.click())
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestDeadlineRepositoryModule {
        @Provides
        fun provideDeadlineRepository(): DeadlineRepository =
            MockDeadlineRepository(listOf())
    }
}
package com.github.multimatum_team.multimatum

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.github.multimatum_team.multimatum.repository.DeadlineID
import com.github.multimatum_team.multimatum.repository.DeadlineRepository
import com.github.multimatum_team.multimatum.util.MockDeadlineRepository
import com.github.multimatum_team.multimatum.viewmodel.DeadlineListViewModel
import com.github.multimatum_team.multimatum.util.getOrAwaitValue
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.isNotNull
import javax.inject.Singleton

@UninstallModules(RepositoryModule::class)
@HiltAndroidTest
class CalendarActivityTest {
    lateinit var deadlineList: DeadlineListViewModel

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

    /*
    @Test
    fun deadlineCanBeAddedUsingButton() {
        Espresso.onView(ViewMatchers.withId(R.id.textInputEditCalendar))
            .perform(click())
            .perform(typeText("deadlineTestCase"))
        Espresso.onView(ViewMatchers.withId(R.id.calendar_add_deadline_button))
            .perform(click())

    }
    */

    @Test
    fun keyboardDisappearWhenTouchedOutside() {
        Espresso.onView(ViewMatchers.withId(R.id.textInputEditCalendar))
            .perform(click())
        Espresso.onView(ViewMatchers.withId(R.id.calendar_view))
            .perform(click())
        Espresso.onView(ViewMatchers.withId(R.id.textInputEditCalendar))
            .check(ViewAssertions.matches(ViewMatchers.isClickable()))
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestDeadlineRepositoryModule {
        @Singleton
        @Provides
        fun provideDeadlineRepository(): DeadlineRepository =
            MockDeadlineRepository(listOf())
    }
}
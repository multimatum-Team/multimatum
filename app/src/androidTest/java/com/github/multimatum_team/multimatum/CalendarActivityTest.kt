package com.github.multimatum_team.multimatum

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test

class CalendarActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(CalendarActivity::class.java)

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
        Espresso.onData(allOf()).inAdapterView(ViewMatchers.withId(R.id.calendar_deadline_listview))
            .atPosition(0).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

}
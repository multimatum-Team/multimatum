package com.github.multimatum_team.multimatum

import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.Rule
import org.junit.Test

class CalendarActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(CalendarActivity::class.java)

    @Test
    fun shouldDisplayCalendarAtOpening() {
        Espresso.onView(ViewMatchers.withId(R.id.calendar_view))
            .check(ViewAssertions.matches(ViewMatchers.isEnabled()))
    }
}
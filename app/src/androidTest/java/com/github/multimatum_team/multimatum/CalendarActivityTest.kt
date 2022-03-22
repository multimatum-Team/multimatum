package com.github.multimatum_team.multimatum

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import org.junit.Test

class CalendarActivityTest {

    @Test
    fun shouldDisplayCalendarWhenLaunched(){
        ActivityScenario.launch(CalendarActivity::class.java)
        Espresso.onView(ViewMatchers.withId(R.id.calendar_view))
            .check(ViewAssertions.matches(ViewMatchers.isClickable()))
    }
}
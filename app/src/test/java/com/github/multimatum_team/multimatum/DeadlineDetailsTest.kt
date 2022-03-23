package com.github.multimatum_team.multimatum

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate


/**
 * Tests for the DeadlineDetailsActivity class
 */
@RunWith(AndroidJUnit4::class)
class DeadlineDetailsTest {

    @Test
    fun `Given a deadline not yet due or done, the activity should display it`() {
        val intent = DeadlineDetailsActivity.newIntent(
            ApplicationProvider.getApplicationContext(),
            Deadline("Test 1", DeadlineState.TODO, LocalDate.now().plusDays(7))
        )
        ActivityScenario.launch<DeadlineDetailsActivity>(intent)
        onView(withId(R.id.titleDeadlineDetails)).check(matches(withText("Test 1")))
        onView(withId(R.id.dateDeadlineDetails)).check(
            matches(
                withText(
                    "Due the ${
                        LocalDate.now().plusDays(7)
                    }"
                )
            )
        )
        onView(withId(R.id.detailsDeadlineDetails)).check(matches(withText("Due in 7 Days")))
    }

    @Test
    fun `Given a deadline already done, the activity should display it`() {
        val intent = DeadlineDetailsActivity.newIntent(
            ApplicationProvider.getApplicationContext(),
            Deadline("Test 2", DeadlineState.DONE, LocalDate.now().plusDays(7))
        )
        ActivityScenario.launch<DeadlineDetailsActivity>(intent)
        onView(withId(R.id.titleDeadlineDetails)).check(matches(withText("Test 2")))
        onView(withId(R.id.dateDeadlineDetails)).check(
            matches(
                withText(
                    "Due the ${
                        LocalDate.now().plusDays(7)
                    }"
                )
            )
        )
        onView(withId(R.id.detailsDeadlineDetails)).check(matches(withText("Done")))
    }

    @Test
    fun `Given a deadline already due, the activity should display it`() {
        val intent = DeadlineDetailsActivity.newIntent(
            ApplicationProvider.getApplicationContext(),
            Deadline("Test 3", DeadlineState.TODO, LocalDate.now().minusDays(2))
        )
        ActivityScenario.launch<DeadlineDetailsActivity>(intent)
        onView(withId(R.id.titleDeadlineDetails)).check(matches(withText("Test 3")))
        onView(withId(R.id.dateDeadlineDetails)).check(
            matches(
                withText(
                    "Due the ${
                        LocalDate.now().minusDays(2)
                    }"
                )
            )
        )
        onView(withId(R.id.detailsDeadlineDetails)).check(matches(withText("Is already Due")))
    }
}
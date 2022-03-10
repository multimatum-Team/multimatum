package com.github.multimatum_team.multimatum

import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import org.junit.Test
import org.junit.Assert.*
import java.lang.IllegalArgumentException
import java.time.LocalDate
import java.time.Month
import java.time.Period

/**
 * Unit tests for the Deadline class.
 */
class DeadlineTests {
    @Test
    fun `Constructor should return Deadline instance defining the right properties`() {
        val title = "Implement tests for Deadline class"
        val state = DeadlineState.DONE
        val startDate = LocalDate.of(2022, Month.MARCH, 9)
        val endDate = LocalDate.of(2022, Month.MARCH, 11)
        val deadline = Deadline(title, state, startDate, endDate)
        assertEquals(deadline.title, title)
        assertEquals(deadline.state, state)
        assertEquals(deadline.startDate, startDate)
        assertEquals(deadline.endDate, endDate)
    }

    @Test
    fun `Constructor should throw IllegalArgumentException when title is empty`() {
        assertThrows(IllegalArgumentException::class.java) {
            Deadline(
                "",
                DeadlineState.TODO,
                LocalDate.of(2022, Month.MARCH, 11),
                LocalDate.of(2022, Month.MARCH, 9),
            )
        }
    }

    @Test
    fun `Constructor should throw IllegalArgumentException when start date is after end date`() {
        assertThrows(IllegalArgumentException::class.java) {
            Deadline(
                "This task is ill formed",
                DeadlineState.TODO,
                LocalDate.of(2022, Month.MARCH, 11),
                LocalDate.of(2022, Month.MARCH, 9),
            )
        }
    }

    @Test
    fun `duration returns difference between endDate and startDate`() {
        val deadline = Deadline(
            "Implement tests for Deadline class",
            DeadlineState.DONE,
            LocalDate.of(2022, Month.MARCH, 9),
            LocalDate.of(2022, Month.MARCH, 11)
        )
        assertEquals(deadline.duration, Period.ofDays(2))
    }

    @Test
    fun `timeRemaining returns difference between now and endDate`() {
        val now = LocalDate.now()
        val days = Period.ofDays(3)
        val deadline = Deadline(
            "Implement tests for Deadline class",
            DeadlineState.DONE,
            now.minusDays(1),
            now.plus(days)
        )
        assertEquals(deadline.timeRemaining, days)
    }

    @Test
    fun `timeRemaining returns null if endDate has passed`() {
        val now = LocalDate.now()
        val deadline = Deadline(
            "Implement tests for Deadline class",
            DeadlineState.DONE,
            now.minusDays(3),
            now.minusDays(1)
        )
        assertNull(deadline.timeRemaining)
    }

    @Test
    fun `isDue returns true when deadline has passed`() {
        val now = LocalDate.now()
        val deadline = Deadline(
            "Implement tests for Deadline class",
            DeadlineState.DONE,
            now.minusDays(3),
            now.minusDays(1)
        )
        assertTrue(deadline.isDue)
    }

    @Test
    fun `isDue returns false if endDate is in the future`() {
        val now = LocalDate.now()
        val days = Period.ofDays(3)
        val deadline = Deadline(
            "Implement tests for Deadline class",
            DeadlineState.DONE,
            now.minusDays(1),
            now.plus(days)
        )
        assertFalse(deadline.isDue)
    }
}
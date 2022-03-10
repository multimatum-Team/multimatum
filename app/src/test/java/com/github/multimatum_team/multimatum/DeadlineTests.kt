package com.github.multimatum_team.multimatum

import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.*

/**
 * Unit tests for the Deadline class.
 */
class DeadlineTests {
    /**
     * This snippet was inspired from
     * https://nieldw.medium.com/unit-testing-fixing-the-system-clock-with-mockk-fc6991afb766
     */

    private val now = 1646920323881L
    private val fixedClock = Clock.fixed(Instant.ofEpochMilli(now), ZoneId.of("UTC"))

    @Before
    fun `Fix the clock to ensure test reproducibility`() {
        mockkStatic(Clock::class)
        every { Clock.systemUTC() } returns fixedClock
    }

    @Test
    fun `Constructor should return Deadline instance defining the right properties`() {
        val title = "Implement tests for Deadline class"
        val state = DeadlineState.DONE
        val date = LocalDate.of(2022, Month.MARCH, 11)
        val deadline = Deadline(title, state, date)
        assertEquals(deadline.title, title)
        assertEquals(deadline.state, state)
        assertEquals(deadline.date, date)
    }

    @Test
    fun `Constructor should throw IllegalArgumentException when title is empty`() {
        assertThrows(IllegalArgumentException::class.java) {
            Deadline(
                "",
                DeadlineState.TODO,
                LocalDate.of(2022, Month.MARCH, 9)
            )
        }
    }

    @Test
    fun `timeRemaining returns difference between now and endDate`() {
        val now = LocalDate.now()
        val days = Period.ofDays(3)
        val deadline = Deadline(
            "Implement tests for Deadline class",
            DeadlineState.DONE,
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
            now.minusDays(1)
        )
        assertNull(deadline.timeRemaining)
    }

    @Test
    fun `isDue returns true when deadline has passed`() {
        val deadline = Deadline(
            "Implement tests for Deadline class",
            DeadlineState.DONE,
            LocalDate.now().minusDays(1)
        )
        assertTrue(deadline.isDue)
    }

    @Test
    fun `isDue returns false if endDate is in the future`() {
        val days = Period.ofDays(3)
        val deadline = Deadline(
            "Implement tests for Deadline class",
            DeadlineState.DONE,
            LocalDate.now().plus(days)
        )
        assertFalse(deadline.isDue)
    }
}
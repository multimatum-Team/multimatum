package com.github.multimatum_team.multimatum

import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.LocalDate
import java.time.Month

/**
 * Unit tests for the Deadline class.
 */
class DeadlineTests {
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
}
package com.github.multimatum_team.multimatum.modelTest

import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.LocalDateTime
import java.time.Month

/**
 * Unit tests for the Deadline class.
 */
class DeadlineTest {
    @Test
    fun `Constructor should return Deadline instance defining the right properties`() {
        val title = "Implement tests for Deadline class"
        val state = DeadlineState.DONE
        val date = LocalDateTime.of(2022, Month.MARCH, 11, 0, 0)
        val description = "Some description"
        val deadline = Deadline(
            title,
            state,
            date,
            description = description
        )
        assertEquals(deadline.description, description)
        assertEquals(deadline.title, title)
        assertEquals(deadline.state, state)
        assertEquals(deadline.dateTime, date)
    }

    @Test
    fun `Constructor should throw IllegalArgumentException when title is empty`() {
        assertThrows(IllegalArgumentException::class.java) {
            Deadline(
                "",
                DeadlineState.TODO,
                LocalDateTime.of(2022, Month.MARCH, 9, 0, 0)
            )
        }
    }
}
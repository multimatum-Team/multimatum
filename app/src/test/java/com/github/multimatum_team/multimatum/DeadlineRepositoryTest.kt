package com.github.multimatum_team.multimatum

import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.github.multimatum_team.multimatum.repository.DeadlineRepository
import com.github.multimatum_team.multimatum.util.MockDeadlineRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class DeadlineRepositoryTest {
    private val repository: DeadlineRepository = MockDeadlineRepository(
        listOf(
            Deadline("Deadline 1", DeadlineState.DONE, LocalDateTime.of(2022, 3, 17, 0, 0)),
            Deadline("Deadline 2", DeadlineState.DONE, LocalDateTime.of(2022, 3, 20, 0, 0)),
            Deadline("Deadline 3", DeadlineState.TODO, LocalDateTime.of(2022, 4, 15, 0, 0)),
        )
    )

    @Test
    fun `Default implementation of fetch returns the right deadline`() = runTest {
        assertEquals(
            repository.fetch("0"),
            Deadline("Deadline 1", DeadlineState.DONE, LocalDateTime.of(2022, 3, 17, 0, 0))
        )
        assertEquals(
            repository.fetch("1"),
            Deadline("Deadline 2", DeadlineState.DONE, LocalDateTime.of(2022, 3, 20, 0, 0))
        )
        assertEquals(
            repository.fetch("2"),
            Deadline("Deadline 3", DeadlineState.TODO, LocalDateTime.of(2022, 4, 15, 0, 0))
        )
    }

    @Test
    fun `fetchAfter returns deadlines due after the given date`() {
        val date = LocalDateTime.of(2022, 3, 18, 0, 0)
        val deadlines = runBlocking { repository.fetchAfter(date) }
        assertEquals(
            deadlines, mapOf(
                "1" to Deadline(
                    "Deadline 2",
                    DeadlineState.DONE,
                    LocalDateTime.of(2022, 3, 20, 0, 0)
                ),
                "2" to Deadline(
                    "Deadline 3",
                    DeadlineState.TODO,
                    LocalDateTime.of(2022, 4, 15, 0, 0)
                ),
            )
        )
    }
}
package com.github.multimatum_team.multimatum.repository

import com.github.multimatum_team.multimatum.model.Deadline
import java.time.LocalDate

/**
 * An interface for the deadline database.
 * A minimal implementation of this interface requires defining `fetchAll` and `put`.
 * The other methods have a default implementation which may be overridden for performance purposes.
 */
interface DeadlineRepository {
    /**
     * Fetch all user-defined deadlines from the repository.
     */
    suspend fun fetchAll(): List<Deadline>

    /**
     * Fetch all deadlines occurring after a given date from the repository.
     */
    suspend fun fetchAfter(dateLimit: LocalDate): List<Deadline> =
        fetchAll().filter { it.date > dateLimit }

    /**
     * Add new deadline to the repository.
     */
    suspend fun put(deadline: Deadline)
}
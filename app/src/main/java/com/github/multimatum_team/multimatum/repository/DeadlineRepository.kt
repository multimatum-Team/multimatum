package com.github.multimatum_team.multimatum.repository

import com.github.multimatum_team.multimatum.model.Deadline
import java.time.LocalDate

typealias DeadlineID = String

/**
 * An interface for the deadline database.
 * A minimal implementation of this interface requires defining `fetchAll`, `put`, `modify` and
 * `delete`.
 * The other methods have a default implementation which may be overridden for performance purposes.
 */
interface DeadlineRepository {
    /**
     * Fetch a single deadline from its ID.
     */
    suspend fun fetch(id: String): Deadline? =
        fetchAll()[id]

    /**
     * Fetch all user-defined deadlines from the repository.
     */
    suspend fun fetchAll(): Map<DeadlineID, Deadline>

    /**
     * Fetch all deadlines occurring after a given date from the repository.
     */
    suspend fun fetchAfter(dateLimit: LocalDate): Map<DeadlineID, Deadline> =
        fetchAll().filterValues { it.date > dateLimit }

    /**
     * Add a new deadline to the repository, returning the freshly generated ID of the new deadline.
     */
    suspend fun put(deadline: Deadline): DeadlineID

    /**
     * Modify an existing deadline to a new value.
     */
    suspend fun modify(id: DeadlineID, newDeadline: Deadline)

    /**
     * Remove a deadline from the repository.
     */
    suspend fun delete(id: DeadlineID)

    /**
     * Add listener for database updates.
     */
    fun onUpdate(callback: (Map<DeadlineID, Deadline>) -> Unit)
}
package com.github.multimatum_team.multimatum.repository

import com.github.multimatum_team.multimatum.model.Deadline
import com.google.android.gms.tasks.Task
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
    fun fetchAll(): Task<List<Deadline>>

    /**
     * Fetch all deadlines occurring after a given date from the repository.
     */
    fun fetchAfter(dateLimit: LocalDate): Task<List<Deadline>> =
        fetchAll().continueWith { task ->
            if (task.isSuccessful) {
                task.result.filter { it.date > dateLimit }
            } else {
                listOf()
            }
        }

    /**
     * Add new deadline to the repository.
     */
    fun put(deadline: Deadline): Task<Unit>
}
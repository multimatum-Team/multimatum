package com.github.multimatum_team.multimatum.repository

import com.github.multimatum_team.multimatum.model.*
import java.time.LocalDateTime

typealias DeadlineID = String

/**
 * An interface for the deadline database.
 * A minimal implementation of this interface requires defining `fetchAll`, `put`, `modify` and
 * `delete`.
 * The other methods have a default implementation which may be overridden for performance purposes.
 */
abstract class DeadlineRepository {
    protected lateinit var _user: User
    protected var _groups: Map<GroupID, UserGroup> = mapOf()

    /**
     * Set the user associated to the deadlines.
     */
    open fun setUser(newUser: User) {
        _user = newUser
    }

    /**
     * Set the user associated to the deadlines.
     */
    open fun setGroups(newGroups: Map<GroupID, UserGroup>) {
        _groups = newGroups
    }

    /**
     * Fetch a single deadline from its ID.
     */
    open suspend fun fetch(id: String): Deadline? =
        fetchAll()[id]

    /**
     * Fetch all deadlines owned by a given owner.
     */
    open suspend fun fetchFromOwner(owner: DeadlineOwner): Map<DeadlineID, Deadline> =
        fetchAll().filterValues { it.owner == owner }

    /**
     * Fetch all user-defined deadlines from the repository.
     */
    abstract suspend fun fetchAll(): Map<DeadlineID, Deadline>

    /**
     * Fetch all deadlines occurring after a given date from the repository.
     */
    suspend fun fetchAfter(dateLimit: LocalDateTime): Map<DeadlineID, Deadline> =
        fetchAll().filterValues { it.dateTime > dateLimit }

    /**
     * Add a new deadline to the repository, returning the freshly generated ID of the new deadline.
     */
    abstract suspend fun put(deadline: Deadline): DeadlineID

    /**
     * Modify an existing deadline to a new value.
     */
    abstract suspend fun modify(id: DeadlineID, newDeadline: Deadline)

    /**
     * Remove a deadline from the repository.
     */
    abstract suspend fun delete(id: DeadlineID)

    /**
     * Add listener for database updates.
     */
    abstract fun onUpdate(callback: (Map<DeadlineID, Deadline>) -> Unit)
}
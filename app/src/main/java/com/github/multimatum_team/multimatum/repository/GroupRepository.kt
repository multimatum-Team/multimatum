package com.github.multimatum_team.multimatum.repository

import com.github.multimatum_team.multimatum.model.*

/**
 * An interface for the user group database.
 * A minimal implementation of this interface requires defining `fetchAll`, `create`, `delete` and
 * `onUpdate`.
 * The other methods have a default implementation which may be overridden for performance purposes.
 */
abstract class GroupRepository {
    protected lateinit var _user: User

    /**
     * Set the user that is querying the group repository.
     * The result of the queries will change depending on the user that is making de request,
     * since the repository will only return group of which the user is a member.
     */
    fun setUser(newUser: User) {
        _user = newUser
    }

    /**
     * Fetch a single group from its ID.
     * @param id the ID of the group to fetch
     */
    open suspend fun fetch(id: GroupID): UserGroup? =
        fetchAll()[id]

    /**
     * Fetch all groups of which the user is a member.
     */
    abstract suspend fun fetchAll(): Map<GroupID, UserGroup>

    /**
     * Fetch all groups of which the user is the owner.
     */
    open suspend fun fetchOwned(): Map<GroupID, UserGroup> =
        fetchAll().filterValues { it.owner == _user.id }

    /**
     * Create a new empty group with a given name.
     * @param name the name of the new group
     */
    abstract suspend fun create(name: String): GroupID

    /**
     * Delete a given group.
     * @param id the ID of the group to delete.
     */
    abstract suspend fun delete(id: DeadlineID)

    /**
     * Add listener for database updates.
     * @param callback the callback to run when the groups of the current user changes.
     */
    abstract fun onUpdate(callback: (Map<GroupID, UserGroup>) -> Unit)
}
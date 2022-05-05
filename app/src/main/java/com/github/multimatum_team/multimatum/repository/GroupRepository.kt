package com.github.multimatum_team.multimatum.repository

import com.github.multimatum_team.multimatum.model.GroupID
import com.github.multimatum_team.multimatum.model.UserGroup
import com.github.multimatum_team.multimatum.model.UserID

/**
 * An interface for the user group database.
 * A minimal implementation of this interface requires defining `fetchAll`, `create`, `delete` and
 * `onUpdate`.
 * The other methods have a default implementation which may be overridden for performance purposes.
 */
abstract class GroupRepository {
    protected lateinit var _userID: UserID

    /**
     * Set the user that is querying the group repository.
     * The result of the queries will change depending on the user that is making de request,
     * since the repository will only return group of which the user is a member.
     */
    fun setUserID(newUserID: UserID) {
        _userID = newUserID
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
        fetchAll().filterValues { it.owner == _userID }

    /**
     * Create a new empty group with a given name.
     * @param name the name of the new group
     */
    abstract suspend fun create(name: String): GroupID

    /**
     * Delete a given group.
     * @param id the ID of the group to delete
     */
    abstract suspend fun delete(id: GroupID)

    /**
     * Rename a group.
     * @param id the ID of the group to rename
     * @param newName the new name of the group
     */
    abstract suspend fun rename(id: GroupID, newName: String)

    /**
     * Invite an user to a group.
     * @param id the ID of the group to which we want to invite the new user
     * @param email the email of the user to invite
     */
    abstract suspend fun invite(id: GroupID, email: String)

    /**
     * Add listener for database updates.
     * @param callback the callback to run when the groups of the current user changes.
     */
    abstract fun onUpdate(callback: (Map<GroupID, UserGroup>) -> Unit)
}
package com.github.multimatum_team.multimatum.repository

import android.net.Uri
import com.github.multimatum_team.multimatum.model.GroupID
import com.github.multimatum_team.multimatum.model.SignedInUser
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
    abstract suspend fun fetch(id: GroupID): UserGroup?

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
     * Generate an invite link to join a group given by its ID
     * @param id the ID of the group for which to generate the invite link
     */
    abstract fun generateInviteLink(id: GroupID, linkTitle: String, linkDescription: String): Uri

    /**
     * Add a user from a group.
     * @param groupID the group to which to add the user
     * @param memberID the ID of the new group member
     */
    abstract suspend fun addMember(groupID: GroupID, memberID: UserID)

    /**
     * Kick a user from a group.
     * @param groupID the group from which to kick the user
     * @param memberID the ID of the group member to kick
     */
    abstract suspend fun removeMember(groupID: GroupID, memberID: UserID)

    /**
     * Add listener for database updates.
     * @param callback the callback to run when the groups of the current user changes.
     */
    abstract fun onUpdate(callback: (Map<GroupID, UserGroup>) -> Unit)
}
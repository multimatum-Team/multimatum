package com.github.multimatum_team.multimatum.util

import com.github.multimatum_team.multimatum.model.AnonymousUser
import com.github.multimatum_team.multimatum.model.GroupID
import com.github.multimatum_team.multimatum.model.UserGroup
import com.github.multimatum_team.multimatum.model.UserID
import com.github.multimatum_team.multimatum.repository.GroupRepository

/**
 * Defines a dummy group repository that works locally on a plain list.
 * This way the tests are completely independent from Firebase or network availability.
 *
 * @param initialContents the initial list of groups contained in the group repository;
 *                        to ensure that created groups are given an unique ID, the IDs of the
 *                        groups in the list are re-assigned
 */
class MockGroupRepository(initialContents: List<UserGroup>) : GroupRepository() {
    private var counter: Int = 0

    private val groupsPerUser: MutableMap<UserID, MutableMap<GroupID, UserGroup>> =
        mutableMapOf()

    private val groups: MutableMap<GroupID, UserGroup>
        get() = groupsPerUser.getOrPut(_userID) { mutableMapOf() }

    private val updateListeners: MutableMap<UserID, MutableList<(Map<GroupID, UserGroup>) -> Unit>> =
        mutableMapOf()

    init {
        for (group in initialContents) {
            val newID = counter.toString()
            counter++
            for (memberID in group.members) {
                groupsPerUser
                    .getOrPut(memberID) { mutableMapOf() }
                    .put(newID, group.copy(id = newID))
            }
        }
        setUserID("0")
    }

    private fun notifyUpdateListeners() =
        updateListeners[_userID]?.forEach { it(groups) }

    override suspend fun fetchAll(): Map<GroupID, UserGroup> = groups

    override suspend fun create(name: String): GroupID {
        val id = counter.toString()
        counter++
        groups[id] = UserGroup(id, name, _userID)
        notifyUpdateListeners()
        return id
    }

    override suspend fun delete(id: GroupID) {
        groups.remove(id)
        notifyUpdateListeners()
    }

    override suspend fun rename(id: GroupID, newName: String) {
        require(groups.containsKey(id))
        groups[id] = groups[id]!!.copy(name = newName)
        notifyUpdateListeners()
    }

    override suspend fun invite(id: GroupID, email: String) {
        throw UnsupportedOperationException("group invites are not supported")
    }

    override suspend fun removeMember(groupID: GroupID, memberID: UserID) {
        var group = groups[groupID]!!
        val newMembers = group.members.toMutableSet()
        newMembers.remove(memberID)
        println("$_userID, $group, $newMembers")
        group = group.copy(members = newMembers)
        groupsPerUser[memberID]?.remove(groupID)
        for (groupMap in groupsPerUser.values) {
            if (groupMap.containsKey(groupID)) {
                groupMap[groupID] = group
            }
        }
        println("removed $memberID from $groupID: $newMembers")
        notifyUpdateListeners()
    }

    override fun onUpdate(callback: (Map<GroupID, UserGroup>) -> Unit) {
        updateListeners
            .getOrPut(_userID) { mutableListOf() }
            .add(callback)
    }
}
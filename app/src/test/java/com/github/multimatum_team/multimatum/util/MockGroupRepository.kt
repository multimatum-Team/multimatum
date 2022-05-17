package com.github.multimatum_team.multimatum.util

import android.net.Uri
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

    private val groups: MutableMap<GroupID, UserGroup> = mutableMapOf()

    private val updateListeners: MutableMap<UserID, MutableList<(Map<GroupID, UserGroup>) -> Unit>> =
        mutableMapOf()

    init {
        for (group in initialContents) {
            val newID = counter.toString()
            counter++
            groups[newID] = group.copy(id = newID)
        }
        setUserID("0")
    }

    private fun notifyUpdateListeners() =
        updateListeners[_userID]?.forEach { it(groups) }

    override suspend fun fetch(id: GroupID): UserGroup =
        groups[id]!!

    override suspend fun fetchAll(): Map<GroupID, UserGroup> =
        groups.filterValues { group -> group.members.contains(_userID) }

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

    override suspend fun generateInviteLink(id: GroupID): Uri {
        val group = fetch(id)!!
        val inviteLink = Uri.Builder()
            .scheme("https")
            .authority("multimatum.page.link")
            .appendQueryParameter("id", group.id)
            .build()
        return Uri.Builder()
            .scheme("https")
            .authority("multimatum.page.link")
            .appendQueryParameter("sd", "Click this link to accept the invite")
            .appendQueryParameter("st", "Join group ${group.name}")
            .appendQueryParameter("apn", "com.github.multimatum_team.multimatum")
            .appendQueryParameter("link", inviteLink.toString())
            .build()
    }

    override suspend fun addMember(groupID: GroupID, memberID: UserID) {
        val group = groups[groupID]!!
        val newMembers = group.members.toMutableList()
        newMembers.add(memberID)
        groups[groupID] = group.copy(members = newMembers.toSet())
        notifyUpdateListeners()
    }

    override suspend fun removeMember(groupID: GroupID, memberID: UserID) {
        val group = groups[groupID]!!
        val newMembers = group.members.toMutableList()
        newMembers.remove(memberID)
        groups[groupID] = group.copy(members = newMembers.toSet())
        notifyUpdateListeners()
    }

    override fun onUpdate(callback: (Map<GroupID, UserGroup>) -> Unit) {
        updateListeners
            .getOrPut(_userID) { mutableListOf() }
            .add(callback)
    }
}
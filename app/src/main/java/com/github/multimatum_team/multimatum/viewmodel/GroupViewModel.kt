package com.github.multimatum_team.multimatum.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.multimatum_team.multimatum.LogUtil
import com.github.multimatum_team.multimatum.R
import com.github.multimatum_team.multimatum.model.GroupID
import com.github.multimatum_team.multimatum.model.UserGroup
import com.github.multimatum_team.multimatum.model.UserID
import com.github.multimatum_team.multimatum.repository.AuthRepository
import com.github.multimatum_team.multimatum.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the group view.
 * Acts as a bridge between the group repository (Firebase) and the user interface.
 * The ViewModel ensures that the data is always fresh, keeping the database and the UI in sync.
 * The list of groups is also updated when the GroupRepository notifies that the authenticated
 * user has changed.
 */
@HiltViewModel
@SuppressLint("StaticFieldLeak")
class GroupViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val groupRepository: GroupRepository,
) : ViewModel() {
    private val _groups: MutableLiveData<Map<GroupID, UserGroup>> = MutableLiveData()

    init {
        // Initialize the group repository with the currently logged in user, then fetch the data
        // to initialize the group list
        groupRepository.setUserID(authRepository.getUser().id)
        viewModelScope.launch {
            val groups = groupRepository.fetchAll()
            _groups.value = groups
            LogUtil.debugLog("fetching for the first time: $groups")
        }

        // Listen for authentication updates, upon which the group list is re-fetched
        authRepository.onUpdate { newUser ->
            LogUtil.debugLog("update auth: $newUser")
            groupRepository.setUserID(newUser.id)
            refreshGroups()
        }

        // Listen for changes in the group list as well, in order to synchronize between the
        // Firebase database contents
        groupRepository.onUpdate { newGroups ->
            LogUtil.debugLog("update groups: $newGroups")
            _groups.value = newGroups
        }
    }

    /**
     * Re-fetch all groups from the repository and assign the value to the LiveData.
     */
    private fun refreshGroups(callback: (Map<GroupID, UserGroup>) -> Unit = {}) =
        viewModelScope.launch {
            val groups = groupRepository.fetchAll()
            LogUtil.debugLog("refreshing groups: ${_groups.value} -> $groups")
            _groups.value = groups
            callback(groups)
        }

    /**
     * Get all groups.
     */
    fun getGroups(): LiveData<Map<GroupID, UserGroup>> =
        _groups

    /**
     * Get all owned groups.
     */
    fun getOwnedGroups(): Map<GroupID, UserGroup> =
        _groups.value!!.filterValues { group -> (group.owner == authRepository.getUser().id) }

    /**
     * Get a single group from its ID.
     */
    fun getGroup(id: GroupID): UserGroup =
        _groups.value!![id]!!

    /**
     * Ask the repository to fetch a group which the current user is possibly not a member of.
     */
    fun fetchNewGroup(id: GroupID, callback: (UserGroup?) -> Unit) =
        viewModelScope.launch {
            callback(groupRepository.fetch(id))
        }

    /**
     * Add a new group to the repository.
     * @param name the name of the new group to create
     * @param callback what to do when the group creation is complete
     */
    fun createGroup(name: String, callback: (GroupID) -> Unit = {}) =
        viewModelScope.launch {
            val id = groupRepository.create(name)
            LogUtil.debugLog("creating group named $name with id $id")
            callback(id)
        }

    /**
     * Remove a group from the repository.
     * @param id the id of the group to remove
     * @param callback what to do when the deletion is finished
     */
    fun deleteGroup(id: GroupID, callback: (GroupID) -> Unit = {}) =
        viewModelScope.launch {
            groupRepository.delete(id)
            LogUtil.debugLog("deleting group with id $id")
            callback(id)
        }

    /**
     * Rename a group with a given ID.
     * @pram id the ID of the group to rename
     * @param newName the new name of the group
     */
    fun renameGroup(id: GroupID, newName: String) = viewModelScope.launch {
        groupRepository.rename(id, newName)
        LogUtil.debugLog("renaming group with id $id to $newName")
    }

    fun addMember(groupID: GroupID, memberID: UserID) = viewModelScope.launch {
        groupRepository.addMember(groupID, memberID)
    }

    fun removeMember(groupID: GroupID, memberID: UserID) = viewModelScope.launch {
        groupRepository.removeMember(groupID, memberID)
    }

    /**
     * Generate invite link to join a group given from its ID.
     * @param id the ID of the group to which we want to invite users
     */
    fun generateInviteLink(id: GroupID, callback: (Uri?) -> Unit) =
        viewModelScope.launch {
            when (val group = groupRepository.fetch(id)) {
                null -> callback(null)
                else -> {
                    val linkTitle = context.getString(R.string.group_invite_link_title, group.name)
                    val linkDescription = context.getString(R.string.group_invite_link_description)
                    callback(groupRepository.generateInviteLink(id, linkTitle, linkDescription))
                }
            }
        }
}
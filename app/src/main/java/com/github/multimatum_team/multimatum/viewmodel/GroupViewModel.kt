package com.github.multimatum_team.multimatum.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.multimatum_team.multimatum.model.GroupID
import com.github.multimatum_team.multimatum.model.UserGroup
import com.github.multimatum_team.multimatum.repository.AuthRepository
import com.github.multimatum_team.multimatum.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
class GroupViewModel @Inject constructor(
    application: Application,
    authRepository: AuthRepository,
    private val groupRepository: GroupRepository
) : AndroidViewModel(application) {
    private val _groups: MutableLiveData<Map<GroupID, UserGroup>> = MutableLiveData()

    init {
        // Initialize the group repository with the currently logged in user, then fetch the data
        // to initialize the group list
        groupRepository.setUserID(authRepository.getUser().id)
        viewModelScope.launch {
            val groups = groupRepository.fetchAll()
            _groups.value = groups
            Log.d("GroupViewModel", "fetching for the first time: $groups")
        }

        // Listen for authentication updates, upon which the group list is re-fetched
        authRepository.onUpdate { newUser ->
            Log.d("GroupViewModel", "update auth: $newUser")
            groupRepository.setUserID(newUser.id)
            refreshGroups()
        }

        // Listen for changes in the group list as well, in order to synchronize between the
        // Firebase database contents
        groupRepository.onUpdate { newGroups ->
            Log.d("GroupViewModel", "update groups: $newGroups")
            _groups.value = newGroups
        }
    }

    /**
     * Re-fetch all groups from the repository and assign the value to the LiveData.
     */
    private fun refreshGroups(callback: (Map<GroupID, UserGroup>) -> Unit = {}) =
        viewModelScope.launch {
            val groups = groupRepository.fetchAll()
            Log.d("GroupViewModel", "refreshing groups: ${_groups.value} -> $groups")
            _groups.value = groups
            callback(groups)
        }

    /**
     * Get all groups.
     */
    fun getGroups(): LiveData<Map<GroupID, UserGroup>> =
        _groups

    /**
     * Get a single group from its ID.
     */
    fun getGroup(id: GroupID): UserGroup =
        _groups.value!![id]!!

    /**
     * Add a new group to the repository.
     * @param name the name of the new group to create
     * @param callback what to do when the group creation is complete
     */
    fun createGroup(name: String, callback: (GroupID) -> Unit = {}) =
        viewModelScope.launch {
            val id = groupRepository.create(name)
            Log.d("GroupViewModel", "creating group named $name with id $id")
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
            Log.d("GroupViewModel", "deleting group with id $id")
            callback(id)
        }

    /**
     * Rename a group with a given ID.
     * @pram id the ID of the group to rename
     * @param newName the new name of the group
     */
    fun renameGroup(id: GroupID, newName: String) =
        viewModelScope.launch {
            groupRepository.rename(id, newName)
            Log.d("GroupViewModel", "renaming group with id $id to $newName")
        }

    /**
     * Invite an user to join a group given from its ID.
     * @param id the ID of the group to which we want to invite the user
     * @param email the email of the user to invite
     */
    fun inviteUser(id: GroupID, email: String) =
        viewModelScope.launch {
            groupRepository.invite(id, email)
            Log.d("GroupViewModel", "inviting user with email $email to group with id $id")
        }
}
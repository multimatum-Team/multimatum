package com.github.multimatum_team.multimatum.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.multimatum_team.multimatum.LogUtil
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.repository.AuthRepository
import com.github.multimatum_team.multimatum.repository.DeadlineID
import com.github.multimatum_team.multimatum.repository.DeadlineRepository
import com.github.multimatum_team.multimatum.repository.GroupRepository
import com.github.multimatum_team.multimatum.util.DeadlineNotification
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * ViewModel for the deadline list view.
 * Acts as a bridge between the deadline repository (Firebase) and the user interface.
 * The ViewModel ensures that the data is always fresh, keeping the database and the UI in sync.
 * The list of deadlines is also updated when the AuthRepository notifies that the authenticated
 * user has changed.
 */
@HiltViewModel
class DeadlineListViewModel @Inject constructor(
    application: Application,
    authRepository: AuthRepository,
    groupRepository: GroupRepository,
    private val deadlineRepository: DeadlineRepository
) : AndroidViewModel(application) {
    private val _deadlines: MutableLiveData<Map<DeadlineID, Deadline>> = MutableLiveData()

    init {
        val context = getApplication<Application>().applicationContext

        // Initialize the deadline repository with the currently logged in user, then fetch the data
        // to initialize the deadline list
        authRepository.getUser().let { user ->
            groupRepository.setUserID(user.id)
            deadlineRepository.setUser(user)
            deadlineRepository.setGroups(runBlocking { groupRepository.fetchAll() })
            runBlocking { deadlineRepository.fetchAll() }.let { deadlines ->
                LogUtil.debugLog("fetching initial deadlines: $deadlines")
                _deadlines.value = deadlines
                DeadlineNotification.updateNotifications(deadlines, context)
            }
        }

        // Listen for authentication updates, upon which the deadline list is re-fetched
        authRepository.onUpdate { newUser ->
            LogUtil.debugLog("update auth: $newUser")
            deadlineRepository.setUser(newUser)
            refreshDeadlines { deadlinesMap ->
                DeadlineNotification.updateNotifications(deadlinesMap, context)
            }
        }

        // Listen for group updates, upon which the deadline list is re-fetched
        groupRepository.onUpdate { newGroups ->
            LogUtil.debugLog("update groups: $newGroups")
            deadlineRepository.setGroups(newGroups)
            refreshDeadlines { deadlinesMap ->
                DeadlineNotification.updateNotifications(deadlinesMap, context)
            }
        }

        // Listen for changes in the deadline list as well, in order to synchronize between the
        // Firebase database contents
        deadlineRepository.onUpdate { newDeadlines ->
            LogUtil.debugLog("update deadlines: $newDeadlines")
            _deadlines.value = newDeadlines
        }
    }

    /**
     * Re-fetch all deadlines from the repository and assign the value to the LiveData.
     */
    private fun refreshDeadlines(callback: (Map<DeadlineID, Deadline>) -> Unit = {}) =
        viewModelScope.launch {
            val newDeadlines = deadlineRepository.fetchAll()
            _deadlines.value = newDeadlines
            LogUtil.debugLog("update deadlines: $newDeadlines")
            callback(_deadlines.value!!)
        }

    /**
     * Get all deadlines.
     */
    fun getDeadlines(): LiveData<Map<DeadlineID, Deadline>> =
        _deadlines

    fun getDeadline(id: DeadlineID): Deadline =
        _deadlines.value!![id]!!

    /**
     * Add a new deadline to the repository.
     */
    fun addDeadline(deadline: Deadline, callback: (DeadlineID) -> Unit = {}) =
        viewModelScope.launch {
            val id = deadlineRepository.put(deadline)
            LogUtil.debugLog("add deadline: $deadline with id $id")
            callback(id)
        }

    /**
     * Remove a deadline from the repository.
     */
    fun deleteDeadline(id: DeadlineID, callback: (DeadlineID) -> Unit = {}) =
        viewModelScope.launch {
            deadlineRepository.delete(id)
            LogUtil.debugLog("deleting deadline with id $id")
            callback(id)
        }

    /**
     * Modify a deadline from the repository.
     */
    fun modifyDeadline(id: DeadlineID, newDeadline: Deadline) = viewModelScope.launch {
        LogUtil.debugLog("modifying deadline with id $id to $newDeadline")
        deadlineRepository.modify(id, newDeadline)
    }
}
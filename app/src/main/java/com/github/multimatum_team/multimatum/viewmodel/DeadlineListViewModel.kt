package com.github.multimatum_team.multimatum.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.repository.AuthRepository
import com.github.multimatum_team.multimatum.repository.DeadlineID
import com.github.multimatum_team.multimatum.repository.DeadlineRepository
import com.github.multimatum_team.multimatum.util.DeadlineNotification
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.security.auth.callback.Callback

/**
 * ViewModel for the deadline list view.
 * Acts as a bridge between the deadline repository (Firebase) and the user interface.
 * The ViewModel ensures that the data is always fresh, keeping the database and the UI in sync.
 * The list of deadlines is also updated when the AuthRepository notifies that the authenticated
 * user has changed.
 */
@HiltViewModel
class DeadlineListViewModel @Inject constructor(
    authRepository: AuthRepository,
    application: Application,
    private val deadlineRepository: DeadlineRepository
) : AndroidViewModel(application) {
    private val _deadlines: MutableLiveData<Map<DeadlineID, Deadline>> = MutableLiveData()
    private var deadlineNotification: DeadlineNotification

    init {
        // Initialize the deadline repository with the currently logged in user, then fetch the data
        // to initialize the deadline list
        deadlineRepository.setUser(runBlocking { authRepository.getCurrentUser() })
        viewModelScope.launch {
            _deadlines.value = deadlineRepository.fetchAll()
        }

        deadlineNotification = DeadlineNotification(getApplication<Application>().applicationContext)
        _deadlines.value?.let { deadlineNotification.updateNotifications(it) }

        // Listen for authentication updates, upon which the deadline list is re-fetched
        authRepository.onUpdate {
            deadlineRepository.setUser(it)
            refreshDeadlines() { it2 ->
                deadlineNotification.updateNotifications(it2)
            }
        }

        // Listen for changes in the deadline list as well, in order to synchronize between the
        // Firebase database contents
        deadlineRepository.onUpdate { _deadlines.value = it }
    }

    /**
     * Re-fetch all deadlines from the repository and assign the value to the LiveData.
     */
    private fun refreshDeadlines(callback: (Map<DeadlineID, Deadline>) -> Unit) = viewModelScope.launch {
        _deadlines.value = deadlineRepository.fetchAll()
        callback(_deadlines.value!!)
    }

    /**
     * Get all deadlines.
     */
    fun getDeadlines(): LiveData<Map<DeadlineID, Deadline>> =
        _deadlines

    /**
     * Add a new deadline to the repository.
     */
    fun addDeadline(deadline: Deadline, callback: (DeadlineID)->Unit) = viewModelScope.launch {
        val id = deadlineRepository.put(deadline)
        callback(id)
    }

    /**
     * Remove a deadline from the repository.
     */
    fun deleteDeadline(id: DeadlineID, callback: (DeadlineID) -> Unit) = viewModelScope.launch {
        deadlineRepository.delete(id)
        callback(id)
    }

    /**
     * Modify a deadline from the repository.
     */
    fun modifyDeadline(id: DeadlineID, newDeadline: Deadline) = viewModelScope.launch {
        deadlineRepository.modify(id, newDeadline)
    }
}
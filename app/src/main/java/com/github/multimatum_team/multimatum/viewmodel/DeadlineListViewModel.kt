package com.github.multimatum_team.multimatum.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.repository.AuthRepository
import com.github.multimatum_team.multimatum.repository.DeadlineID
import com.github.multimatum_team.multimatum.repository.DeadlineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

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
    private val deadlineRepository: DeadlineRepository
) : ViewModel() {
    private val _deadlines: MutableLiveData<Map<DeadlineID, Deadline>> = MutableLiveData()

    init {
        deadlineRepository.setUser(runBlocking { authRepository.getCurrentUser() })

        // Initialize the deadline repository with the currently logged in user, then fetch the data
        // to initialize the deadline list
        viewModelScope.launch {
            _deadlines.value = deadlineRepository.fetchAll()
        }

        // Listen for authentication updates, upon which the deadline list is re-fetched
        authRepository.onUpdate {
            deadlineRepository.setUser(it)
            refreshDeadlines()
        }

        // Listen for changes in the deadline list as well, in order to synchronize between the
        // Firebase database contents
        deadlineRepository.onUpdate { _deadlines.value = it }
    }

    /**
     * Re-fetch all deadlines from the repository and assign the value to the LiveData.
     */
    private fun refreshDeadlines() = viewModelScope.launch {
        _deadlines.value = deadlineRepository.fetchAll()
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
    fun addDeadline(deadline: Deadline) = viewModelScope.launch {
        deadlineRepository.put(deadline)
    }

    /**
     * Remove a deadline from the repository.
     */
    fun deleteDeadline(id: DeadlineID) = viewModelScope.launch {
        deadlineRepository.delete(id)
    }
}
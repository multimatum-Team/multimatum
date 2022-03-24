package com.github.multimatum_team.multimatum.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.repository.DeadlineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the deadline list view.
 * Acts as a bridge between the deadline repository (Firebase) and the user interface.
 * The ViewModel ensures that the data is always fresh, keeping the database and the UI in sync.
 */
@HiltViewModel
class DeadlineListViewModel @Inject constructor(
    private val deadlineRepository: DeadlineRepository
) : ViewModel() {
    private val _deadlines: MutableLiveData<List<Deadline>> = MutableLiveData()

    init {
        viewModelScope.launch {
            _deadlines.value = deadlineRepository.fetchAll()
        }

        deadlineRepository.onUpdate { _deadlines.value = it }
    }

    val deadlines: LiveData<List<Deadline>>
        get() = _deadlines
}
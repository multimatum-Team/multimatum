package com.github.multimatum_team.multimatum.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.repository.DeadlineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeadlineListViewModel @Inject constructor(
    deadlineRepository: DeadlineRepository
) : ViewModel() {
    val deadlines: MutableLiveData<List<Deadline>> = MutableLiveData()

    init {
        viewModelScope.launch {
            deadlines.value = deadlineRepository.fetchAll()
        }
    }
}
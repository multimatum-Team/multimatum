package com.github.multimatum_team.multimatum.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.github.multimatum_team.multimatum.repository.DeadlineRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class DeadlineListViewModel @Inject constructor(
    deadlineRepository: DeadlineRepository
) : ViewModel() {
    private val _deadlines: MutableLiveData<List<Deadline>> = MutableLiveData()

    init {
        viewModelScope.launch {
            _deadlines.value = deadlineRepository.fetchAll()
        }
    }

    val deadlines: LiveData<List<Deadline>>
        get() {
            Firebase.firestore
                .collection("deadlines")
                .addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
                    if (e != null) {
                        Log.w("DeadlineListViewModel", "Listen failed.", e)
                        _deadlines.value = null
                        return@EventListener
                    }

                    val deadlineList: MutableList<Deadline> = mutableListOf()
                    for (deadlineSnapshot in value!!) {
                        val title = deadlineSnapshot["title"] as String
                        val state = DeadlineState.values()[(deadlineSnapshot["state"] as Long).toInt()]
                        val timestamp = deadlineSnapshot["date"] as Timestamp
                        val milliseconds = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
                        val date = Instant
                            .ofEpochMilli(milliseconds)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        deadlineList.add(Deadline(title, state, date))
                    }
                    _deadlines.value = deadlineList
                })

            return _deadlines
        }
}
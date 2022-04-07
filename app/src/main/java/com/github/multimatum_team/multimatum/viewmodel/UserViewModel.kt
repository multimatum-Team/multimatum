package com.github.multimatum_team.multimatum.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.multimatum_team.multimatum.model.User
import com.github.multimatum_team.multimatum.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _user: MutableLiveData<User> = MutableLiveData()

    init {
        viewModelScope.launch {
            _user.value = authRepository.getCurrentUser()
        }

        authRepository.onUpdate { newUser ->
            Log.d("UserViewModel", "update: $newUser")
            _user.value = newUser
        }
    }

    fun getUser(): LiveData<User> =
        _user

    fun signOut() = viewModelScope.launch {
        _user.value = authRepository.signOut()
    }
}
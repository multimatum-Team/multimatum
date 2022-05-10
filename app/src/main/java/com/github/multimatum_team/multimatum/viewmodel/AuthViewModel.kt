package com.github.multimatum_team.multimatum.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.multimatum_team.multimatum.LogUtil
import com.github.multimatum_team.multimatum.model.SignedInUser
import com.github.multimatum_team.multimatum.model.User
import com.github.multimatum_team.multimatum.repository.AuthRepository
import com.github.multimatum_team.multimatum.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _user: MutableLiveData<User> = MutableLiveData()

    init {
        authRepository.getUser().let { user ->
            LogUtil.debugLog("initiating viewmodel with user $user")
            _user.value = user
        }

        authRepository.onUpdate { newUser ->
            LogUtil.debugLog("update auth: $newUser")
            _user.value = newUser
        }
    }

    fun getUser(): LiveData<User> =
        _user

    fun signOut() = viewModelScope.launch {
        LogUtil.debugLog("signing out from ${_user.value}")
        authRepository.signOut()
    }
}
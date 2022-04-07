package com.github.multimatum_team.multimatum.repository

import com.github.multimatum_team.multimatum.model.AnonymousUser
import com.github.multimatum_team.multimatum.model.User

interface AuthRepository {
    suspend fun getCurrentUser(): User

    suspend fun signOut(): AnonymousUser

    fun onUpdate(callback: (User) -> Unit)
}
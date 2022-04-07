package com.github.multimatum_team.multimatum.util

import com.github.multimatum_team.multimatum.model.AnonymousUser
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.User
import com.github.multimatum_team.multimatum.repository.AuthRepository
import com.github.multimatum_team.multimatum.repository.DeadlineID
import com.github.multimatum_team.multimatum.repository.DeadlineRepository

/**
 * Defines a dummy AuthRepository that works locally by generating unique anonymous users.
 */
class MockAuthRepository : AuthRepository {
    private var uniqueIDSupply = 0

    private var user = generateNewAnonymousUser()

    private val updateListeners: MutableList<(User) -> Unit> =
        mutableListOf()

    private fun notifyUpdateListeners() =
        updateListeners.forEach { it(user) }

    private fun generateNewAnonymousUser(): AnonymousUser =
        AnonymousUser((uniqueIDSupply++).toString())

    override suspend fun getCurrentUser(): User =
        user

    override suspend fun signOut(): AnonymousUser {
        user = AnonymousUser((uniqueIDSupply++).toString())
        notifyUpdateListeners()
        return user
    }

    override fun onUpdate(callback: (User) -> Unit) {
        updateListeners.add(callback)
    }
}
package com.github.multimatum_team.multimatum.util

import com.github.multimatum_team.multimatum.model.AnonymousUser
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.SignedInUser
import com.github.multimatum_team.multimatum.model.User
import com.github.multimatum_team.multimatum.repository.AuthRepository
import com.github.multimatum_team.multimatum.repository.DeadlineID
import com.github.multimatum_team.multimatum.repository.DeadlineRepository

/**
 * Defines a dummy AuthRepository that works locally by generating unique anonymous users.
 */
class MockAuthRepository : AuthRepository {
    private var uniqueIDSupply = 0

    private var user: User = generateNewAnonymousUser()

    private val updateListeners: MutableList<(User) -> Unit> =
        mutableListOf()

    private fun notifyUpdateListeners() =
        updateListeners.forEach { it(user) }

    private fun generateNewAnonymousUser(): AnonymousUser =
        AnonymousUser((uniqueIDSupply++).toString())

    override suspend fun getCurrentUser(): User =
        user

    fun signIn(email: String) {
        user = SignedInUser(user.id, email)
        notifyUpdateListeners()
    }

    fun logIn(newUser: User) {
        user = newUser
        notifyUpdateListeners()
    }

    override suspend fun signOut(): AnonymousUser {
        val anonymousUser = AnonymousUser((uniqueIDSupply++).toString())
        user = anonymousUser
        notifyUpdateListeners()
        return anonymousUser
    }

    override fun onUpdate(callback: (User) -> Unit) {
        updateListeners.add(callback)
    }
}
package com.github.multimatum_team.multimatum.util

import com.github.multimatum_team.multimatum.model.AnonymousUser
import com.github.multimatum_team.multimatum.model.SignedInUser
import com.github.multimatum_team.multimatum.model.User
import com.github.multimatum_team.multimatum.repository.AuthRepository

/**
 * Defines a dummy AuthRepository that works locally by generating unique anonymous users.
 */
class MockAuthRepository : AuthRepository() {
    private var uniqueIDSupply = 0

    init {
        _user = generateNewAnonymousUser()
    }

    private val updateListeners: MutableList<(User) -> Unit> =
        mutableListOf()

    private fun notifyUpdateListeners() =
        updateListeners.forEach { it(_user) }

    private fun generateNewAnonymousUser(): AnonymousUser =
        AnonymousUser((uniqueIDSupply++).toString())

    fun signIn(name: String, email: String) {
        _user = SignedInUser(_user.id, name, email)
        notifyUpdateListeners()
    }

    fun logIn(newUser: User) {
        _user = newUser
        notifyUpdateListeners()
    }

    override suspend fun signOut(): AnonymousUser {
        val anonymousUser = AnonymousUser((uniqueIDSupply++).toString())
        _user = anonymousUser
        notifyUpdateListeners()
        return anonymousUser
    }

    override fun onUpdate(callback: (User) -> Unit) {
        updateListeners.add(callback)
    }
}
package com.github.multimatum_team.multimatum.repository

import com.github.multimatum_team.multimatum.model.*

/**
 * An interface to keep track of the currently logged-in user.
 */
abstract class AuthRepository {
    protected lateinit var _user: User

    /**
     * Get the currently logged-in user.
     * Notice that the return type is not nullable since we assume at all time that an user is
     * logged in.
     */
    fun getUser(): User = _user

    /**
     * Sign-out from the current account.
     * We return the freshly created anonymous user on which the application falls back.
     */
    abstract suspend fun signOut(): AnonymousUser?

    /**
     * Add a callback to run when the user is updated (e.g. on sign-in and sign-out events).
     */
    abstract fun onUpdate(callback: (User) -> Unit)
}
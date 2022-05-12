package com.github.multimatum_team.multimatum.repository

import com.github.multimatum_team.multimatum.model.*

/**
 * An interface to the user repository.
 * This lets us get publicly available information about signed-in users.
 */
abstract class UserRepository {
    /**
     * Get public information of a user identified by a user ID.
     */
    open suspend fun fetch(id: UserID): UserInfo =
        fetch(listOf(id))[0]

    /**
     * Get public information of multiple users at once.
     */
    abstract suspend fun fetch(ids: List<UserID>): List<UserInfo>

    /**
     * Add information about an signed-in user.
     */
    abstract suspend fun setInfo(userInfo: UserInfo)
}
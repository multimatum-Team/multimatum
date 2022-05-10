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
    abstract suspend fun fetch(id: UserID): UserInfo

    /**
     * Add information about an signed-in user.
     */
    abstract suspend fun add(userInfo: UserInfo)
}
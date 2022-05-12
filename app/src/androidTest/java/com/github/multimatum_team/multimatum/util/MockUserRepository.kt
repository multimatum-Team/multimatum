package com.github.multimatum_team.multimatum.util

import com.github.multimatum_team.multimatum.model.AnonymousUser
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.UserID
import com.github.multimatum_team.multimatum.model.UserInfo
import com.github.multimatum_team.multimatum.repository.DeadlineID
import com.github.multimatum_team.multimatum.repository.DeadlineRepository
import com.github.multimatum_team.multimatum.repository.UserRepository

/**
 * Defines a dummy user info repository that works locally on a plain list.
 * This way the tests are completely independent from Firebase or network availability.
 */
class MockUserRepository(initialContents: List<UserInfo>) : UserRepository() {
    private val userInfoMap: MutableMap<UserID, UserInfo> =
        initialContents
            .associateBy { it.id }
            .toMutableMap()

    override suspend fun fetch(ids: List<UserID>): List<UserInfo> =
        ids.map { id -> userInfoMap[id]!! }

    override suspend fun setInfo(userInfo: UserInfo) {
        userInfoMap[userInfo.id] = userInfo
    }
}
package com.github.multimatum_team.multimatum

import com.github.multimatum_team.multimatum.model.UserGroup
import com.github.multimatum_team.multimatum.repository.GroupRepository
import com.github.multimatum_team.multimatum.util.MockGroupRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@ExperimentalCoroutinesApi
class GroupRepositoryTest {
    private val repository: GroupRepository = MockGroupRepository(
        listOf(
            UserGroup("", "Group 1", "0", setOf("0", "1", "2")),
            UserGroup("", "Group 2", "1", setOf("0", "1")),
            UserGroup("", "Group 3", "0", setOf("0", "2")),
        )
    )

    @Test
    fun `Default implementation of fetch returns the right group`() = runTest {
        assertEquals(
            UserGroup("0", "Group 1", "0", setOf("0", "1", "2")),
            repository.fetch("0")
        )
        assertEquals(
            UserGroup("1", "Group 2", "1", setOf("0", "1")),
            repository.fetch("1")
        )
        assertEquals(
            UserGroup("2", "Group 3", "0", setOf("0", "2")),
            repository.fetch("2")
        )
    }
}
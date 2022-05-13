package com.github.multimatum_team.multimatum

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.model.UserInfo
import com.github.multimatum_team.multimatum.repository.UserRepository
import com.github.multimatum_team.multimatum.util.MockUserRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@HiltAndroidTest
class UserRepositoryTest {
    private val repository: UserRepository = MockUserRepository(
        listOf(
            UserInfo("0", "Valentin"),
            UserInfo("1", "Léo"),
            UserInfo("2", "Florian"),
            UserInfo("3", "Lenny"),
            UserInfo("4", "Louis"),
            UserInfo("5", "Joseph"),
        )
    )

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Test
    fun `Default implementation of fetch returns the right deadline`() = runTest {
        assertEquals(
            UserInfo("0", "Valentin"),
            repository.fetch("0")
        )
        assertEquals(
            UserInfo("1", "Léo"),
            repository.fetch("1")
        )
        assertEquals(
            UserInfo("2", "Florian"),
            repository.fetch("2")
        )
    }
}
package com.github.multimatum_team.multimatum

import android.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.github.multimatum_team.multimatum.repository.AuthRepository
import com.github.multimatum_team.multimatum.repository.DeadlineRepository
import com.github.multimatum_team.multimatum.util.MockAuthRepository
import com.github.multimatum_team.multimatum.util.MockDeadlineRepository
import com.github.multimatum_team.multimatum.util.getOrAwaitValue
import com.github.multimatum_team.multimatum.viewmodel.DeadlineListViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@ExperimentalCoroutinesApi
class DeadlineListViewModelTest {
    private val deadlines: List<Deadline> = listOf(
        Deadline("Deadline 1", DeadlineState.DONE, LocalDateTime.of(2022, 3, 17, 0, 0)),
        Deadline("Deadline 2", DeadlineState.DONE, LocalDateTime.of(2022, 3, 20, 0, 0)),
        Deadline("Deadline 3", DeadlineState.TODO, LocalDateTime.of(2022, 4, 15, 0, 0)),
    )

    private lateinit var authRepository: AuthRepository
    private lateinit var deadlineRepository: DeadlineRepository

    private lateinit var viewModel: DeadlineListViewModel

    @Before
    fun setUp() {
        Intents.init()
        Dispatchers.setMain(UnconfinedTestDispatcher())
        hiltRule.inject()
        authRepository = MockAuthRepository()
        deadlineRepository = MockDeadlineRepository(deadlines)
        viewModel = DeadlineListViewModel(authRepository, deadlineRepository)
    }

    @After
    fun teardown(){
        Intents.release()
    }

    // Set executor to be synchronous so that LiveData's notify their observers immediately and
    // finish executing before continuing.
    @get:Rule
    val executorRule: TestRule = InstantTaskExecutorRule()

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Test
    fun `LiveData initially contains repository contents`() = runTest {
        val deadlines = viewModel.getDeadlines().getOrAwaitValue()
        assertEquals(
            deadlines["0"],
            Deadline("Deadline 1", DeadlineState.DONE, LocalDateTime.of(2022, 3, 17, 0, 0))
        )
        assertEquals(
            deadlines["1"],
            Deadline("Deadline 2", DeadlineState.DONE, LocalDateTime.of(2022, 3, 20, 0, 0))
        )
        assertEquals(
            deadlines["2"],
            Deadline("Deadline 3", DeadlineState.TODO, LocalDateTime.of(2022, 4, 15, 0, 0))
        )
    }

    @Test
    fun `LiveData is updated when new deadline is put into the repository`() = runTest {
        val newDeadline =
            Deadline("Deadline 4", DeadlineState.TODO, LocalDateTime.of(2022, 5, 2, 0, 0))
        val newDeadlineList = deadlines.toMutableList()
        newDeadlineList.add(newDeadline)
        deadlineRepository.put(newDeadline).run {
            assertEquals(viewModel.getDeadlines().getOrAwaitValue()[this]!!, newDeadline)
        }
    }
}
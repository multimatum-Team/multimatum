package com.github.multimatum_team.multimatum

import android.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.github.multimatum_team.multimatum.repository.DeadlineRepository
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
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@ExperimentalCoroutinesApi
class DeadlineListViewModelTest {
    private val deadlines: List<Deadline> = listOf(
        Deadline("Deadline 1", DeadlineState.DONE, LocalDate.of(2022, 3, 17)),
        Deadline("Deadline 2", DeadlineState.DONE, LocalDate.of(2022, 3, 20)),
        Deadline("Deadline 3", DeadlineState.TODO, LocalDate.of(2022, 4, 15)),
    )
    private lateinit var repository: DeadlineRepository

    private lateinit var viewModel: DeadlineListViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        hiltRule.inject()
        repository = MockDeadlineRepository(deadlines)
        viewModel = DeadlineListViewModel(repository)
    }

    // Set executor to be synchronous so that LiveData's notify their observers immediately and
    // finish executing before continuing.
    @get:Rule
    val executorRule: TestRule = InstantTaskExecutorRule()

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Test
    fun `LiveData initially contains repository contents`() = runTest {
        val deadlines = viewModel.deadlines.getOrAwaitValue()
        assertEquals(deadlines["0"], Deadline("Deadline 1", DeadlineState.DONE, LocalDate.of(2022, 3, 17)))
        assertEquals(deadlines["1"], Deadline("Deadline 2", DeadlineState.DONE, LocalDate.of(2022, 3, 20)))
        assertEquals(deadlines["2"], Deadline("Deadline 3", DeadlineState.TODO, LocalDate.of(2022, 4, 15)))
    }

    @Test
    fun `LiveData is updated when new deadline is put into the repository`() = runTest {
        val newDeadline = Deadline("Deadline 4", DeadlineState.TODO, LocalDate.of(2022, 5, 2))
        val newDeadlineList = deadlines.toMutableList()
        newDeadlineList.add(newDeadline)
        repository.put(newDeadline).run {
            assertEquals(viewModel.deadlines.getOrAwaitValue()[this]!!, newDeadline)
        }
    }
}
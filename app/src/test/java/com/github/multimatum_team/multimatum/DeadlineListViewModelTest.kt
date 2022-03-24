package com.github.multimatum_team.multimatum

import android.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.github.multimatum_team.multimatum.repository.DeadlineRepository
import com.github.multimatum_team.multimatum.util.MockDeadlineRepository
import com.github.multimatum_team.multimatum.util.getOrAwaitValue
import com.github.multimatum_team.multimatum.viewmodel.DeadlineListViewModel
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
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
@UninstallModules(DependenciesProvider::class)
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
        repository = MockDeadlineRepository(deadlines)
        viewModel = DeadlineListViewModel(repository)
    }

    // Set executor to be synchronous so that LiveData's notify their observers immediately and
    // finish executing before continuing.
    @get:Rule
    val executorRule: TestRule = InstantTaskExecutorRule()

    @Test
    fun liveDataInitiallyContainsRepositoryContents() {
        assertEquals(viewModel.deadlines.getOrAwaitValue(), deadlines)
    }

    @Test
    fun liveDataIsUpdatedWhenNewDeadlineIsPutIntoRepository() = runTest {
        val newDeadline = Deadline("Deadline 4", DeadlineState.TODO, LocalDate.of(2022, 5, 2))
        val newDeadlineList = deadlines.toMutableList()
        newDeadlineList.add(newDeadline)
        repository.put(newDeadline)
        assertEquals(viewModel.deadlines.getOrAwaitValue(), newDeadlineList)
    }
}
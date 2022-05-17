package com.github.multimatum_team.multimatum

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.model.AnonymousUser
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.github.multimatum_team.multimatum.model.UserID
import com.github.multimatum_team.multimatum.repository.*
import com.github.multimatum_team.multimatum.util.*
import com.github.multimatum_team.multimatum.viewmodel.DeadlineListViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
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
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@UninstallModules(FirebaseRepositoryModule::class)
@ExperimentalCoroutinesApi
class DeadlineListViewModelTest {
    companion object {
        private val deadlines: Map<UserID, List<Deadline>> = sortedMapOf(
            "alice" to listOf(
                Deadline(
                    "Alice's deadline 1",
                    DeadlineState.DONE,
                    LocalDateTime.of(2022, 3, 17, 0, 0)
                ),
                Deadline(
                    "Alice's deadline 2",
                    DeadlineState.DONE,
                    LocalDateTime.of(2022, 3, 20, 0, 0)
                ),
                Deadline(
                    "Alice's deadline 3",
                    DeadlineState.TODO,
                    LocalDateTime.of(2022, 4, 15, 0, 0)
                ),
            ),
            "bob" to listOf(
                Deadline(
                    "Bob's deadline 1",
                    DeadlineState.DONE,
                    LocalDateTime.of(2022, 3, 17, 0, 0)
                ),
                Deadline(
                    "Bob's deadline 2",
                    DeadlineState.TODO,
                    LocalDateTime.of(2022, 4, 15, 0, 0)
                ),
            ),
        )
    }

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var groupRepository: GroupRepository

    @Inject
    lateinit var deadlineRepository: DeadlineRepository

    private lateinit var viewModel: DeadlineListViewModel


    // Set executor to be synchronous so that LiveData's notify their observers immediately and
    // finish executing before continuing.
    @get:Rule
    val executorRule: TestRule = InstantTaskExecutorRule()

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        hiltRule.inject()
        (authRepository as MockAuthRepository).logIn(AnonymousUser("alice"))
        viewModel = DeadlineListViewModel(
            ApplicationProvider.getApplicationContext(),
            authRepository,
            groupRepository,
            deadlineRepository
        )
    }

    @Test
    fun `LiveData initially contains repository contents`() = runTest {
        val deadlines = viewModel.getDeadlines().getOrAwaitValue()
        assertEquals(
            deadlines["0"],
            Deadline("Alice's deadline 1", DeadlineState.DONE, LocalDateTime.of(2022, 3, 17, 0, 0))
        )
        assertEquals(
            deadlines["1"],
            Deadline("Alice's deadline 2", DeadlineState.DONE, LocalDateTime.of(2022, 3, 20, 0, 0))
        )
        assertEquals(
            deadlines["2"],
            Deadline("Alice's deadline 3", DeadlineState.TODO, LocalDateTime.of(2022, 4, 15, 0, 0))
        )
    }

    @Test
    fun `LiveData is updated when new deadline is put into the repository`() = runTest {
        val newDeadline =
            Deadline("Alice's deadline 4", DeadlineState.TODO, LocalDateTime.of(2022, 5, 2, 0, 0))
        val id = deadlineRepository.put(newDeadline)
        assertEquals(viewModel.getDeadlines().getOrAwaitValue()[id]!!, newDeadline)
    }

    @Test
    fun `LiveData is updated when deadline is removed from the repository`() = runTest {
        val newDeadlineMap = deadlines["alice"]!!
            .withIndex()
            .associate { Pair(it.index.toString(), it.value) }
            .toMutableMap()
        newDeadlineMap.remove("1")
        deadlineRepository.delete("1")
        assertEquals(viewModel.getDeadlines().getOrAwaitValue(), newDeadlineMap)
    }

    @Test
    fun `LiveData is updated when deadline is modified in the repository`() = runTest {
        val modifiedDeadline =
            Deadline("Alice's deadline 2", DeadlineState.TODO, LocalDateTime.of(2022, 3, 20, 0, 0))
        deadlineRepository.modify("1", modifiedDeadline)
        assertEquals(viewModel.getDeadline("1"), modifiedDeadline)
    }

    @Test
    fun `Adding deadlines in the viewmodel updates the repository`() = runTest {
        val newDeadline =
            Deadline("Alice's deadline 4", DeadlineState.TODO, LocalDateTime.of(2022, 6, 13, 0, 0))
        viewModel.addDeadline(newDeadline) { id ->
            assertEquals(runTest { deadlineRepository.fetch(id) }, newDeadline)
        }
    }

    @Test
    fun `Deleting deadlines from the viewmodel updates the repository`() = runTest {
        val newDeadlineMap = deadlines["alice"]!!
            .withIndex()
            .associate { Pair(it.index.toString(), it.value) }
            .toMutableMap()
        newDeadlineMap.remove("1")
        viewModel.deleteDeadline("1")
        assertEquals(deadlineRepository.fetchAll(), newDeadlineMap)
    }

    @Test
    fun `Modifying deadlines from the viewmodel updates the repository`() = runTest {
        val modifiedDeadline =
            Deadline("Alice's deadline 2", DeadlineState.TODO, LocalDateTime.of(2022, 3, 20, 0, 0))
        viewModel.modifyDeadline("1", modifiedDeadline)
        assertEquals(deadlineRepository.fetch("1"), modifiedDeadline)
    }

    @Test
    fun `Authenticating as a different user updates the viewmodel`() = runTest {
        (authRepository as MockAuthRepository).logIn(AnonymousUser("bob"))
        val deadlines = viewModel.getDeadlines().getOrAwaitValue()
        assertEquals(
            deadlines["3"],
            Deadline("Bob's deadline 1", DeadlineState.DONE, LocalDateTime.of(2022, 3, 17, 0, 0))
        )
        assertEquals(
            deadlines["4"],
            Deadline("Bob's deadline 2", DeadlineState.TODO, LocalDateTime.of(2022, 4, 15, 0, 0))
        )
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestRepositoryModule {
        @Singleton
        @Provides
        fun provideDeadlineRepository(): DeadlineRepository =
            MockDeadlineRepository(deadlines)

        @Singleton
        @Provides
        fun provideGroupRepository(): GroupRepository =
            MockGroupRepository(listOf())

        @Singleton
        @Provides
        fun provideAuthRepository(): AuthRepository =
            MockAuthRepository()

        @Singleton
        @Provides
        fun provideUserRepository(): UserRepository =
            MockUserRepository(listOf())

        @Singleton
        @Provides
        fun providePdfRepository(): PdfRepository =
            MockPdfRepository()
    }
}
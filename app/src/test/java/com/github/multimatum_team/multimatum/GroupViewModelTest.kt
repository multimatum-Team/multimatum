package com.github.multimatum_team.multimatum

import android.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.model.AnonymousUser
import com.github.multimatum_team.multimatum.model.UserGroup
import com.github.multimatum_team.multimatum.repository.AuthRepository
import com.github.multimatum_team.multimatum.repository.DeadlineRepository
import com.github.multimatum_team.multimatum.repository.GroupRepository
import com.github.multimatum_team.multimatum.util.MockAuthRepository
import com.github.multimatum_team.multimatum.util.MockDeadlineRepository
import com.github.multimatum_team.multimatum.util.MockGroupRepository
import com.github.multimatum_team.multimatum.util.getOrAwaitValue
import com.github.multimatum_team.multimatum.viewmodel.GroupViewModel
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
import javax.inject.Inject
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@UninstallModules(FirebaseRepositoryModule::class)
@ExperimentalCoroutinesApi
class GroupViewModelTest {
    companion object {
        private val groups: List<UserGroup> = listOf(
            UserGroup("0", "Group 1", "alice", setOf("alice", "bob", "charlie")),
            UserGroup("1", "Group 2", "alice", setOf("alice", "charlie")),
            UserGroup("2", "Group 3", "charlie", setOf("bob", "charlie")),
        )
    }

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var groupRepository: GroupRepository

    private lateinit var viewModel: GroupViewModel


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
        viewModel = GroupViewModel(
            ApplicationProvider.getApplicationContext(),
            authRepository,
            groupRepository
        )
    }

    @Test
    fun `LiveData initially contains repository contents`() = runTest {
        val groups = viewModel.getGroups().getOrAwaitValue()
        assertEquals(
            groups["0"],
            UserGroup("0", "Group 1", "alice", setOf("alice", "bob", "charlie"))
        )
        assertEquals(
            groups["1"],
            UserGroup("1", "Group 2", "alice", setOf("alice", "charlie"))
        )
    }

    @Test
    fun `LiveData is updated when new empty group is created`() = runTest {
        val id = groupRepository.create("Group 4")
        assertEquals(
            UserGroup("3", "Group 4", "alice"),
            viewModel.getGroups().getOrAwaitValue()[id]!!
        )
    }

    @Test
    fun `LiveData is updated when group is removed from the repository`() = runTest {
        groupRepository.delete("1")
        assertEquals(
            mapOf(
                "0" to UserGroup("0", "Group 1", "alice", setOf("alice", "bob", "charlie"))
            ),
            viewModel.getGroups().getOrAwaitValue()
        )
    }

    @Test
    fun `LiveData is updated when group is renamed`() = runTest {
        val newName = "New group 1 name"
        val renamedGroup = groups[0].copy(name = newName)
        groupRepository.rename("0", newName)
        assertEquals(renamedGroup, viewModel.getGroup("0"))
    }

    @Test
    fun `Adding a group in the viewmodel updates the repository`() {
        viewModel.createGroup("Group 4") { id ->
            assertEquals(
                UserGroup("3", "Group 4", "alice"),
                runTest { groupRepository.fetch(id) }
            )
        }
    }

    @Test
    fun `Deleting a group from the viewmodel updates the repository`() = runTest {
        viewModel.deleteGroup("1")
        assertEquals(
            mapOf(
                "0" to UserGroup("0", "Group 1", "alice", setOf("alice", "bob", "charlie"))
            ),
            groupRepository.fetchAll()
        )
    }

    @Test
    fun `Modifying a group from the viewmodel updates the repository`() = runTest {
        val newName = "New group 1 name"
        val renamedGroup = groups[0].copy(name = newName)
        viewModel.renameGroup("0", newName)
        assertEquals(renamedGroup, groupRepository.fetch("0"))
    }

    @Test
    fun `Authenticating as a different user updates the viewmodel`() = runTest {
        (authRepository as MockAuthRepository).logIn(AnonymousUser("bob"))
        val groups = viewModel.getGroups().getOrAwaitValue()
        assertEquals(
            UserGroup("0", "Group 1", "alice", setOf("alice", "bob", "charlie")),
            groups["0"]
        )
        assertEquals(
            UserGroup("2", "Group 3", "charlie", setOf("bob", "charlie")),
            groups["2"]
        )
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestRepositoryModule {
        @Singleton
        @Provides
        fun provideDeadlineRepository(): DeadlineRepository =
            MockDeadlineRepository(listOf())

        @Singleton
        @Provides
        fun provideGroupRepository(): GroupRepository =
            MockGroupRepository(groups)

        @Singleton
        @Provides
        fun provideAuthRepository(): AuthRepository =
            MockAuthRepository()
    }
}
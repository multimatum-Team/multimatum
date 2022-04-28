package com.github.multimatum_team.multimatum

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.model.AnonymousUser
import com.github.multimatum_team.multimatum.model.UserGroup
import com.github.multimatum_team.multimatum.repository.FirebaseGroupRepository
import com.github.multimatum_team.multimatum.util.MockFirestore
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@HiltAndroidTest
@UninstallModules(FirebaseModule::class)
class FirebaseGroupRepositoryTest {
    @Inject
    lateinit var database: FirebaseFirestore

    lateinit var repository: FirebaseGroupRepository

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun setup() {
        hiltRule.inject()
        repository = FirebaseGroupRepository(database)
        repository.setUser(AnonymousUser("1"))
    }

    @Test
    fun `Fetching a group ID returns the correct group instance`() = runTest {
        assertEquals(
            UserGroup("1", "Group 2", "1", setOf("0", "1")),
            repository.fetch("1")
        )
    }

    @Test
    fun `Fetching all groups returns the correct group list`() = runTest {
        assertEquals(
            mapOf(
                "0" to UserGroup("0", "Group 1", "0", setOf("0", "1", "2")),
                "1" to UserGroup("1", "Group 2", "1", setOf("0", "1")),
            ),
            repository.fetchAll()
        )
    }

    @Test
    fun `Fetching all owned groups returns the correct group list`() = runTest {
        assertEquals(
            mapOf(
                "1" to UserGroup("1", "Group 2", "1", setOf("0", "1")),
            ),
            repository.fetchOwned()
        )
    }

    @Test
    fun `Creating a group inserts a new empty group owned by the current user`() = runTest {
        repository.create("Group 4")
        assertEquals(
            mapOf(
                "0" to UserGroup("0", "Group 1", "0", setOf("0", "1", "2")),
                "1" to UserGroup("1", "Group 2", "1", setOf("0", "1")),
                "3" to UserGroup("3", "Group 4", "1", setOf("1")),
            ),
            repository.fetchAll()
        )
    }

    @Test
    fun `Deleting a group owned by the current user removes it from the database`() = runTest {
        repository.delete("1")
        assertEquals(
            mapOf(
                "0" to UserGroup("0", "Group 1", "0", setOf("0", "1", "2"))
            ),
            repository.fetchAll()
        )
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestFirebaseModule {
        @Singleton
        @Provides
        fun provideFirebaseFirestore(): FirebaseFirestore =
            MockFirestore(
                listOf(
                    UserGroup("0", "Group 1", "0", setOf("0", "1", "2")),
                    UserGroup("1", "Group 2", "1", setOf("0", "1")),
                    UserGroup("2", "Group 3", "0", setOf("0", "2")),
                )
            ).database
    }
}
package com.github.multimatum_team.multimatum

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.model.UserGroup
import com.github.multimatum_team.multimatum.repository.FirebaseGroupRepository
import com.github.multimatum_team.multimatum.util.MockFirebaseAuth
import com.github.multimatum_team.multimatum.util.MockFirebaseFirestore
import com.github.multimatum_team.multimatum.util.mockFirebaseDynamicLinks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import javax.inject.Inject
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@HiltAndroidTest
@UninstallModules(FirebaseModule::class, FirebaseDynamicLinksModule::class)
class FirebaseGroupRepositoryTest {
    @Inject
    lateinit var database: FirebaseFirestore

    @Inject
    lateinit var dynamicLinks: FirebaseDynamicLinks

    lateinit var repository: FirebaseGroupRepository

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun setup() {
        hiltRule.inject()
        repository = FirebaseGroupRepository(database, dynamicLinks)
        repository.setUserID("1")
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

    /* Untestable even with mocking
    @Test
    fun `Removing a group member removes it from the database`() = runTest {
        repository.removeMember("0", "2")
        assertEquals(
            mapOf(
                "0" to UserGroup("0", "Group 1", "0", setOf("0", "1"))
            ),
            repository.fetch("0")
        )
    }
    */

    @Test
    fun `Creating a group notifies the owner`() = runTest {
        var notified = false
        repository.onUpdate { notified = true }
        repository.create("Group 4")
        assertTrue(notified)
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestFirebaseModule {
        @Singleton
        @Provides
        fun provideFirebaseFirestore(): FirebaseFirestore =
            MockFirebaseFirestore(
                deadlines = listOf(),
                groups = listOf(
                    UserGroup("0", "Group 1", "0", setOf("0", "1", "2")),
                    UserGroup("1", "Group 2", "1", setOf("0", "1")),
                    UserGroup("2", "Group 3", "0", setOf("0", "2")),
                ),
                users = listOf()
            ).database

        @Singleton
        @Provides
        fun provideFirebaseAuth(): FirebaseAuth =
            MockFirebaseAuth().auth

        @Singleton
        @Provides
        fun provideFirebaseStorage(): FirebaseStorage =
            Mockito.mock(FirebaseStorage::class.java)
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestDynamicLinksModule {
        @Singleton
        @Provides
        fun providesFirebaseDynamicLinks(): FirebaseDynamicLinks =
            mockFirebaseDynamicLinks()
    }
}
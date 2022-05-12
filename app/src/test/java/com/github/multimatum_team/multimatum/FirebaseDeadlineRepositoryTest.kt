package com.github.multimatum_team.multimatum

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.model.*
import com.github.multimatum_team.multimatum.repository.FirebaseDeadlineRepository
import com.github.multimatum_team.multimatum.util.*
import com.google.firebase.auth.FirebaseAuth
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@HiltAndroidTest
@UninstallModules(FirebaseModule::class)
class FirebaseDeadlineRepositoryTest {
    @Inject
    lateinit var database: FirebaseFirestore

    lateinit var repository: FirebaseDeadlineRepository

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun setup() {
        hiltRule.inject()
        repository = FirebaseDeadlineRepository(database)
        repository.setUser(AnonymousUser("0"))
        repository.setGroups(
            mapOf(
                "0" to UserGroup("0", "Group 1", "0", setOf("0", "1", "2")),
                "1" to UserGroup("1", "Group 2", "1", setOf("0", "1")),
                "2" to UserGroup("2", "Group 3", "0", setOf("0", "2")),
            )
        )
    }

    @Test
    fun `Fetching a deadline ID returns the correct deadline instance`() = runTest {
        assertEquals(
            Deadline(
                "Deadline 2",
                DeadlineState.DONE,
                LocalDateTime.of(2022, 3, 20, 0, 0),
                "Deadline 2 description",
                UserOwned,
                "foo/pdf2"
            ),
            repository.fetch("1")
        )
    }

    @Test
    fun `Fetching all deadlines returns the correct deadline map`() = runTest {
        assertEquals(
            mapOf(
                "1" to Deadline(
                    "Deadline 2",
                    DeadlineState.DONE,
                    LocalDateTime.of(2022, 3, 20, 0, 0),
                    "Deadline 2 description",
                    UserOwned,
                    "foo/pdf2"
                ),
                "2" to Deadline(
                    "Deadline 3",
                    DeadlineState.TODO,
                    LocalDateTime.of(2022, 4, 15, 0, 0),
                    "Deadline 3 description",
                    GroupOwned("0"),
                    "foo/pdf3"
                )
            ),
            repository.fetchAll()
        )
    }

    @Test
    fun `Fetching all personal deadlines returns the correct deadline map`() = runTest {
        assertEquals(
            mapOf(
                "1" to Deadline(
                    "Deadline 2",
                    DeadlineState.DONE,
                    LocalDateTime.of(2022, 3, 20, 0, 0),
                    "Deadline 2 description",
                    UserOwned,
                    "foo/pdf2"
                )
            ),
            repository.fetchFromOwner(UserOwned)
        )
    }

    @Test
    fun `Fetching all deadlines from group returns the correct deadline map`() = runTest {
        assertEquals(
            mapOf(
                "2" to Deadline(
                    "Deadline 3",
                    DeadlineState.TODO,
                    LocalDateTime.of(2022, 4, 15, 0, 0),
                    "Deadline 3 description",
                    GroupOwned("0"),
                    "foo/pdf3"
                )
            ),
            repository.fetchFromOwner(GroupOwned("0"))
        )
    }

    @Test
    fun `Creating a deadline inserts a new deadline owned by the current user`() = runTest {
        repository.put(
            Deadline(
                "Deadline 4",
                DeadlineState.TODO,
                LocalDateTime.of(2022, 5, 15, 0, 0),
                "Deadline 4 description",
                UserOwned,
                "foo/pdf4"
            )
        )
        assertEquals(
            mapOf(
                "1" to Deadline(
                    "Deadline 2",
                    DeadlineState.DONE,
                    LocalDateTime.of(2022, 3, 20, 0, 0),
                    "Deadline 2 description",
                    UserOwned,
                    "foo/pdf2"
                ),
                "2" to Deadline(
                    "Deadline 3",
                    DeadlineState.TODO,
                    LocalDateTime.of(2022, 4, 15, 0, 0),
                    "Deadline 3 description",
                    GroupOwned("0"),
                    "foo/pdf3"
                ),
                "3" to Deadline(
                    "Deadline 4",
                    DeadlineState.TODO,
                    LocalDateTime.of(2022, 5, 15, 0, 0),
                    "Deadline 4 description",
                    UserOwned,
                    "foo/pdf4"
                )
            ),
            repository.fetchAll()
        )
    }

    @Test
    fun `Deleting a deadline owned by the current user removes it from the database`() = runTest {
        repository.delete("1")
        assertEquals(
            mapOf(
                "2" to Deadline(
                    "Deadline 3",
                    DeadlineState.TODO,
                    LocalDateTime.of(2022, 4, 15, 0, 0),
                    "Deadline 3 description",
                    GroupOwned("0"),
                    "foo/pdf3"
                )
            ),
            repository.fetchAll()
        )
    }

    @Test
    fun `Creating a deadline notifies the owner`() = runTest {
        var notified = false
        repository.onUpdate { notified = true }
        repository.put(
            Deadline(
                "Deadline 4",
                DeadlineState.TODO,
                LocalDateTime.of(2022, 5, 15, 0, 0),
                "Deadline 4 description",
                UserOwned,
                "foo/pdf4"
            )
        )
        assertTrue(notified)
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestFirebaseModule {
        @Singleton
        @Provides
        fun provideFirebaseFirestore(): FirebaseFirestore =
            MockFirebaseFirestore(
                deadlines = listOf(
                    DeadlineData(
                        "Deadline 1",
                        DeadlineState.DONE,
                        LocalDateTime.of(2022, 3, 17, 0, 0),
                        "Deadline 1 description",
                        UserOwnedData("1"),
                        "foo/pdf1"
                    ),
                    DeadlineData(
                        "Deadline 2",
                        DeadlineState.DONE,
                        LocalDateTime.of(2022, 3, 20, 0, 0),
                        "Deadline 2 description",
                        UserOwnedData("0"),
                        "foo/pdf2"
                    ),
                    DeadlineData(
                        "Deadline 3",
                        DeadlineState.TODO,
                        LocalDateTime.of(2022, 4, 15, 0, 0),
                        "Deadline 3 description",
                        GroupOwnedData("0"),
                        "foo/pdf3"
                    )
                ),
                groups = listOf(
                    UserGroup("0", "Group 1", "0", setOf("0", "1", "2")),
                    UserGroup("1", "Group 2", "1", setOf("0", "1")),
                    UserGroup("2", "Group 3", "0", setOf("0", "2")),
                ),
                users = listOf(
                    UserInfo("0", "Pierre"),
                    UserInfo("1", "Paul"),
                    UserInfo("2", "Jacques"),
                )
            ).database

        @Singleton
        @Provides
        fun provideFirebaseAuth(): FirebaseAuth =
            MockFirebaseAuth().auth
    }
}
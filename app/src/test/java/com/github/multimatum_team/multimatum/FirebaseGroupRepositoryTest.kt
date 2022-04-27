package com.github.multimatum_team.multimatum

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.model.AnonymousUser
import com.github.multimatum_team.multimatum.model.UserGroup
import com.github.multimatum_team.multimatum.model.UserID
import com.github.multimatum_team.multimatum.repository.FirebaseGroupRepository
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
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
import org.mockito.Mockito.*
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

    object MockFirestore {
        private fun generateDocumentSnapshot(group: UserGroup): DocumentSnapshot {
            val snapshot = mock(DocumentSnapshot::class.java)
            `when`(snapshot.id).thenReturn(group.id)
            `when`(snapshot.get("name")).thenReturn(group.name)
            `when`(snapshot.get("owner")).thenReturn(group.owner)
            `when`(snapshot.get("members")).thenReturn(group.members.toList())
            return snapshot
        }

        private fun generateDocument(group: UserGroup): DocumentReference {
            val snapshot = generateDocumentSnapshot(group)
            val document = mock(DocumentReference::class.java)
            `when`(document.get()).thenReturn(Tasks.forResult(snapshot))
            return document
        }

        private fun generateQuerySnapshot(queryResult: List<UserGroup>): QuerySnapshot {
            val querySnapshot = mock(QuerySnapshot::class.java)
            val groupSnapshots = queryResult.map { generateDocumentSnapshot(it) }
            `when`(querySnapshot.documents).thenReturn(groupSnapshots)
            return querySnapshot
        }

        private fun generateMemberQuery(groups: List<UserGroup>, userID: UserID): Query {
            val query = mock(Query::class.java)
            val querySnapshot = generateQuerySnapshot(groups.filter { group ->
                group.members.any { it == userID }
            })
            `when`(query.get()).thenReturn(Tasks.forResult(querySnapshot))
            return query
        }

        private fun generateOwnerQuery(groups: List<UserGroup>, userID: UserID): Query {
            val query = mock(Query::class.java)
            val querySnapshot = generateQuerySnapshot(groups.filter { group ->
                group.owner == userID
            })
            `when`(query.get()).thenReturn(Tasks.forResult(querySnapshot))
            return query
        }

        private fun generateCollection(groups: List<UserGroup>): CollectionReference {
            val collection = mock(CollectionReference::class.java)
            for (group in groups) {
                val document = generateDocument(group)
                `when`(collection.document(group.id)).thenReturn(document)
            }
            `when`(collection.whereArrayContains(eq("members"), anyString())).then {
                val userID = it.getArgument<String>(1)
                generateMemberQuery(groups, userID)
            }
            `when`(collection.whereEqualTo(eq("owner"), anyString())).then {
                val userID = it.getArgument<String>(1)
                generateOwnerQuery(groups, userID)
            }
            return collection
        }

        fun generate(groups: List<UserGroup>): FirebaseFirestore {
            val collection = generateCollection(groups)
            val database = mock(FirebaseFirestore::class.java)
            `when`(database.collection("groups")).thenReturn(collection)
            return database
        }
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestFirebaseModule {
        @Singleton
        @Provides
        fun provideFirebaseFirestore(): FirebaseFirestore =
            MockFirestore.generate(
                listOf(
                    UserGroup("0", "Group 1", "0", setOf("0", "1", "2")),
                    UserGroup("1", "Group 2", "1", setOf("0", "1")),
                    UserGroup("2", "Group 3", "0", setOf("0", "2")),
                )
            )
    }
}
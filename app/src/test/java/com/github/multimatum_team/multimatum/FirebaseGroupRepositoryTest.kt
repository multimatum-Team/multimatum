package com.github.multimatum_team.multimatum

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.model.AnonymousUser
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.github.multimatum_team.multimatum.model.UserGroup
import com.github.multimatum_team.multimatum.repository.FirebaseDeadlineRepository
import com.github.multimatum_team.multimatum.repository.FirebaseGroupRepository
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
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
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.time.LocalDateTime
import java.time.ZoneId
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
        repository.setUser(AnonymousUser("alice"))
    }

    @Test
    fun `Fetching a group ID returns the correct group instance`() = runTest {
        assertEquals(
            UserGroup(
                "testid",
                "test group",
                "alice",
                setOf("alice", "bob")
            ),
            repository.fetch("testid")
        )
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestFirebaseModule {
        @Singleton
        @Provides
        fun provideFirebaseFirestore(): FirebaseFirestore {
            val mockSnapshot = mock(DocumentSnapshot::class.java)
            `when`(mockSnapshot.id).thenReturn("testid")
            `when`(mockSnapshot.get("name")).thenReturn("test group")
            `when`(mockSnapshot.get("owner")).thenReturn("alice")
            `when`(mockSnapshot.get("members")).thenReturn(listOf("alice", "bob"))
            val mockDocument = mock(DocumentReference::class.java)
            `when`(mockDocument.get()).thenReturn(Tasks.forResult(mockSnapshot))
            val mockCollection = mock(CollectionReference::class.java)
            `when`(mockCollection.document("testid")).thenReturn(mockDocument)
            val mockDatabase = mock(FirebaseFirestore::class.java)
            `when`(mockDatabase.collection("groups")).thenReturn(mockCollection)
            return mockDatabase
        }
    }
}
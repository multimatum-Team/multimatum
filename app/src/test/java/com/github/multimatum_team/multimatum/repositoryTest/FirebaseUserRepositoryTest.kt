package com.github.multimatum_team.multimatum.repositoryTest

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.FirebaseModule
import com.github.multimatum_team.multimatum.model.UserInfo
import com.github.multimatum_team.multimatum.repository.FirebaseUserRepository
import com.github.multimatum_team.multimatum.util.MockFirebaseAuth
import com.github.multimatum_team.multimatum.util.MockFirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import javax.inject.Inject
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@HiltAndroidTest
@UninstallModules(FirebaseModule::class)
class FirebaseUserRepositoryTest {
    @Inject
    lateinit var database: FirebaseFirestore

    lateinit var repository: FirebaseUserRepository

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun setup() {
        hiltRule.inject()
        repository = FirebaseUserRepository(database)
    }

    @Test
    fun `Fetching user info works`() = runTest {
        assertEquals(
            UserInfo("1", "Léo"),
            repository.fetch("1")
        )
    }

    @Test
    fun `Fetching multiple user info at once works`() = runTest {
        assertEquals(
            listOf(
                UserInfo("0", "Valentin"),
                UserInfo("3", "Lenny"),
            ),
            repository.fetch(listOf("0", "3"))
        )
    }

    @Test
    fun `Setting info for a new user updates the repository correctly`() = runTest {
        repository.setInfo(UserInfo("6", "Yugesh"))
        assertEquals(
            repository.fetch("6"),
            UserInfo("6", "Yugesh")
        )
    }

    @Test
    fun `Setting info for an existing user updates the user correctly`() = runTest {
        repository.setInfo(UserInfo("4", "Louise"))
        assertEquals(
            repository.fetch("4"),
            UserInfo("4", "Louise")
        )
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestFirebaseModule {
        @Singleton
        @Provides
        fun provideFirebaseFirestore(): FirebaseFirestore =
            MockFirebaseFirestore(
                deadlines = listOf(),
                groups = listOf(),
                users = listOf(
                    UserInfo("0", "Valentin"),
                    UserInfo("1", "Léo"),
                    UserInfo("2", "Florian"),
                    UserInfo("3", "Lenny"),
                    UserInfo("4", "Louis"),
                    UserInfo("5", "Joseph"),
                )
            ).database

        @Singleton
        @Provides
        fun provideFirebaseAuth(): FirebaseAuth =
            MockFirebaseAuth().auth

        @Singleton
        @Provides
        fun provideFirebaseStorage(): FirebaseStorage =
            mock(FirebaseStorage::class.java)
    }
}
package com.github.multimatum_team.multimatum.viewmodelTest

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.FirebaseRepositoryModule
import com.github.multimatum_team.multimatum.model.AnonymousUser
import com.github.multimatum_team.multimatum.model.SignedInUser
import com.github.multimatum_team.multimatum.repository.*
import com.github.multimatum_team.multimatum.util.*
import com.github.multimatum_team.multimatum.viewmodel.AuthViewModel
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
class AuthViewModelTest {
    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var userRepository: UserRepository

    private lateinit var viewModel: AuthViewModel

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
        viewModel = AuthViewModel(authRepository, userRepository)
    }

    @Test
    fun `LiveData initially contains anonymous user`() {
        assertEquals(viewModel.getUser().value!!, AnonymousUser("0"))
    }

    @Test
    fun `LiveData is updated when the user signs in`() {
        (authRepository as MockAuthRepository).signIn("John Doe", "john.doe@example.com")
        assertEquals(viewModel.getUser().value!!,
            SignedInUser("0", "John Doe", "john.doe@example.com"))
    }

    @Test
    fun `Repository is updated when signing out from the viewModel`() {
        viewModel.signOut()
        assertEquals(viewModel.getUser().value!!, AnonymousUser("1"))
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object MockRepositoryModule {
        @Singleton
        @Provides
        fun provideDeadlineRepository(): DeadlineRepository =
            MockDeadlineRepository(listOf())

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
        fun providePdfRepository(): PdfRepository =
            MockPdfRepository()

        @Singleton
        @Provides
        fun provideUserRepository(): UserRepository =
            MockUserRepository(listOf())
    }
}
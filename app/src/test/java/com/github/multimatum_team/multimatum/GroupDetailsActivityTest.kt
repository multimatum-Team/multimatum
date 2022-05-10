package com.github.multimatum_team.multimatum

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.activity.DeadlineDetailsActivity
import com.github.multimatum_team.multimatum.activity.GroupDetailsActivity
import com.github.multimatum_team.multimatum.model.SignedInUser
import com.github.multimatum_team.multimatum.model.UserGroup
import com.github.multimatum_team.multimatum.model.UserInfo
import com.github.multimatum_team.multimatum.repository.AuthRepository
import com.github.multimatum_team.multimatum.repository.DeadlineRepository
import com.github.multimatum_team.multimatum.repository.GroupRepository
import com.github.multimatum_team.multimatum.repository.UserRepository
import com.github.multimatum_team.multimatum.service.ClockService
import com.github.multimatum_team.multimatum.util.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tests for the GroupDetailsActivity class
 */
@UninstallModules(FirebaseRepositoryModule::class, ClockModule::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class GroupDetailsActivityTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var groupRepository: GroupRepository

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var userRepository: UserRepository

    @Before
    @Throws(Exception::class)
    fun setUp() {
        Intents.init()
        hiltRule.inject()
        (authRepository as MockAuthRepository).logIn(
            SignedInUser(
                "Joseph",
                "Joseph",
                "unemail@jsp.com"
            )
        )
    }

    @After
    fun teardown() {
        Intents.release()
    }

    @Test
    fun `Given a group owned by the current user, all widget have the right properties`() {
        // UserGroup("0", "SDP", "Joseph", setOf("Joseph", "Louis", "Florian", "Léo", "Val"))
        val intent =
            GroupDetailsActivity.newIntent(ApplicationProvider.getApplicationContext(), "0")
        val scenario = ActivityScenario.launch<DeadlineDetailsActivity>(intent)
        scenario.use {
            onView(withId(R.id.group_details_name))
                .check(matches(withText("SDP")))
                .check(matches(isFocusable()))
            onView(withId(R.id.group_details_owner)).check(matches(withText("Owner: Joseph")))
            onView(withId(R.id.group_details_delete_button)).check(matches(isEnabled()))
        }
    }

    @Test
    fun `Given a group owned by the current user, they can rename the group`() = runTest {
        // UserGroup("0", "SDP", "Joseph", setOf("Joseph", "Louis", "Florian", "Léo", "Val"))
        val intent =
            GroupDetailsActivity.newIntent(ApplicationProvider.getApplicationContext(), "0")
        val scenario = ActivityScenario.launch<DeadlineDetailsActivity>(intent)
        scenario.use {
            onView(withId(R.id.group_details_name))
                .perform(ViewActions.replaceText("Multimatum"))
                .perform(ViewActions.pressImeActionButton())
            assertThat(
                "Group name has been updated",
                groupRepository.fetch("0")?.name,
                equalTo("Multimatum")
            )
        }
    }

    @Test
    fun `Given a group which the the current user does not own, all widget have the right properties`() {
        // UserGroup("1", "MIT", "Louis", setOf("Joseph", "Louis", "Florian", "Léo", "Val"))
        (authRepository as MockAuthRepository).logIn(
            SignedInUser(
                "Joseph",
                "Joseph",
                "unemail@jsp.com"
            )
        )
        val intent =
            GroupDetailsActivity.newIntent(ApplicationProvider.getApplicationContext(), "1")
        val scenario = ActivityScenario.launch<DeadlineDetailsActivity>(intent)
        scenario.use {
            onView(withId(R.id.group_details_name))
                .check(matches(withText("MIT")))
                .check(matches(isNotFocusable()))
            onView(withId(R.id.group_details_owner)).check(matches(withText("Owner: Louis")))
            onView(withId(R.id.group_details_delete_button)).check(matches(isNotEnabled()))
        }
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestClockModule {
        @Provides
        fun provideClockService(): ClockService =
            MockClockService(LocalDateTime.of(2022, 3, 12, 0, 0))
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
            MockGroupRepository(
                listOf(
                    UserGroup(
                        "0",
                        "SDP",
                        "Joseph",
                        setOf("Joseph", "Louis", "Florian", "Léo", "Val")
                    ),
                    UserGroup(
                        "1",
                        "MIT",
                        "Louis",
                        setOf("Joseph", "Louis", "Florian", "Léo", "Val")
                    ),
                    UserGroup(
                        "2",
                        "JDR",
                        "Florian",
                        setOf("Joseph", "Louis", "Florian", "Léo", "Val")
                    ),
                    UserGroup(
                        "3",
                        "Quantic",
                        "Léo",
                        setOf("Joseph", "Louis", "Florian", "Léo", "Val")
                    ),
                )
            )

        @Singleton
        @Provides
        fun provideAuthRepository(): AuthRepository =
            MockAuthRepository()

        @Singleton
        @Provides
        fun provideUserRepository(): UserRepository =
            MockUserRepository(
                listOf(
                    UserInfo(id = "Joseph", name = "Joseph"),
                    UserInfo(id = "Louis", name = "Louis"),
                    UserInfo(id = "Florian", name = "Florian"),
                    UserInfo(id = "Léo", name = "Léo"),
                    UserInfo(id = "Val", name = "Val"),
                )
            )
    }
}
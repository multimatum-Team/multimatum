package com.github.multimatum_team.multimatum

import android.app.AlertDialog
import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.content.ClipboardManager
import android.net.Uri
import androidx.core.content.ContextCompat.getSystemService
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.activity.DeadlineDetailsActivity
import com.github.multimatum_team.multimatum.activity.GroupDetailsActivity
import com.github.multimatum_team.multimatum.model.SignedInUser
import com.github.multimatum_team.multimatum.model.UserGroup
import com.github.multimatum_team.multimatum.model.UserInfo
import com.github.multimatum_team.multimatum.repository.*
import com.github.multimatum_team.multimatum.service.ClockService
import com.github.multimatum_team.multimatum.util.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.robolectric.shadows.ShadowAlertDialog
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tests for the GroupDetailsActivity class
 */
@UninstallModules(FirebaseRepositoryModule::class, ClockModule::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class GroupDetailsActivityTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val executorRule: TestRule = InstantTaskExecutorRule()

    @Inject
    lateinit var groupRepository: GroupRepository

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var userRepository: UserRepository

    companion object {
        private val joseph = SignedInUser(
            "Joseph",
            "Joseph",
            "unemail@jsp.com"
        )

        private val louis = SignedInUser(
            "Louis",
            "Louis",
            "louis@mit.edu"
        )
    }

    @Before
    @Throws(Exception::class)
    fun setUp() {
        Intents.init()
        hiltRule.inject()
    }

    @After
    fun teardown() {
        Intents.release()
    }

    @Test
    fun `Given a group owned by the current user, all widget have the right properties`() {
        // UserGroup("0", "SDP", "Joseph", setOf("Joseph", "Louis", "Florian", "Lenny", "Léo", "Val"))
        (authRepository as MockAuthRepository).logIn(joseph)
        groupRepository.setUserID(joseph.id)
        val intent =
            GroupDetailsActivity.newIntent(ApplicationProvider.getApplicationContext(), "0")
        val scenario = ActivityScenario.launch<DeadlineDetailsActivity>(intent)
        scenario.use {
            onView(withId(R.id.group_details_name))
                .check(matches(withText("SDP")))
                .check(matches(isFocusable()))
            onView(withId(R.id.group_details_owner)).check(matches(withText("Owner: Joseph")))
            onView(withId(R.id.group_details_delete_or_leave_button)).check(matches(withText("Delete")))
        }
    }

    @Test
    fun `Given a group owned by the current user, they can rename the group`() = runTest {
        // UserGroup("0", "SDP", "Joseph", setOf("Joseph", "Louis", "Florian", "Lenny", "Léo", "Val"))
        (authRepository as MockAuthRepository).logIn(joseph)
        groupRepository.setUserID(joseph.id)
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
    fun `Given a group owned by the current user, the leave button has the right behavior`() =
        runTest {
            // UserGroup("0", "SDP", "Joseph", setOf("Joseph", "Louis", "Florian", "Lenny", "Léo", "Val"))
            (authRepository as MockAuthRepository).logIn(joseph)
            groupRepository.setUserID(joseph.id)
            val intent =
                GroupDetailsActivity.newIntent(ApplicationProvider.getApplicationContext(), "0")
            val scenario = ActivityScenario.launch<DeadlineDetailsActivity>(intent)
            scenario.use {
                onView(withId(R.id.group_details_delete_or_leave_button))
                    .perform(click())
                ShadowAlertDialog.getShownDialogs()
                (ShadowAlertDialog.getLatestDialog() as AlertDialog)
                    .getButton(AlertDialog.BUTTON_NEGATIVE)
                    .performClick()

                onView(withId(R.id.group_details_delete_or_leave_button))
                    .perform(click())
                ShadowAlertDialog.getShownDialogs()
                (ShadowAlertDialog.getLatestDialog() as AlertDialog)
                    .getButton(AlertDialog.BUTTON_POSITIVE)
                    .performClick()
            }
        }

    @Test
    fun `Given a group which the the current user does not own, all widget have the right properties`() {
        // UserGroup("1", "MIT", "Louis", setOf("Joseph", "Louis", "Florian", "Lenny", "Léo", "Val"))
        (authRepository as MockAuthRepository).logIn(joseph)
        groupRepository.setUserID(joseph.id)
        val intent =
            GroupDetailsActivity.newIntent(ApplicationProvider.getApplicationContext(), "1")
        val scenario = ActivityScenario.launch<DeadlineDetailsActivity>(intent)
        scenario.use {
            onView(withId(R.id.group_details_name))
                .check(matches(withText("MIT")))
                .check(matches(isNotFocusable()))
            onView(withId(R.id.group_details_owner)).check(matches(withText("Owner: Louis")))
            onView(withId(R.id.group_details_delete_or_leave_button)).check(matches(withText("Leave")))
        }
    }

    @Test
    fun `Given a group which the the current user does not own, the leave button has the right behavior`() =
        runTest {
            // UserGroup("0", "SDP", "Joseph", setOf("Joseph", "Louis", "Florian", "Lenny", "Léo", "Val"))
            (authRepository as MockAuthRepository).logIn(joseph)
            groupRepository.setUserID(joseph.id)
            val intent =
                GroupDetailsActivity.newIntent(ApplicationProvider.getApplicationContext(), "1")
            val scenario = ActivityScenario.launch<GroupDetailsActivity>(intent)
            scenario.use {
                onView(withId(R.id.group_details_delete_or_leave_button))
                    .perform(click())
                ShadowAlertDialog.getShownDialogs()
                (ShadowAlertDialog.getLatestDialog() as AlertDialog)
                    .getButton(AlertDialog.BUTTON_NEGATIVE)
                    .performClick()

                onView(withId(R.id.group_details_delete_or_leave_button))
                    .perform(click())
                ShadowAlertDialog.getShownDialogs()
                (ShadowAlertDialog.getLatestDialog() as AlertDialog)
                    .getButton(AlertDialog.BUTTON_POSITIVE)
                    .performClick()
            }
        }

    @Test
    fun `Clicking invite button copies invite link to clipboard`() = runTest {
        (authRepository as MockAuthRepository).logIn(joseph)
        groupRepository.setUserID(joseph.id)
        val inviteLink = groupRepository.generateInviteLink("1")
        val intent =
            GroupDetailsActivity.newIntent(ApplicationProvider.getApplicationContext(), "1")
        val scenario = ActivityScenario.launch<GroupDetailsActivity>(intent)
        scenario.use {
            onView(withId(R.id.group_details_invite_button))
                .perform(click())
            val clipboard =
                getSystemService(
                    ApplicationProvider.getApplicationContext(),
                    ClipboardManager::class.java
                )!!
            assertThat(
                "Clipboard contains invite link",
                Uri.parse(clipboard.primaryClip?.getItemAt(0)?.text.toString()),
                equalTo(inviteLink)
            )
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
                        setOf("Joseph", "Louis", "Florian", "Lenny", "Léo", "Val")
                    ),
                    UserGroup(
                        "1",
                        "MIT",
                        "Louis",
                        setOf("Joseph", "Louis", "Florian", "Lenny", "Léo", "Val")
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
                    UserInfo(id = "Lenny", name = "Lenny"),
                    UserInfo(id = "Léo", name = "Léo"),
                    UserInfo(id = "Val", name = "Val"),
                )
            )

        @Singleton
        @Provides
        fun providePdfRepository(): PdfRepository =
            MockPdfRepository()
    }
}
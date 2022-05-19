package com.github.multimatum_team.multimatum

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.activity.GroupInviteActivity
import com.github.multimatum_team.multimatum.model.GroupID
import com.github.multimatum_team.multimatum.model.SignedInUser
import com.github.multimatum_team.multimatum.model.UserGroup
import com.github.multimatum_team.multimatum.model.UserInfo
import com.github.multimatum_team.multimatum.repository.*
import com.github.multimatum_team.multimatum.service.ClockService
import com.github.multimatum_team.multimatum.util.*
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.hasItem
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Tests for the GroupInviteActivity class
 */
@UninstallModules(
    FirebaseRepositoryModule::class,
    FirebaseDynamicLinksModule::class,
    ClockModule::class
)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class GroupInviteActivityTest {
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

    private lateinit var context: Context

    companion object {
        private val joseph = SignedInUser(
            "Joseph",
            "Joseph",
            "unemail@jsp.com"
        )
    }

    @Before
    @Throws(Exception::class)
    fun setUp() {
        Intents.init()
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext()
    }

    @After
    fun teardown() {
        Intents.release()
    }

    private suspend fun groupInviteIntent(groupID: GroupID): Intent {
        val intent =
            Intent(context, GroupInviteActivity::class.java)
        val group = groupRepository.fetch(groupID)
        val linkTitle = context.getString(R.string.group_invite_link_title, group.name)
        val linkDescription = context.getString(R.string.group_invite_link_description)
        val inviteLink = groupRepository.generateInviteLink(groupID, linkTitle, linkDescription)
        intent.action = Intent.ACTION_VIEW
        intent.data = inviteLink
        return intent
    }

    @Test
    fun `Clicking accept on an invite adds the current user to the group`() = runTest {
        // UserGroup("2", "JDR", "Florian", setOf("Louis", "Florian", "Léo", "Val"))
        (authRepository as MockAuthRepository).logIn(joseph)
        groupRepository.setUserID(joseph.id)
        val intent = groupInviteIntent("2")
        val scenario = ActivityScenario.launch<GroupInviteActivity>(intent)
        scenario.use {
            onView(withId(R.id.group_invite_accept))
                .perform(click())
            assertThat(
                "Group contains invited user",
                groupRepository.fetch("2").members,
                hasItem(joseph.id)
            )
        }
    }

    @Test
    fun `Clicking deny on an invite does not add the current user to the group`() = runTest {
        // UserGroup("2", "JDR", "Florian", setOf("Louis", "Florian", "Léo", "Val"))
        (authRepository as MockAuthRepository).logIn(joseph)
        groupRepository.setUserID(joseph.id)
        val intent = groupInviteIntent("2")
        val scenario = ActivityScenario.launch<GroupInviteActivity>(intent)
        scenario.use {
            onView(withId(R.id.group_invite_deny))
                .perform(click())
            assertThat(
                "Group contains invited user",
                groupRepository.fetch("2").members,
                not(hasItem(joseph.id))
            )
        }
    }

    @Test
    fun `Launching the activity for a group that the user has already joined shows the right message`() =
        runTest {
            // UserGroup("0", "SDP", "Joseph", setOf("Joseph", "Louis", "Florian", "Lenny", "Léo", "Val"))
            (authRepository as MockAuthRepository).logIn(joseph)
            groupRepository.setUserID(joseph.id)
            val intent = groupInviteIntent("0")
            val scenario = ActivityScenario.launch<GroupInviteActivity>(intent)
            scenario.use {
                val message =
                    context.getString(R.string.group_invite_message_already_member_of_this_group)
                onView(withId(R.id.group_invite_message))
                    .check(matches(withText(message)))
            }
        }

    @Test
    fun `Launching the activity for a group ID that does not exist shows the right message`() =
        runTest {
            (authRepository as MockAuthRepository).logIn(joseph)
            groupRepository.setUserID(joseph.id)
            val deepLink = Uri.Builder()
                .scheme("https")
                .authority("multimatum.page.link")
                .build()
            val inviteLink = Uri.Builder()
                .scheme("https")
                .authority("multimatum.page.link")
                .appendQueryParameter("sd", "Click this link to accept the invite")
                .appendQueryParameter("st", "Join non existing group")
                .appendQueryParameter("apn", "com.github.multimatum_team.multimatum")
                .appendQueryParameter("link", deepLink.toString())
                .build()
            val intent =
                Intent(context, GroupInviteActivity::class.java)
            intent.action = Intent.ACTION_VIEW
            intent.data = inviteLink
            val scenario = ActivityScenario.launch<GroupInviteActivity>(intent)
            scenario.use {
                val message = context.getString(R.string.group_invite_message_invalid)
                onView(withId(R.id.group_invite_message))
                    .check(matches(withText(message)))
            }
        }

    @Test
    fun `Launching the activity while being logged-in anonymously show the right message`() =
        runTest {
            val user = authRepository.signOut()
            groupRepository.setUserID(user.id)
            val intent = groupInviteIntent("0")
            val scenario = ActivityScenario.launch<GroupInviteActivity>(intent)
            scenario.use {
                val message = context.getString(R.string.group_invite_message_must_be_signed_in)
                onView(withId(R.id.group_invite_message))
                    .check(matches(withText(message)))
            }
        }

    @Test
    fun `Launching the activity without link data shows invalid link`() =
        runTest {
            val user = authRepository.signOut()
            groupRepository.setUserID(user.id)
            val intent =
                Intent(context, GroupInviteActivity::class.java)
            intent.action = Intent.ACTION_VIEW
            val scenario = ActivityScenario.launch<GroupInviteActivity>(intent)
            scenario.use {
                val message = context.getString(R.string.group_invite_message_must_be_signed_in)
                onView(withId(R.id.group_invite_message))
                    .check(matches(withText(message)))
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
    object TestDynamicLinksModule {
        @Singleton
        @Provides
        fun providesFirebaseDynamicLinks(): FirebaseDynamicLinks =
            mockFirebaseDynamicLinks()
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
                        setOf("Louis", "Florian", "Léo", "Val")
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
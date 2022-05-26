package com.github.multimatum_team.multimatum

import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.activity.GroupDetailsActivity
import com.github.multimatum_team.multimatum.activity.GroupsActivity
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
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@UninstallModules(FirebaseRepositoryModule::class, ClockModule::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class GroupsActivityTest {
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val activityRule = ActivityScenarioRule(GroupsActivity::class.java)

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
    fun setUp() {
        Intents.init()
        hiltRule.inject()
    }

    @After
    fun teardown() {
        Intents.release()
    }

    @Test
    fun `Clicking on a group starts GroupDetailsActivity`() {
        (authRepository as MockAuthRepository).logIn(joseph)
        onData(Matchers.anything()).inAdapterView(withId(R.id.listViewGroups))
            .atPosition(0).perform(click())

        Intents.intended(
            allOf(
                hasComponent(GroupDetailsActivity::class.java.name),
                IntentMatchers.hasExtra(
                    "com.github.multimatum_team.group.details.id",
                    "2"
                )
            )
        )
    }

    /*@Test
    fun followingCorrectStepToCreateGroupShouldLaunchAnIntentIntoGroupDetailsAndDisplayToast(){
        (authRepository as MockAuthRepository).logIn(joseph)
        onView(withId(R.id.addGroupButton)).perform(click())
        val dialog = ShadowAlertDialog.getLatestAlertDialog()
        assertTrue(dialog.isShowing)
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).callOnClick()
        val toast = ShadowToast.getLatestToast()
        sleep(1)

        MatcherAssert.assertThat(
            ShadowToast.getTextOfLatestToast(),
            CoreMatchers.equalTo("Group created")
        )
    }*/


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

        @Singleton
        @Provides
        fun providePdfRepository(): PdfRepository =
            MockPdfRepository()
    }
}
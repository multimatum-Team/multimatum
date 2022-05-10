package com.github.multimatum_team.multimatum

import android.app.Application
import android.graphics.Typeface
import android.view.View
import android.widget.ListView
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.adaptater.GroupMemberAdapter
import com.github.multimatum_team.multimatum.adaptater.UserGroupAdapter
import com.github.multimatum_team.multimatum.model.*
import com.github.multimatum_team.multimatum.repository.AuthRepository
import com.github.multimatum_team.multimatum.repository.DeadlineRepository
import com.github.multimatum_team.multimatum.repository.GroupRepository
import com.github.multimatum_team.multimatum.repository.UserRepository
import com.github.multimatum_team.multimatum.util.MockAuthRepository
import com.github.multimatum_team.multimatum.util.MockDeadlineRepository
import com.github.multimatum_team.multimatum.util.MockGroupRepository
import com.github.multimatum_team.multimatum.util.MockUserRepository
import com.github.multimatum_team.multimatum.viewmodel.GroupViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.junit.*
import org.junit.runner.RunWith
import javax.inject.Inject
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@UninstallModules(FirebaseRepositoryModule::class)
class GroupMemberAdapterTest {
    companion object {
        private val groups: List<UserGroup> = listOf(
            UserGroup("0", "SDP", "Joseph", setOf("Joseph", "Louis", "Florian", "Léo", "Val")),
            UserGroup("1", "MIT", "Louis", setOf("Joseph", "Louis", "Florian", "Léo", "Val")),
            UserGroup("2", "JDR", "Florian", setOf("Joseph", "Louis", "Florian", "Léo", "Val")),
            UserGroup("3", "Quantic", "Léo", setOf("Joseph", "Louis", "Florian", "Léo", "Val")),
        )
    }

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var groupRepository: GroupRepository

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var userRepository: UserRepository

    private lateinit var adapter: GroupMemberAdapter
    private lateinit var context: Application
    private lateinit var groupMap: Map<GroupID, UserGroup>

    @Before
    @Throws(Exception::class)
    fun setUp() {
        Intents.init()
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext()
        adapter = GroupMemberAdapter(context, userRepository)
        groupMap = groups.associateBy { it.id }
        adapter.setGroup(UserGroup("0", "SDP", "Joseph", setOf("Joseph", "Louis", "Florian", "Léo", "Val")))
        (authRepository as MockAuthRepository).logIn(
            SignedInUser(
                "Joseph",
                "Joseph",
                "unemail@jsp.com"
            )
        )
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun `Get count should give correct count`() {
        Assert.assertEquals(5, adapter.itemCount)
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
            MockGroupRepository(listOf(
                UserGroup("0", "SDP", "Joseph", setOf("Joseph", "Louis", "Florian", "Léo", "Val")),
                UserGroup("1", "MIT", "Louis", setOf("Joseph", "Louis", "Florian", "Léo", "Val")),
                UserGroup("2", "JDR", "Florian", setOf("Joseph", "Louis", "Florian", "Léo", "Val")),
                UserGroup("3", "Quantic", "Léo", setOf("Joseph", "Louis", "Florian", "Léo", "Val")),
            ))

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

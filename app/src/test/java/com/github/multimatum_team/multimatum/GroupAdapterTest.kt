package com.github.multimatum_team.multimatum

import android.app.Application
import android.graphics.Typeface
import android.view.View
import android.widget.ListView
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
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
class GroupAdapterTest {
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

    private lateinit var adapter: UserGroupAdapter
    private var context: Application? = null
    private lateinit var groupMap: Map<GroupID, UserGroup>

    @Before
    @Throws(Exception::class)
    fun setUp() {
        Intents.init()
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext()
        (authRepository as MockAuthRepository).logIn(AnonymousUser("0"))
        val viewModel = GroupViewModel(
            authRepository,
            groupRepository
        )

        adapter = UserGroupAdapter(context!!, viewModel, userRepository)
        groupMap = groups.associateBy { it.id }
        adapter.setGroups(groupMap)
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun `Get count should give correct count`() {
        (authRepository as MockAuthRepository).logIn(
            SignedInUser(
                "Val",
                "Val Dormeur",
                "val.dormeur@décédé.fr"
            )
        )
        Assert.assertEquals(4, adapter.count)
    }

    @Test
    fun `GetItem should give the correct group`() {
        Assert.assertEquals(adapter.getItem(0), groupMap.entries.toList()[0].value)
        Assert.assertEquals(adapter.getItem(1), groupMap.entries.toList()[1].value)
        Assert.assertEquals(adapter.getItem(2), groupMap.entries.toList()[2].value)
        Assert.assertEquals(adapter.getItem(3), groupMap.entries.toList()[3].value)
    }

    @Test
    fun `GetItemId should give the correct id`() {
        Assert.assertEquals(adapter.getItemId(0), 0.toLong())
        Assert.assertEquals(adapter.getItemId(1), 1.toLong())
        Assert.assertEquals(adapter.getItemId(2), 2.toLong())
        Assert.assertEquals(adapter.getItemId(3), 3.toLong())
    }

    @Test
    fun `GetView should display all field with correct font`() {
        (authRepository as MockAuthRepository).logIn(
            SignedInUser(
                "Val",
                "Val Dormeur",
                "val.dormeur@décédé.fr"
            )
        )
        val parent = ListView(context)
        val listItemView: View = adapter.getView(1, null, parent)
        //check title
        Assert.assertEquals(
            "MIT",
            listItemView.findViewById<TextView>(R.id.group_list_name).text
        )
        Assert.assertEquals(
            Typeface.BOLD,
            listItemView.findViewById<TextView>(R.id.group_list_name).typeface.style
        )
        //check subtitle
        Assert.assertEquals(
            "Owner: Louis",
            listItemView.findViewById<TextView>(R.id.group_list_owner).text
        )
        Assert.assertEquals(
            Typeface.ITALIC,
            listItemView.findViewById<TextView>(R.id.group_list_owner).typeface.style
        )
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
            MockGroupRepository(groups)

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

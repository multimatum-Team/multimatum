package com.github.multimatum_team.multimatum.adapterTest

import android.app.AlertDialog
import android.app.Application
import android.content.DialogInterface
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.FirebaseRepositoryModule
import com.github.multimatum_team.multimatum.adaptater.GroupMemberAdapter
import com.github.multimatum_team.multimatum.model.GroupID
import com.github.multimatum_team.multimatum.model.SignedInUser
import com.github.multimatum_team.multimatum.model.UserGroup
import com.github.multimatum_team.multimatum.model.UserInfo
import com.github.multimatum_team.multimatum.repository.*
import com.github.multimatum_team.multimatum.util.*
import com.github.multimatum_team.multimatum.viewmodel.AuthViewModel
import com.github.multimatum_team.multimatum.viewmodel.GroupViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.jvm.isAccessible

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@UninstallModules(FirebaseRepositoryModule::class)
class GroupMemberAdapterTest {
    companion object {
        private val groups: List<UserGroup> = listOf(
            UserGroup("0", "SDP", "Joseph", setOf("Joseph", "Louis", "Florian", "Lenny", "Léo", "Val")),
            UserGroup("1", "MIT", "Louis", setOf("Joseph", "Louis", "Florian", "Lenny", "Léo", "Val")),
            UserGroup("2", "JDR", "Florian", setOf("Joseph", "Louis", "Florian", "Lenny", "Léo", "Val")),
            UserGroup("3", "Quantic", "Léo", setOf("Joseph", "Louis", "Florian", "Lenny", "Léo", "Val")),
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

    lateinit var authViewModel: AuthViewModel

    lateinit var groupViewModel: GroupViewModel

    private lateinit var adapter: GroupMemberAdapter
    private lateinit var context: Application
    private lateinit var groupMap: Map<GroupID, UserGroup>

    @Before
    @Throws(Exception::class)
    fun setUp() {
        Intents.init()
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext()
        authViewModel = AuthViewModel(authRepository, userRepository)
        groupViewModel = GroupViewModel(context, authRepository, groupRepository)
        adapter = GroupMemberAdapter(context, userRepository, authViewModel, groupViewModel)
        groupMap = groups.associateBy { it.id }
        adapter.setGroup(
            UserGroup(
                "0",
                "SDP",
                "Joseph",
                setOf("Joseph", "Louis", "Florian", "Lenny", "Léo", "Val")
            )
        )
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
        assertEquals(6, adapter.itemCount)
    }

    @Test
    fun `showConfirmMemberRemovalDialog should configure the dialog correctly`(){

        // These values should be set inside the functions called on the mocks,
        // and then compared with the expected values
        val mockAlertDialogBuilder: AlertDialog.Builder = mock()
        var msg: String? = null
        var posButtonMsg: String? = null
        var posButtonListener: DialogInterface.OnClickListener? = null
        var negButtonMsg: String? = null
        var negButtonListener: DialogInterface.OnClickListener? = null
        var shown = false

        // Setup mocks
        whenever(mockAlertDialogBuilder.setMessage(anyString())).then {
            msg = it.getArgument(0)
            mockAlertDialogBuilder
        }
        whenever(mockAlertDialogBuilder.setPositiveButton(anyString(), any())).then {
            posButtonMsg = it.getArgument(0)
            posButtonListener = it.getArgument(1)
            mockAlertDialogBuilder
        }
        whenever(mockAlertDialogBuilder.setNegativeButton(anyString(), any())).then {
            negButtonMsg = it.getArgument(0)
            negButtonListener = it.getArgument(1)
            mockAlertDialogBuilder
        }
        whenever(mockAlertDialogBuilder.show()).then {
            shown = true
            mock<AlertDialog>()
        }

        val idOfUserAverell = "1234"
        val idOfUserJoe = "1235"
        val averellUserInfo = UserInfo(idOfUserAverell, "Averell Dalton")
        val mockUserRepository = MockUserRepository(listOf(
            averellUserInfo,
            UserInfo(idOfUserJoe, "Joe Dalton")
        ))
        val mockGroupViewModel: GroupViewModel = mock()
        val groupMemberAdapter = GroupMemberAdapter(context, mockUserRepository, mock(), mockGroupViewModel){ mockAlertDialogBuilder }
        val group = UserGroup("789", "MultimatumTeam", idOfUserAverell, setOf(idOfUserAverell, idOfUserJoe))
        groupMemberAdapter.setGroup(group)

        callShowConfirmMemberRemovalDialog(groupMemberAdapter, averellUserInfo)

        assertEquals("Are you sure you want to remove Averell Dalton from the group?\nThis action is irreversible", msg)
        assertEquals("Remove", posButtonMsg)
        assertEquals("Cancel", negButtonMsg)
        assertTrue(shown)
        assertNotNull(posButtonListener)
        assertNotNull(negButtonListener)
        checkPosButtonListener(mockGroupViewModel, posButtonListener, group, idOfUserAverell)

    }

    // calls showConfirmMemberRemovalDialog, even though it is private
    private fun callShowConfirmMemberRemovalDialog(
        groupMemberAdapter: GroupMemberAdapter,
        averellUserInfo: UserInfo
    ) {
        val funcUnderTest =
            GroupMemberAdapter::class.declaredFunctions.first { it.name == "showConfirmMemberRemovalDialog" }
        funcUnderTest.isAccessible = true
        funcUnderTest.call(groupMemberAdapter, averellUserInfo, 0)
        funcUnderTest.isAccessible = false
    }

    // performs assertions on the listener attached to the positive button of the
    // member removal confirmation dialog
    private fun checkPosButtonListener(
        mockGroupViewModel: GroupViewModel,
        posButtonListener: DialogInterface.OnClickListener?,
        group: UserGroup,
        @Suppress("SameParameterValue") idOfUserAverell: String
    ) {
        var removedGroupId: GroupID? = null
        var removedUserId: GroupID? = null
        whenever(mockGroupViewModel.removeMember(any(), any())).then {
            removedGroupId = it.getArgument(0)
            removedUserId = it.getArgument(1)
            null
        }
        posButtonListener!!.onClick(mock(), 0)
        assertEquals(group.id, removedGroupId)
        assertEquals(idOfUserAverell, removedUserId)
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

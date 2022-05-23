package com.github.multimatum_team.multimatum

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.View
import android.widget.EditText
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.activity.GroupsActivity
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.github.multimatum_team.multimatum.model.GroupID
import com.github.multimatum_team.multimatum.repository.*
import com.github.multimatum_team.multimatum.util.*
import com.github.multimatum_team.multimatum.viewmodel.GroupViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.shadows.ShadowToast
import java.time.LocalDateTime
import javax.inject.Singleton

@UninstallModules(GroupsActivityModule::class, FirebaseRepositoryModule::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class GroupsActivityTest2 {

    companion object {
        private val mockGroupViewModel: GroupViewModel = mock()
        private val mockAlertDialogBuilder: AlertDialog.Builder = mock()
    }

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        Intents.init()
        hiltRule.inject()
    }

    @After
    fun release() {
        Intents.release()
    }

    @Test
    fun `alert dialog is configured correctly and shown in addGroups`() {

        var title: String? = null
        var msg: String? = null
        var view: View? = null
        var posButtonText: String? = null
        var negButtonText: String? = null
        var posButtonOnClickListener: DialogInterface.OnClickListener? = null
        var negButtonOnClickListener: DialogInterface.OnClickListener? = null
        var shown = false

        whenever(mockGroupViewModel.getGroups()).thenReturn(mock())
        whenever(mockAlertDialogBuilder.setTitle(ArgumentMatchers.anyString())).then {
            title = it.getArgument(0)
            mockAlertDialogBuilder
        }
        whenever(mockAlertDialogBuilder.setMessage(ArgumentMatchers.anyString())).then {
            msg = it.getArgument(0)
            mockAlertDialogBuilder
        }
        whenever(mockAlertDialogBuilder.setView(any<View>())).then {
            view = it.getArgument(0)
            mockAlertDialogBuilder
        }
        whenever(
            mockAlertDialogBuilder.setPositiveButton(
                ArgumentMatchers.anyString(),
                any()
            )
        ).then {
            posButtonText = it.getArgument(0)
            posButtonOnClickListener = it.getArgument(1)
            mockAlertDialogBuilder
        }
        whenever(
            mockAlertDialogBuilder.setNegativeButton(
                ArgumentMatchers.anyString(),
                any()
            )
        ).then {
            negButtonText = it.getArgument(0)
            negButtonOnClickListener = it.getArgument(1)
            mockAlertDialogBuilder
        }
        whenever(mockAlertDialogBuilder.show()).then {
            shown = true
            null
        }

        val ctx: Context = ApplicationProvider.getApplicationContext()
        val intent = Intent(ctx, GroupsActivity::class.java)
        val scenario = ActivityScenario.launch<GroupsActivity>(intent)
        scenario.use { scen ->
            scen.onActivity { activity ->
                activity.addGroup(mock())
            }
        }

        Assert.assertEquals("Create group", title)
        Assert.assertEquals("Group name :", msg)
        Assert.assertNotNull(view)
        Assert.assertNotNull(view!!.layoutParams)
        Assert.assertEquals("Create", posButtonText)
        Assert.assertEquals("Cancel", negButtonText)
        Assert.assertTrue(shown)
        Assert.assertNotNull(posButtonOnClickListener)
        Assert.assertNotNull(negButtonOnClickListener)

        Assert.assertTrue(view is EditText)
        val editText = view as EditText

        checkPositiveButtonListener(editText, posButtonOnClickListener)
        checkNegativeButtonListener(negButtonOnClickListener)

    }

    private fun checkPositiveButtonListener(
        editText: EditText,
        posButtonOnClickListener: DialogInterface.OnClickListener?
    ) {
        val testText = "<demo text example>"
        editText.text.clear()
        editText.text.insert(0, testText)
        var readText: String? = null
        var callback: ((GroupID) -> Unit)? = null
        whenever(mockGroupViewModel.createGroup(ArgumentMatchers.anyString(), any())).then {
            readText = it.getArgument(0)
            callback = it.getArgument(1)
            null
        }
        posButtonOnClickListener!!.onClick(mock(), DialogInterface.BUTTON_POSITIVE)
        Assert.assertEquals(testText, readText)

        Assert.assertNotNull(callback)
        callback!!.invoke("42-24")
        assertLastToastWas("Group created")
    }

    private fun assertLastToastWas(expectedText: String) {
        MatcherAssert.assertThat(
            ShadowToast.getTextOfLatestToast(),
            Matchers.equalTo(expectedText)
        )
    }

    private fun checkNegativeButtonListener(negButtonOnClickListener: DialogInterface.OnClickListener?) {
        val mockDialog: Dialog = mock()
        var dismissed = false
        whenever(mockDialog.dismiss()).then {
            dismissed = true
            null
        }
        negButtonOnClickListener!!.onClick(mockDialog, 0)
        Assert.assertTrue(dismissed)
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestGroupsActivityModule {

        @Singleton
        @Provides
        fun provideAlertDialogBuilderProducer(): AlertDialogBuilderProducer =
            AlertDialogBuilderProducer { mockAlertDialogBuilder }

        @Singleton
        @Provides
        fun provideGroupViewModelProducer(): GroupViewModelProducer =
            GroupViewModelProducer { mockGroupViewModel }

    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestRepositoryModule {
        @Singleton
        @Provides
        fun provideDeadlineRepository(): DeadlineRepository =
            MockDeadlineRepository(
                listOf(
                    Deadline("Test 1", DeadlineState.TODO, LocalDateTime.of(2022, 3, 1, 0, 0)),
                    Deadline("Test 2", DeadlineState.DONE, LocalDateTime.of(2022, 3, 30, 0, 0)),
                    Deadline("Test 3", DeadlineState.TODO, LocalDateTime.of(2022, 3, 7, 0, 0))
                )
            )

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
        fun provideUserRepository(): UserRepository =
            MockUserRepository(listOf())

        @Singleton
        @Provides
        fun providePdfRepository(): PdfRepository =
            MockPdfRepository()
    }

}
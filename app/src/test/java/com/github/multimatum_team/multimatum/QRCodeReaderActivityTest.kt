package com.github.multimatum_team.multimatum

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.github.multimatum_team.multimatum.activity.QRCodeReaderActivity
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.github.multimatum_team.multimatum.repository.*
import com.github.multimatum_team.multimatum.util.*
import com.google.gson.JsonSyntaxException
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.shadows.ShadowToast
import java.lang.Exception
import java.time.LocalDateTime
import java.time.Month
import javax.inject.Singleton

// alias to distinguish it from Kotlin Result<T>
typealias LibResult = com.google.zxing.Result

// Robolectric suffix is used to distinguish this test case from the one in androidTests
@UninstallModules(CodeScannerModule::class, FirebaseRepositoryModule::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class QRCodeReaderActivityTestRobolectric {

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
    fun `basic config test`() {
        launchQRCodeReaderActivity()
        verify(mockCodeScanner).scanMode = ScanMode.SINGLE
        verify(mockCodeScanner).camera = CodeScanner.CAMERA_BACK
        verify(mockCodeScanner).isFlashEnabled = false
    }

    @Test
    fun `successful reading scenario`() {
        var decodeCallbackAsAny: Any? = null
        whenever(mockCodeScanner.setDecodeCallback(any())).then { invoc ->
            decodeCallbackAsAny = invoc.getArgument(0)
            null
        }
        launchQRCodeReaderActivity()
        assertNotNull(decodeCallbackAsAny)
        assertTrue(decodeCallbackAsAny is DecodeCallback)
        val decodeCallback = decodeCallbackAsAny as DecodeCallback
        val mockResult: LibResult = mock()

        whenever(mockJsonDeadlineConverter.fromJson(any())).thenReturn(fakeDeadline)
        whenever(mockResult.text).thenReturn("")
        decodeCallback.onDecoded(mockResult)
        assertLastToastWas("Deadline successfully added")
    }

    @Test
    fun `json error scenario`() {
        var decodeCallbackAsAny: Any? = null
        whenever(mockCodeScanner.setDecodeCallback(any())).then { invoc ->
            decodeCallbackAsAny = invoc.getArgument(0)
            null
        }
        launchQRCodeReaderActivity()
        assertNotNull(decodeCallbackAsAny)
        assertTrue(decodeCallbackAsAny is DecodeCallback)
        val decodeCallback = decodeCallbackAsAny as DecodeCallback
        val mockResult: LibResult = mock()
        whenever(mockJsonDeadlineConverter.fromJson(any())).then {
            throw JsonSyntaxException("test exception")
        }
        whenever(mockResult.text).thenReturn("")
        decodeCallback.onDecoded(mockResult)
        assertLastToastWas("provide a valid QRCode please")
    }

    @Test
    fun `error callback scenario`() {
        var errorCallbackAsAny: Any? = null
        whenever(mockCodeScanner.setErrorCallback(any())).then { invoc ->
            errorCallbackAsAny = invoc.getArgument(0)
            null
        }
        launchQRCodeReaderActivity()
        val exceptionMsg = "[text exception message]"
        assertNotNull(errorCallbackAsAny)
        assertTrue(errorCallbackAsAny is ErrorCallback)
        val errorCallback = errorCallbackAsAny as ErrorCallback
        errorCallback.onError(Exception(exceptionMsg))
        assertLastToastWas("Camera initialization error: $exceptionMsg")
    }

    @Test
    fun `onResume should call startPreview`() {
        var wasCalled = false
        whenever(mockCodeScanner.startPreview()).then {
            wasCalled = true
            null
        }
        launchQRCodeReaderActivity { invokeNonPublicMethodOnActivity("onResume", it) }
        assertTrue(wasCalled)
    }

    @Test
    fun `onPause should call releaseResources`() {
        var wasCalled = false
        whenever(mockCodeScanner.startPreview()).then {
            wasCalled = true
            null
        }
        launchQRCodeReaderActivity { invokeNonPublicMethodOnActivity("onPause", it) }
        assertTrue(wasCalled)
    }

    // invokes the method with the given name, on the given activity, regardless of its visibility
    private fun invokeNonPublicMethodOnActivity(
        methodName: String,
        activity: QRCodeReaderActivity,
    ) {
        val onResumeMethod =
            QRCodeReaderActivity::class.java.declaredMethods.find { it.name == methodName }!!
        onResumeMethod.isAccessible = true
        onResumeMethod.invoke(activity)
    }

    private fun assertLastToastWas(expectedText: String) {
        MatcherAssert.assertThat(
            ShadowToast.getTextOfLatestToast(),
            Matchers.equalTo(expectedText)
        )
    }

    // launches the activity and executes the specified action inside the `use` block
    private fun launchQRCodeReaderActivity(action: (QRCodeReaderActivity) -> Unit = {}) {
        val applicationContext = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(applicationContext, QRCodeReaderActivity::class.java)
        val activityScenario: ActivityScenario<QRCodeReaderActivity> =
            ActivityScenario.launch(intent)
        activityScenario.use { it.onActivity(action) }
    }

    companion object {
        private val mockCodeScanner: CodeScanner = mock()
        private val mockJsonDeadlineConverter: JsonDeadlineConverter = mock()
        private val fakeDeadline =
            Deadline(
                "test",
                DeadlineState.TODO,
                LocalDateTime.of(2020, Month.DECEMBER, 21, 0, 0)
            )
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestCodeScannerProvider {

        @Singleton
        @Provides
        fun provideTestCodeScanner(): CodeScannerProducer =
            CodeScannerProducer { _, _ -> mockCodeScanner }

        @Singleton
        @Provides
        fun provideTestJsonDeadlineConverter(): JsonDeadlineConverter =
            mockJsonDeadlineConverter

    }

    // Copied from MainSettingsActivityTest
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
package com.github.multimatum_team.multimatum

import android.Manifest
import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.hardware.SensorManager
import android.view.View
import android.widget.ListView
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.multimatum_team.multimatum.activity.*
import com.github.multimatum_team.multimatum.model.*
import com.github.multimatum_team.multimatum.repository.*
import com.github.multimatum_team.multimatum.util.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.anything
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.shadow.api.Shadow.extract
import org.robolectric.shadows.ShadowAlertDialog
import org.robolectric.shadows.ShadowApplication
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton


@UninstallModules(DependenciesProvider::class, FirebaseRepositoryModule::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class MainActivityTest {
    @Inject
    lateinit var authRepository: AuthRepository

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

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
    fun `clicking on the settings button should open the settings`() {
        onView(withId(R.id.main_open_settings_but)).perform(click())
        Intents.intended(
            allOf(
                hasComponent(MainSettingsActivity::class.java.name),
                toPackage("com.github.multimatum_team.multimatum")
            )
        )
    }

    @Test
    fun `long click on deadline listview should open DeadlineDetailsActivity`() {
        onData(anything()).inAdapterView(withId(R.id.deadlineListView)).atPosition(0)
            .perform(longClick())

        Intents.intended(
            allOf(
                hasComponent(DeadlineDetailsActivity::class.java.name),
                hasExtra(
                    "com.github.multimatum_team.deadline.details.id",
                    "0"
                )
            )
        )

    }

    @Test
    fun `clicking on the calendar button should open the calendar`() {
        onView(withId(R.id.goToCalendarButton)).perform(click())
        Intents.intended(
            allOf(
                hasComponent(CalendarActivity::class.java.name),
                toPackage("com.github.multimatum_team.multimatum")
            )
        )
    }

    @Test
    fun `clicking on add deadline button should add a deadline`() {
        onView(withId(R.id.main_go_to_add_deadline)).perform(click())
        Intents.intended(
            allOf(
                hasComponent(AddDeadlineActivity::class.java.name),
                toPackage("com.github.multimatum_team.multimatum")
            )
        )
    }

    @Test
    fun `clicking on the QR code reader button should open the QR code reader`() {
        grantPermission()
        onView(withId(R.id.goToQrCodeReader)).perform(click())
        Intents.intended(
            allOf(
                hasComponent(QRCodeReaderActivity::class.java.name),
                toPackage("com.github.multimatum_team.multimatum")
            )
        )
    }

    @Test
    fun `clicking on the group button should open GroupsActivity`() {
        (authRepository as MockAuthRepository).logIn(signedInUser)
        onView(withId(R.id.groupButton)).check(matches(isDisplayed())).perform(click())
        Intents.intended(
            allOf(
                hasComponent(GroupsActivity::class.java.name),
                toPackage("com.github.multimatum_team.multimatum")
            )
        )
    }

    @Test
    fun `Clicking the groups button while being signed-in anonymously shows an error message`() = runTest {
        (authRepository as MockAuthRepository).logIn(anonymousUser)
        onView(withId(R.id.groupButton)).perform(click())
        ShadowAlertDialog.getShownDialogs()
        (ShadowAlertDialog.getLatestDialog() as AlertDialog)
            .getButton(AlertDialog.BUTTON_NEGATIVE)
            .performClick()
    }

    @Test
    fun `Clicking the groups button while being signed-in anonymously and clicking the button to go to the sign-in page starts the right intent`() = runTest {
        (authRepository as MockAuthRepository).logIn(anonymousUser)
        onView(withId(R.id.groupButton)).perform(click())
        ShadowAlertDialog.getShownDialogs()
        (ShadowAlertDialog.getLatestDialog() as AlertDialog)
            .getButton(AlertDialog.BUTTON_POSITIVE)
            .performClick()
        Intents.intended(
            allOf(
                hasComponent(SignInActivity::class.java.name),
                toPackage("com.github.multimatum_team.multimatum")
            )
        )
    }

    @Test
    fun `button does not open QR code reader if permission not granted`() {
        onView(withId(R.id.goToQrCodeReader)).perform(click())
        denyPermission()
        onView(withId(R.id.goToQrCodeReader)).check(matches(isDisplayed()))
    }

    @Test
    fun `swipe deadline twice should delete it`() {
        onData(anything()).inAdapterView(withId(R.id.deadlineListView))
            .atPosition(0).perform(swipeRight())
        onData(anything()).inAdapterView(withId(R.id.deadlineListView))
            .atPosition(0).perform(swipeRight())
        onView(withId(R.id.deadlineListView)).check(matches(withListSize(2)))
    }

    @Test
    fun `swipe deadline once and click undo should undo`() {
        onData(anything()).inAdapterView(withId(R.id.deadlineListView)).atPosition(0)
            .perform(swipeLeft())
        onData(anything()).inAdapterView(withId(R.id.deadlineListView)).atPosition(0)
            .perform(click())
        onView(withId(R.id.deadlineListView)).check(matches(withListSize(3)))
    }

    /*
    ListView matcher for size found in:
   https://stackoverflow.com/questions/30361068/assert-proper-number-of-items-in-list-with-espresso
     */
    private fun withListSize(size: Int): Matcher<in View> {
        return object : TypeSafeMatcher<View?>() {
            override fun matchesSafely(view: View?): Boolean {
                return (view as ListView).count == size
            }

            override fun describeTo(description: Description) {
                description.appendText("ListView should have $size items")
            }

        }
    }

    /*
    The two following helper functions have been found online at:
    https://alexzh.com/ui-testing-of-android-runtime-permissions/
     */
    private fun grantPermission() {
        val application = InstrumentationRegistry.getInstrumentation().targetContext as Application
        val shadowApplication = extract<ShadowApplication>(application)
        shadowApplication.grantPermissions(
            Manifest.permission.CAMERA
        )

    }

    private fun denyPermission() {
        val application = InstrumentationRegistry.getInstrumentation().targetContext as Application
        val shadowApplication = extract<ShadowApplication>(application)
        shadowApplication.denyPermissions(
            Manifest.permission.CAMERA
        )
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestDependenciesProvider {
        @Provides
        fun provideSharedPreferences(): SharedPreferences =
            mockSharedPreferences

        @Provides
        fun provideSensorManager(@ApplicationContext applicationContext: Context): SensorManager =
            DependenciesProvider.provideSensorManager(applicationContext)
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object MockRepositoryModule {
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
            MockGroupRepository(
                listOf(
                    UserGroup("1", "SDP", "joseph"),
                    UserGroup("2", "JDR", "Florian"),
                    UserGroup("3", "Camera", "Leo")
                )
            )

        @Singleton
        @Provides
        fun providePdfRepository(): PdfRepository =
            MockPdfRepository()

        @Singleton
        @Provides
        fun provideAuthRepository(): AuthRepository =
            MockAuthRepository()

        @Singleton
        @Provides
        fun provideUserRepository(): UserRepository =
            MockUserRepository(
                listOf(
                    UserInfo(id = "joseph", name = "Joseph"),
                    UserInfo(id = "louis", name = "Louis"),
                    UserInfo(id = "florian", name = "Florian"),
                    UserInfo(id = "lenny", name = "Lenny"),
                    UserInfo(id = "leo", name = "Léo"),
                    UserInfo(id = "valentin", name = "Valentin"),
                )
            )
    }


    companion object {
        val mockSharedPreferences: SharedPreferences = mock()

        val signedInUser: SignedInUser = SignedInUser(
            id = "joseph",
            name = "Joseph",
            email = "fake.email@epfl.ch"
        )

        val anonymousUser: AnonymousUser = AnonymousUser("anonymous")
    }
}
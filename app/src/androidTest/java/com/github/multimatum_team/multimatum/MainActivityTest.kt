package com.github.multimatum_team.multimatum

import android.content.SharedPreferences
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.rule.GrantPermissionRule
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate


@UninstallModules(DependenciesProvider::class)
@HiltAndroidTest
class MainActivityTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule(order = 2)
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.CAMERA
    )

    @Before
    fun init() {
        hiltRule.inject()
        Intents.init()
    }

    @After
    fun release(){
        Intents.release()
    }

    @Test
    fun goToQRTest() {
        onView(withId(R.id.goToQR)).perform(ViewActions.click())
        Intents.intended(
            allOf(
                hasComponent(QRGenerator::class.java.name),
                toPackage("com.github.multimatum_team.multimatum")
            )
        )
    }

    @Test
    fun goToSetting() {
        onView(withId(R.id.main_open_settings_but)).perform(ViewActions.click())
        Intents.intended(
            allOf(
                hasComponent(MainSettingsActivity::class.java.name),
                toPackage("com.github.multimatum_team.multimatum")
            )
        )
    }

    @Test
    fun goToDeadlineDetails(){
        onData(anything()).inAdapterView(withId(R.id.deadlineListView)).atPosition(0).perform(longClick())

        Intents.intended(
            allOf(
                hasComponent(DeadlineDetailsActivity::class.java.name),
                hasExtra("com.github.multimatum_team.multimatum.deadline.details.title", "Test 1"),
                hasExtra("com.github.multimatum_team.multimatum.deadline.details.date", LocalDate.now().plusDays(7)),
                hasExtra("com.github.multimatum_team.multimatum.deadline.details.state", DeadlineState.TODO),
                toPackage("com.github.multimatum_team.multimatum")
            )
        )

    }

    @Test
    fun launchAccountActivityIntent(){
        onView(withId(R.id.logInButton)).perform(ViewActions.click())
        Intents.intended(toPackage("com.github.multimatum_team.multimatum"))
    }

    @Test
    fun buttonOpensQrCodeReader() {
        onView(withId(R.id.goToQrCodeReader)).perform(ViewActions.click())
        Intents.intended(
            allOf(
                hasComponent(QRCodeReaderActivity::class.java.name),
                toPackage("com.github.multimatum_team.multimatum")
            )
        )
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestDependenciesProvider {

        @Provides
        fun provideSharedPreferences(): SharedPreferences =
            MainSettingsActivityTest.mockSharedPreferences

        @Provides
        fun provideDemoList(): List<Deadline> =
            listOf(Deadline("Test 1", DeadlineState.TODO, LocalDate.now().plusDays(7)),
                Deadline("Test 2", DeadlineState.DONE, LocalDate.of(2022, 3,30)),
                Deadline("Test 3", DeadlineState.TODO, LocalDate.of(2022, 3,1)))

    }

}
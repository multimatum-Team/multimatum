package com.github.multimatum_team.multimatum

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.repository.DeadlineRepository
import com.github.multimatum_team.multimatum.util.MockDeadlineRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.shadows.ShadowToast
import javax.inject.Singleton


/**
 * Class test for AddDeadlineActivity
 */
@UninstallModules(RepositoryModule::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AddDeadlineTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val activityRule = ActivityScenarioRule(AddDeadlineActivity::class.java)

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun `The button should send a Toast if there is no title for the deadline`() {
        Espresso.onView(ViewMatchers.withId(R.id.add_deadline_select_title))
            .perform(ViewActions.replaceText(""))
        Espresso.closeSoftKeyboard()
        Espresso.onView(ViewMatchers.withId(R.id.add_deadline_button)).perform(ViewActions.click())
        MatcherAssert.assertThat(
            ShadowToast.getTextOfLatestToast(),
            CoreMatchers.equalTo("Enter a title")
        )
    }

    @Test
    fun `The button should add a deadline given it's title and its date`() {
        Espresso.onView(ViewMatchers.withId(R.id.add_deadline_select_title))
            .perform(ViewActions.replaceText("Test 1"))
        Espresso.closeSoftKeyboard()
        Espresso.onView(ViewMatchers.withId(R.id.add_deadline_date_picker))
            .perform(PickerActions.setDate(2022, 5, 8))
        Espresso.onView(ViewMatchers.withId(R.id.add_deadline_button)).perform(ViewActions.click())
        MatcherAssert.assertThat(
            ShadowToast.getTextOfLatestToast(),
            CoreMatchers.equalTo("Deadline created.")
        )
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestDeadlineRepositoryModule {
        @Singleton
        @Provides
        fun provideDeadlineRepository(): DeadlineRepository =
            MockDeadlineRepository(listOf())
    }


}
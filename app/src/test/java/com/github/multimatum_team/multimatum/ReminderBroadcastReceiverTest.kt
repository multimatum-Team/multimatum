package com.github.multimatum_team.multimatum

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.MainSettingsActivityTest.Companion.mockSharedPreferences
import com.github.multimatum_team.multimatum.activity.AddDeadlineActivity
import com.github.multimatum_team.multimatum.repository.AuthRepository
import com.github.multimatum_team.multimatum.repository.DeadlineRepository
import com.github.multimatum_team.multimatum.util.MockAuthRepository
import com.github.multimatum_team.multimatum.util.MockDeadlineRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowNotificationManager
import javax.inject.Inject
import javax.inject.Singleton

@UninstallModules(DependenciesProvider::class)
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class ReminderBroadcastReceiverTest {

    private lateinit var context: Context

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    @Throws(Exception::class)
    fun setUp() {
        Intents.init()
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext<Context>()
    }

    @After
    fun teardown() {
        Intents.release()
    }

    /**
     * Test if the broadCast receiver launch a notification when "onReceive" is called
     */
    @Test
    fun testNotificationLaunchOnReceive() {
        val notificationManager =
            ApplicationProvider.getApplicationContext<Context>()
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val shadowNotificationManager: ShadowNotificationManager = shadowOf(notificationManager)
        val reminderBroadcastReceiver = ReminderBroadcastReceiver()
        reminderBroadcastReceiver.onReceive(context, Intent())

        assertEquals(1, shadowNotificationManager.size())
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestDependenciesProvider {

        @Provides
        fun provideSharedPreferences(): SharedPreferences = mockSharedPreferences
    }
    companion object {
        val mockSharedPreferences: SharedPreferences = mock()
    }

}
package com.github.multimatum_team.multimatum

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.SensorManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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

        @Provides
        fun provideSensorManager(@ApplicationContext context: Context): SensorManager = DependenciesProvider.provideSensorManager(context)
    }

    companion object {
        private val mockSharedPreferences: SharedPreferences = mock()
    }

}
package com.github.multimatum_team.multimatum

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowNotificationManager

@RunWith(AndroidJUnit4::class)
class ReminderBroadcastReceiverTest {

    private lateinit var context: Context

    @Before
    @Throws(Exception::class)
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
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
        val reminderBroadcastReceiver: ReminderBroadcastReceiver = ReminderBroadcastReceiver()
        reminderBroadcastReceiver.onReceive(context, Intent())

        assertEquals(1, shadowNotificationManager.size())
    }

}
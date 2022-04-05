package com.github.multimatum_team.multimatum

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.github.multimatum_team.multimatum.service.ClockService
import com.github.multimatum_team.multimatum.util.MockClockService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowAlarmManager
import org.robolectric.shadows.ShadowNotificationManager
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@UninstallModules(ClockModule::class)
class DeadlineNotificationTest {

    private lateinit var context: Context
    private lateinit var deadlineNotification: DeadlineNotification
    private lateinit var shadowNotificationManager: ShadowNotificationManager


    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var clockService: ClockService


    @Before
    @Throws(Exception::class)
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        deadlineNotification = DeadlineNotification()
        hiltRule.inject()
        val notificationManager =
            ApplicationProvider.getApplicationContext<Context>()
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        shadowNotificationManager = Shadows.shadowOf(notificationManager)

    }

    /**
    * Test if createNotification channel creates a notification channel with the right parameters
    *  */
    @Test
    fun testCreateNotificationChannel() {
        val channel = NotificationChannel(
            "remindersChannel",
            "reminders channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "channel for reminders notifications"

        deadlineNotification.createNotificationChannel(context)

        Assert.assertEquals(channel, shadowNotificationManager.notificationChannels[0])

    }

    /**
     * Test if testSetNotification actually set an alarm.
    * */
    @Test
    fun testSetNotification() {
        val reminderBroadcastReceiver: ReminderBroadcastReceiver = ReminderBroadcastReceiver()
        reminderBroadcastReceiver.onReceive(context, Intent())

        Assert.assertEquals(1, shadowNotificationManager.size())
        val alarmManager = ApplicationProvider.getApplicationContext<Context>()
            .getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val shadowAlarmManager: ShadowAlarmManager = Shadows.shadowOf(alarmManager)
        val id = "someFirebaseID"
        val deadline = Deadline("notifDeadline", DeadlineState.TODO, clockService.now())
        val timeBeforeDue = Duration.of(5, ChronoUnit.HOURS).toMillis()

        deadlineNotification.setNotification(id, deadline, context, timeBeforeDue)

        val triggerAtTime = deadline.dateTime.atZone(ZoneId.systemDefault()).toInstant()
            .toEpochMilli() - timeBeforeDue
        Assert.assertEquals(
            triggerAtTime,
            shadowAlarmManager.peekNextScheduledAlarm().triggerAtTime
        )
    }

    /**
     * Test if a notification get properly cancel after a call to the method
     */
    @Test
    fun testCancelNotification() {
        val alarmManager = ApplicationProvider.getApplicationContext<Context>()
            .getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val shadowAlarmManager: ShadowAlarmManager = Shadows.shadowOf(alarmManager)

        val id = "someFirebaseID"
        val deadline = Deadline("notifDeadline", DeadlineState.TODO, clockService.now())
        val timeBeforeDue = Duration.of(5, ChronoUnit.HOURS).toMillis()

        deadlineNotification.setNotification(id, deadline, context, timeBeforeDue)

        val triggerAtTime = deadline.dateTime.atZone(ZoneId.systemDefault()).toInstant()
            .toEpochMilli() - timeBeforeDue
        Assert.assertEquals(
            triggerAtTime,
            shadowAlarmManager.peekNextScheduledAlarm().triggerAtTime
        )

        deadlineNotification.cancelNotification(id, deadline, context)

        Assert.assertEquals(0, shadowAlarmManager.scheduledAlarms.size)
    }

    /**
     * Inject a clockService to ensure test safety when using Java's 'LocalDateTime.now()'
     */
    @Module
    @InstallIn(SingletonComponent::class)
    object TestClockModule {
        @Provides
        fun provideClockService(): ClockService =
            MockClockService(LocalDateTime.of(2022, 3, 12, 0, 0))
    }
}
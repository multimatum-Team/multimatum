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
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.*
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowAlarmManager
import org.robolectric.shadows.ShadowNotificationManager
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class DeadlineNotificationTest {

    private lateinit var context: Context
    private lateinit var deadlineNotification: DeadlineNotification

    @Before
    @Throws(Exception::class)
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        deadlineNotification = DeadlineNotification()
    }

    /*
    * Test if createNotification channel creates a notification channel with the right parameters
    *  */
    @Test
    fun testCreateNotificationChannel(){
        val notificationManager =
            ApplicationProvider.getApplicationContext<Context>().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val shadowNotificationManager: ShadowNotificationManager =
            Shadows.shadowOf(notificationManager)
        val channel = NotificationChannel("remindersChannel", "reminders channel", NotificationManager.IMPORTANCE_DEFAULT)
        channel.description = "channel for reminders notifications"

        deadlineNotification.createNotificationChannel(context)

        Assert.assertEquals(channel, notificationManager.notificationChannels.get(0))

    }

    /*Test if testSetNotification actually set an alarm.
    * I abuse of mocking here, there's probably a better way to do it with shadow (because here for example "PendingIntent" cannot be mocked so we have to use Mockito.any())
    * */
    @Test
    fun testSetNotification(){
        val notificationManager =
            ApplicationProvider.getApplicationContext<Context>().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val shadowNotificationManager: ShadowNotificationManager =
            Shadows.shadowOf(notificationManager)
        val reminderBroadcastReceiver: ReminderBroadcastReceiver = ReminderBroadcastReceiver()
        reminderBroadcastReceiver.onReceive(context, Intent())

        Assert.assertEquals(1, shadowNotificationManager.size())


        val deadlineNotification = DeadlineNotification()
        val alarmManager = ApplicationProvider.getApplicationContext<Context>().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val shadowAlarmManager: ShadowAlarmManager = Shadows.shadowOf(alarmManager)
        val triggerAtTime = System.currentTimeMillis()
        deadlineNotification.setNotification(Deadline("notifDeadline", DeadlineState.TODO, LocalDate.now()), context, triggerAtTime)

        Assert.assertEquals(triggerAtTime, shadowAlarmManager.peekNextScheduledAlarm().triggerAtTime)
    }
}
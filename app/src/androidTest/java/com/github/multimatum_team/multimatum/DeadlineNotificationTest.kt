package com.github.multimatum_team.multimatum

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.*
import java.time.LocalDate


class DeadlineNotificationTest {

    private lateinit var mContext: Context

    @Before
    @Throws(Exception::class)
    fun setUp() {
        mContext = Mockito.mock(Context::class.java)
    }

    /* Test if createNotification channel creates a notification channel with the right parameters
    * */
    @Test
    fun testCreateNotificationChannel(){
        val deadlineNotification = DeadlineNotification()
        var mockedNotificationService = Mockito.mock(NotificationManager::class.java)
        val channel = NotificationChannel("remindersChannel", "reminders channel", NotificationManager.IMPORTANCE_DEFAULT)
        channel.description = "channel for reminders notifications"
        Mockito.`when`(mContext.getSystemService(Context.NOTIFICATION_SERVICE)).thenReturn(mockedNotificationService)

        doNothing().`when`(mockedNotificationService).createNotificationChannel(channel)

        deadlineNotification.createNotificationChannel(mContext)

        verify(mockedNotificationService,times(1)).createNotificationChannel(channel)

    }
/*
    /*Test if testSetNotification actually set an alarm.
    * I abuse of mocking here, there's probably a better way to do it with shadow (because here for example "PendingIntent" cannot be mocked so we have to use Mockito.any())
    * */
    @Test
    fun testSetNotification(){
        val deadlineNotification = DeadlineNotification()
        var mockedAlarmManager = Mockito.mock(AlarmManager::class.java)
        Mockito.`when`(mContext.getSystemService(Context.ALARM_SERVICE)).thenReturn(mockedAlarmManager)
        Mockito.`when`(PendingIntent.getBroadcast(Mockito.any(), Mockito.anyInt(), Mockito.any(), Mockito.anyInt())).thenReturn(Mockito.any())

        doNothing().`when`(mockedAlarmManager).setExact(eq(AlarmManager.RTC_WAKEUP), Mockito.anyLong(), Mockito.any())

        deadlineNotification.setNotification(Deadline("notifDeadline", DeadlineState.TODO, LocalDate.now()), mContext)

        verify(mockedAlarmManager, times(1)).setExact(eq(AlarmManager.RTC_WAKEUP), Mockito.anyLong(), Mockito.any())

    }*/
}
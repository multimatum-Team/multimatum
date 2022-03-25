package com.github.multimatum_team.multimatum

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.invocation.MockHandler
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class ReminderBroadcastReceiverTest {

    private lateinit var mReminderBroadcastReceiver: ReminderBroadcastReceiver
    private lateinit var mContext: Context
    private lateinit var mockedNotificationService: NotificationManager

    @Before
    @Throws(Exception::class)
    fun setUp() {
        mReminderBroadcastReceiver = ReminderBroadcastReceiver()
        mContext = mock(Context::class.java)
        mockedNotificationService = mock(NotificationManager::class.java)
    }

    @Test
    fun emptyTest(){
        assertEquals(1, 1)
    }

    /*
    /*
    * Test if the broadCast receiver launch a notification when "onReceive" is called
    * */
    @Test
    fun testNotificationLaunchOnReceive(){
        Mockito.`when`(mContext.getSystemService(Context.NOTIFICATION_SERVICE)).thenReturn(mockedNotificationService)

        Mockito.`when`(PendingIntent.getActivity(Mockito.any(), Mockito.anyInt(), Mockito.any(), Mockito.anyInt())).thenReturn(Mockito.any())

        doNothing().`when`(mockedNotificationService).notify(Mockito.anyInt(), Mockito.any())
        mReminderBroadcastReceiver.onReceive(mContext, Intent())

        verify(mockedNotificationService, times(1)).notify(Mockito.anyInt(), Mockito.any())
    }
     */

    /*
    @Test
    fun testBroadcastReceiverSetTheNotification() {
        val titleText = "foo"
        val descrText = "foo is foo"
        val id = 12
        val intent = Intent(mContext, ReminderBroadcastReceiver::class.java) //this create an intent of broadcast receiver
        //Adding extra parameter that will be used in the broadcast receiver to create the notification
        intent.putExtra("title", titleText)
        intent.putExtra("description", descrText)
        intent.putExtra("id", id)
        mReminderBroadcastReceiver!!.onReceive(mContext, intent)


        //here we're looking for the notificationManager that have been
        val notificationService: NotificationManager = mContext!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        Assert.assertEquals(1, notificationService.activeNotifications.size)

        val notification: Notification = notificationService.activeNotifications[0].notification

        val contentIntent: PendingIntent = notification.contentIntent

        Assert.assertEquals(contentIntent, MainActivity::class.java.name)
    }*/
}
package com.github.multimatum_team.multimatum

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock;

@RunWith(AndroidJUnit4::class)
class ReminderBroadcastReceiverTest {
    private var mReminderBroadcastReceiver: ReminderBroadcastReceiver? = null
    private var mContext: Context? = null


    @Before
    @Throws(Exception::class)
    fun setUp() {
        mReminderBroadcastReceiver = ReminderBroadcastReceiver()
        mContext = mock(Context::class.java)
    }

    @Test
    fun `test if Broadcast Receiver set the notification`(){
        val titleText = "foo"
        val descrText = "foo is foo"
        val id = 12
        val intent = Intent(mContext, ReminderBroadcastReceiver::class.java) //this create an intent of broadcast receiver
        //Adding extra parameter that will be used in the broadcase receiver to create the notification
        val pendingIntent = PendingIntent.getBroadcast(mContext, id, intent, PendingIntent.FLAG_IMMUTABLE)
        intent.putExtra("title", titleText)
        intent.putExtra("description", descrText)
        intent.putExtra("id", id)
        mReminderBroadcastReceiver!!.onReceive(mContext, intent)


        //here we're looking for the notificationManager that have been
        val notificationService: NotificationManager = mContext!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        Assert.assertEquals(1, notificationService.getActiveNotifications().size)

        val notification: Notification = notificationService.getActiveNotifications().get(0).notification

        val contentIntent: PendingIntent = notification.contentIntent

        Assert.assertEquals(contentIntent, MainActivity::class.java.getName())
    }
}
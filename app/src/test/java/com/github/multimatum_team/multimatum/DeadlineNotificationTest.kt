package com.github.multimatum_team.multimatum

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.model.DeadlineNotification
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowLooper
import java.sql.Time
import java.util.concurrent.TimeUnit


@RunWith(AndroidJUnit4::class)
class DeadlineNotificationTest {


    /*@Test
    fun `notification is triggered at currentTime and redirect to MainActivity`() {


        val activity: MainActivity = Robolectric.setupActivity(MainActivity::class.java) //main Activity
        DeadlineNotification.setNotification(System.currentTimeMillis(), "title", "empty description", 1, activity) //this create an alarm in 1000ms
        //here we're looking for the notificationManager that have been
        val notificationService: NotificationManager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val shadowNotificationManager =
            shadowOf(notificationService)

        assertEquals(1, shadowNotificationManager.size())

        val notification: Notification = shadowNotificationManager.allNotifications[0]

        val contentIntent: PendingIntent = notification.contentIntent

        val nextIntent = shadowOf(contentIntent).savedIntent

        val nextClassName = nextIntent.component!!.className
        assertEquals(nextClassName, MainActivity::class.java.getName())

    }*/
}
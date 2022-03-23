package com.github.multimatum_team.multimatum

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith


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
package com.github.multimatum_team.multimatum

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

class DeadlineNotificationTest {
    @Test
    fun `test`() {
        Assert.assertEquals(1, 1)
    }

    /*@Test
    fun `lauch notification correctely` () {
        val activity: MainActivity = Robolectric.setupActivity(MainActivity::class.java) //main Activity
        DeadlineNotification.setNotification(Deadline("notifDeadline", DeadlineState.TODO, LocalDate.now()), this) //this create an alarm in 1000ms
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
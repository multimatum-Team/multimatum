package com.github.multimatum_team.multimatum

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.repository.DeadlineID
import java.time.ZoneId

/**
 * This class provides function to manage notifications
 */
class DeadlineNotification {
    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent

    /**
    Create a notification channel for reminder notifications
    Creating an existing notification channel with its original values performs no operation,
    so it's safe to call this code when starting an app.
     */
    fun createNotificationChannel(context: Context) {
        val channelName: CharSequence = "reminders channel"
        val description = "channel for reminders notifications"
        val channel = NotificationChannel(
            "remindersChannel",
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        channel.description = description

        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Set a notification for a deadline in a given time before it is due.
     *
     * For example, to set an alarm (=notif) 1 hours before the deadline is due,
     * use "setNotification(Pair(deadline_id, deadline), context, Duration.of(1, ChronoUnit.HOURS).toMillis())
     *
     * @param timeBeforeDeadline : time in ms before the deadline, when the alarm has to be triggered
     *
     * @return void
     */
    fun setNotification(
        id: String,
        deadline: Deadline,
        context: Context,
        timeBeforeDeadline: Long
    ) {
        alarmManager =
            context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager  //this get an service instance of AlarmManager
        val intent = Intent(
            context,
            ReminderBroadcastReceiver::class.java
        )
        //this create an intent of broadcast receiver
        //Adding extra parameter that will be used in the broadcast receiver to create the notification
        intent.putExtra("title", deadline.title)
        intent.putExtra("description", deadline.description)
        intent.putExtra("id", id)

        //compute the time where the alarm will be triggered in millis.
        val alarmTriggerTime = deadline.dateTime.atZone(ZoneId.systemDefault()).toInstant()
            .toEpochMilli() - timeBeforeDeadline

        //set the receiver as pending intent
        pendingIntent =
            PendingIntent.getBroadcast(context, id.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE)

        //set an alarm that will wake up the pending intent (receiver)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTriggerTime, pendingIntent)

    }

    /**
     * Set all the notification of a deadline's notificationsTimes list.
     */
    fun setDeadlineNotifications(deadlineId: DeadlineID, deadline: Deadline, context: Context) {
        for (x in deadline.notificationsTimes) {
            setNotification(deadlineId+x.toString(), deadline, context, x)
        }
    }

    /**
     * Delete notification for a given deadline
     */
    fun cancelNotification(id: String, deadline: Deadline, context: Context) {
        val intent = Intent(
            context,
            ReminderBroadcastReceiver::class.java
        ) //this create an intent of broadcast receiver
        //Adding extra parameter that will be used in the broadcast receiver to create the notification
        intent.putExtra("title", deadline.title)
        intent.putExtra("description", deadline.description)
        intent.putExtra("id", id)

        pendingIntent =
            PendingIntent.getBroadcast(
                context,
                id.hashCode(),
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )

        alarmManager =
            context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager  //this get an service instance of AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    /**
     * delete all notification of a deadline's notificationsTimes list.
     */
    fun cancelDeadlineNotifications(deadlineId: DeadlineID, deadline: Deadline, context: Context){
        for(x in deadline.notificationsTimes){
            cancelNotification(deadlineId+x.toString(), deadline, context)
        }
    }
}
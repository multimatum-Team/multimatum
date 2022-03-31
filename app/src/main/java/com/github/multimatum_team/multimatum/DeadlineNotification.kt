package com.github.multimatum_team.multimatum

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.github.multimatum_team.multimatum.model.Deadline
import java.lang.NullPointerException

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
    fun createNotificationChannel(context: Context){
        val channelName :CharSequence = "reminders channel"
        val description = "channel for reminders notifications"
        val channel = NotificationChannel("remindersChannel", channelName, NotificationManager.IMPORTANCE_DEFAULT)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        channel.description=description

        notificationManager.createNotificationChannel(channel)
    }

    /**
    Set a notification that will be triggered in a given time in ms.
    you can pass a title/description and Id in parameter
    */
    fun setNotification(deadline: Deadline, context: Context, triggerTime: Long){
        alarmManager = context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager  //this get an service instance of AlarmManager
        val intent = Intent(context, ReminderBroadcastReceiver::class.java) //this create an intent of broadcast receiver
        //Adding extra parameter that will be used in the broadcase receiver to create the notification
        intent.putExtra("title", deadline.title)
        intent.putExtra("description", deadline.description)
        intent.putExtra("id", deadline.id)

        //set the receiver as pending intent
        pendingIntent = PendingIntent.getBroadcast(context, System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_IMMUTABLE)

        //set an alarm that will wake up the pending intent (receiver)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)

    }
}
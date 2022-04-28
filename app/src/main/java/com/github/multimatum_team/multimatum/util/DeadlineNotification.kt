package com.github.multimatum_team.multimatum.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.github.multimatum_team.multimatum.R
import com.github.multimatum_team.multimatum.ReminderBroadcastReceiver
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.repository.DeadlineID
import com.github.multimatum_team.multimatum.service.ClockService
import com.github.multimatum_team.multimatum.service.SystemClockService
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import java.time.ZoneId
import javax.inject.Inject


/**
 * This provides util function to manage notifications
 * It provide functions to access and modifiy notifications that are set for a deadline,
 * A function that verify that notification are bound to existing deadline,
 * and a function to create notification channel
 */
object DeadlineNotification {

    /**
     * return a list of all notifications set for this deadline
     */
    fun listDeadlineNotification(deadlineId: DeadlineID, context: Context): List<Long> {
        val res = readFromSharedPreference(deadlineId, context)
        //this is just to remove decimal to the number
        return res.map { it.toLong() }
    }

    /**
     * edit Notification for a given deadline
     */
    fun editNotification(
        deadlineId: DeadlineID,
        deadline: Deadline,
        newNotificationTime: List<Long>,
        context: Context
    ) {
        cancelDeadlineNotifications(deadlineId, context)
        writeToSharedPreference(deadlineId, newNotificationTime, context)
        setDeadlineNotifications(deadlineId, deadline, newNotificationTime, context)
        //logNotificationSP(context)
    }

    /**
     * delete all notification for a given deadline
     */
    fun deleteNotification(deadlineId: DeadlineID, context: Context) {
        cancelDeadlineNotifications(deadlineId, context)
        writeToSharedPreference(deadlineId, emptyList(), context)
        //logNotificationSP()
    }

    /**
     * Iterate through existing notification to ensure that there isn't any notification that aren't bound to a deadline.
     * If such a notification is detected, it'll delete it.
     */
    fun updateNotifications(deadlineList: Map<DeadlineID, Deadline>, context: Context) {
        val sp = context.getSharedPreferences(
            context.getString(R.string.notification_shared_preference),
            Context.MODE_PRIVATE
        )
        val notifications = sp.all.keys //key of deadline that have notification
        val existingDeadline = deadlineList.keys //keys of existing deadline
        val editor: SharedPreferences.Editor = sp.edit()
        for (k in notifications) {
            if (!existingDeadline.contains(k)) { //if deadlineId isn't in the list of deadline, then remove the notifications
                editor.remove(k)
                cancelDeadlineNotifications(k, context)
            }
        }
        editor.apply()
        //logNotificationSP()
    }

    /**
     * write an array of notification mapped to the daedline id to a sharedpreference file for storing notifications
     */
    private fun writeToSharedPreference(id: String, value: List<Long>, context: Context) {
        val jsonString = Gson().toJson(value)
        val sharedPref: SharedPreferences = context.getSharedPreferences(
            context.getString(R.string.notification_shared_preference),
            Context.MODE_PRIVATE
        )
        val editor = sharedPref.edit()
        editor.putString(id, jsonString)
        editor.apply()
    }

    /**
     * read the array of notification mapped to this daedline id from a sharedpreference file
     * return an empty list if there isn't any notification set for this deadline
     */
    private fun readFromSharedPreference(id: String, context: Context): List<Long> {
        val jsonString = context.getSharedPreferences(
            context.getString(R.string.notification_shared_preference),
            Context.MODE_PRIVATE
        ).getString(id, "0")
        if (jsonString == "0") return ArrayList<Long>()
        return Gson().fromJson(jsonString, ArrayList<Long>()::class.java)
    }

    /**
     * cancel all the notification for all deadlines
     */
    private fun cancelAllNotification(context: Context) {
        val sharedPref = context.getSharedPreferences(
            context.getString(R.string.notification_shared_preference),
            Context.MODE_PRIVATE
        )
        val sharedPreferencesMap: Map<String, *> = sharedPref.all
        for ((k, v) in sharedPreferencesMap) {
            if (v.toString() != "0") {
                cancelDeadlineNotifications(k, context)
                writeToSharedPreference(k, emptyList(), context)
            }
        }
    }

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
    private fun setNotification(
        id: String,
        deadline: Deadline,
        timeBeforeDeadline: Long,
        context: Context
    ) {
        val alarmManager =
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
        val alarmTriggerTimeMS: Long = deadline.dateTime.atZone(ZoneId.systemDefault()).toInstant()
            .toEpochMilli() - timeBeforeDeadline
        if (alarmTriggerTimeMS >= SystemClockService().now().atZone(ZoneId.systemDefault())
                .toInstant().toEpochMilli()
        ) { //set notification only if it is trigger in the future
            //set the receiver as pending intent
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                id.hashCode(),
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )

            //set an alarm that will wake up the pending intent (receiver)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTriggerTimeMS, pendingIntent)
        }
    }

    /**
     * Set all the notification of a deadline's notificationsTimes list.
     */
    private fun setDeadlineNotifications(
        deadlineId: DeadlineID,
        deadline: Deadline,
        notificationTimes: List<Long>,
        context: Context
    ) {
        for (notifTime in notificationTimes) {
            setNotification(deadlineId + notifTime.toString(), deadline, notifTime, context)
        }
    }

    /**
     * Delete a notification
     */
    private fun cancelNotification(notificationTag: String, context: Context) {
        val intent = Intent(
            context,
            ReminderBroadcastReceiver::class.java
        ) //this create an intent of broadcast receiver

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                notificationTag.hashCode(),
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )

        val alarmManager =
            context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager  //this get an service instance of AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    /**
     * delete all notification of a deadline's notificationsTimes list.
     */
    private fun cancelDeadlineNotifications(deadlineId: DeadlineID, context: Context) {
        val notificationTimes: List<Long> = readFromSharedPreference(deadlineId, context)
        for (notifTime in notificationTimes) {
            cancelNotification(deadlineId + notifTime.toString(), context)
        }
    }

    /**
     * tool function to log the current sharedPreference file of notification
     */
    private fun logNotificationSP(context: Context) {
        Log.i("Notification", "list of notification in sp")
        val notifications: Map<DeadlineID, *> = context.getSharedPreferences(
            context.getString(R.string.notification_shared_preference),
            Context.MODE_PRIVATE
        ).all
        for ((k, v) in notifications) {
            Log.i(k, v.toString())
        }
    }
}
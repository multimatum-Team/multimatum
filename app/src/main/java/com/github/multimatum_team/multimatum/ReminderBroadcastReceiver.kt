package com.github.multimatum_team.multimatum

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

//this class is a BroadcastReceiver that will send a notification when it is triggered
class ReminderBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        var channelId = "remindersChannel"

        //retrieving some parameters for the notification
        var title = intent!!.getStringExtra("title")
        var content = intent!!.getStringExtra("description")
        var notifId = intent!!.getIntExtra("id", 0)

        val intent2 = Intent(context, MainActivity::class.java)
        intent2!!.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        //this is the intent where the user will be send when clicking on the notification
        val pendingIntent = PendingIntent.getActivity(context, 0, intent2, PendingIntent.FLAG_IMMUTABLE)

        //Builder of the notification
        val notifBuilder = NotificationCompat.Builder(context!!, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title + notifId.toString())
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        //send the notification
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(notifId, notifBuilder.build())
    }
}
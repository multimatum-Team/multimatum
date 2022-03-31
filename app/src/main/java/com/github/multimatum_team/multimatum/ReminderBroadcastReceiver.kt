package com.github.multimatum_team.multimatum

import android.app.Activity
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.ListView
import androidx.activity.viewModels
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.github.multimatum_team.multimatum.model.DeadlineAdapter
import com.github.multimatum_team.multimatum.repository.DeadlineRepository
import com.github.multimatum_team.multimatum.viewmodel.DeadlineListViewModel
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

//this class is a BroadcastReceiver that will send a notification when it is triggered
class ReminderBroadcastReceiver : BroadcastReceiver() {
    @Inject
    lateinit var deadlineRepository: DeadlineRepository

    override fun onReceive(context: Context, intent: Intent?) {
        var channelId = "remindersChannel"

        //retrieving some parameters for the notification
        var title = intent!!.getStringExtra("title")
        var content = intent!!.getStringExtra("description")
        var notificationId = intent!!.getStringExtra("id")


        val intent2 = Intent(context, MainActivity::class.java)
        intent2!!.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        //this is the intent where the user will be send when clicking on the notification
        val pendingIntent = PendingIntent.getActivity(context, 0, intent2, PendingIntent.FLAG_IMMUTABLE)

        //Builder of the notification
        val notificationBuilder = NotificationCompat.Builder(context!!, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        //send the notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId.hashCode(), notificationBuilder.build())
    }
}
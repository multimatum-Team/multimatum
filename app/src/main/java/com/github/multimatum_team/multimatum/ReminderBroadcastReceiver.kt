package com.github.multimatum_team.multimatum

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.core.app.NotificationCompat
import com.github.multimatum_team.multimatum.activity.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


/**
 * This class is a BroadcastReceiver that will send a notification when it is triggered
 */
@AndroidEntryPoint
class ReminderBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var preferences: SharedPreferences

    companion object {
        private const val NOTIF_ENABLED_PREF_KEY =
            "com.github.multimatum_team.multimatum.activity.MainSettingsActivity.NotifEnabled"
    }

    /**
     * When an intent of this broadcast is started, it will create a notification with the right parameters and launch it
     */
    override fun onReceive(context: Context, intent: Intent) {
        if (preferences
                .getBoolean(NOTIF_ENABLED_PREF_KEY, true)
        ) {
            val channelId = "remindersChannel"

            //retrieving some parameters for the notification
            val title = intent!!.getStringExtra("title")
            val content = intent!!.getStringExtra("description")
            val notificationId = intent!!.getStringExtra("id")


            val intent2 = Intent(context, MainActivity::class.java)
            intent2.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            //this is the intent where the user will be send when clicking on the notification
            val pendingIntent =
                PendingIntent.getActivity(context, 0, intent2, PendingIntent.FLAG_IMMUTABLE)

            //Builder of the notification
            val notificationBuilder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            //send the notification
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(notificationId.hashCode(), notificationBuilder.build())
        }
    }

}
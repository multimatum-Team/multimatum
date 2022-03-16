package com.github.multimatum_team.multimatum

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ClipDescription
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

const val EXTRA_NAME = "com.github.multimatum_team.multimatum.main.name"

class MainActivity : AppCompatActivity() {
    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotificationChannel()

    }

    //Create a notification channel for reminder notifications
    //Creating an existing notification channel with its original values performs no operation,
    // so it's safe to call this code when starting an app.
    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channelName :CharSequence = "reminders channel"
            val description = "channel for reminders notifications"
            val channel = NotificationChannel("remindersChannel", channelName, NotificationManager.IMPORTANCE_DEFAULT)
            channel.description=description
            val notificationManager = getSystemService(NotificationManager::class.java)

            notificationManager.createNotificationChannel(channel)
        }
    }

    //Set a notification that will be triggered in a given time in ms.
    //you can pass a title/description and Id in parameter
    private fun setNotification(timeMS: Long, title: String, description: String, id: Int){
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderBroadcastReceiver::class.java)
        intent.putExtra("title", title)
        intent.putExtra("description", description)
        intent.putExtra("id", id)

        pendingIntent = PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_IMMUTABLE)

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeMS, pendingIntent)

    }

    //this button trigger a basics notification in 1 sec
    //here we use an id based on current time. We may use some parsed part of the corresponding deadline later.
    fun triggerNotification(view:View) {
        var id = System.currentTimeMillis().toInt()
        setNotification(System.currentTimeMillis()+1000, "asdf", "ouafouaf", id)
    }

    fun goQRGenerator(view:View){
        val intent = Intent(this, QRGenerator::class.java)
        startActivity(intent)
    }

    fun displayGreeting(view: View) {
        val mainNameField = findViewById<EditText>(R.id.mainName)
        val intent = Intent(this, GreetingActivity::class.java).apply {
            putExtra(EXTRA_NAME, mainNameField.text.toString())
        }
        startActivity(intent)
    }

    fun launchSettingsActivity(view: View) {
        val intent = Intent(this, MainSettingsActivity::class.java)
        startActivity(intent)
    }

}
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
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineAdapter
import com.github.multimatum_team.multimatum.model.DeadlineState
import java.time.LocalDate

class MainActivity : AppCompatActivity() {
    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        val listView = findViewById<ListView>(R.id.deadlineListView)

        //generate a list of deadline to demo. To remove later and link it to the real list
        val demoList = listOf(Deadline("Number 1",DeadlineState.TODO, LocalDate.now().plusDays(1)),
            Deadline("Number 2",DeadlineState.TODO, LocalDate.now().plusDays(7)),
            Deadline("Number 3",DeadlineState.DONE, LocalDate.of(2022, 3,30)),
            Deadline("Number 4",DeadlineState.TODO, LocalDate.of(2022, 3,1)))


        //put them on the listview.
        val adapter = DeadlineAdapter(this, demoList)
        listView.adapter = adapter

        createNotificationChannel()

    }

    /*
    Create a notification channel for reminder notifications
    Creating an existing notification channel with its original values performs no operation,
    so it's safe to call this code when starting an app.
    */
    private fun createNotificationChannel(){
        val channelName :CharSequence = "reminders channel"
        val description = "channel for reminders notifications"
        val channel = NotificationChannel("remindersChannel", channelName, NotificationManager.IMPORTANCE_DEFAULT)
        val notificationManager = getSystemService(NotificationManager::class.java)

        channel.description=description

        notificationManager.createNotificationChannel(channel)
    }

    /*
    Set a notification that will be triggered in a given time in ms.
    you can pass a title/description and Id in parameter
    */
    private fun setNotification(timeMS: Long, title: String, description: String, id: Int){
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager  //this get an service instance of AlarmManager
        val intent = Intent(this, ReminderBroadcastReceiver::class.java) //this create an intent of broadcast receiver
        //Adding extra parameter that will be used in the broadcase receiver to create the notification
        intent.putExtra("title", title)
        intent.putExtra("description", description)
        intent.putExtra("id", id)

        //set the receiver as pending intent
        pendingIntent = PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_IMMUTABLE)

        //set an alarm that will wake up the pending intent (receiver)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeMS, pendingIntent)

    }

    /*
    This button trigger a basics notification in 1 sec
    here we use an id based on current time. We may use some parsed part of the corresponding deadline later.
    */
    fun triggerNotification(view:View) {
        var id = System.currentTimeMillis().toInt()
        setNotification(System.currentTimeMillis()+4000, "asdf", "ouafouaf", id)
    }

    fun goQRGenerator(view:View){
        val intent = Intent(this, QRGenerator::class.java)
        startActivity(intent)
    }

    fun openCodeScanner(view: View) {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                123
            )
        } else {
            val intent = Intent(this, QRCodeReaderActivity::class.java)
            startActivity(intent)
        }
    }

    fun launchSettingsActivity(view: View) {
        val intent = Intent(this, MainSettingsActivity::class.java)
        startActivity(intent)
    }

}
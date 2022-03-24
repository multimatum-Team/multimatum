package com.github.multimatum_team.multimatum

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ListView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.multimatum_team.multimatum.model.DeadlineAdapter
import com.github.multimatum_team.multimatum.repository.FirebaseDeadlineRepository
import com.github.multimatum_team.multimatum.viewmodel.DeadlineListViewModel
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: DeadlineListViewModel
    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val listView = findViewById<ListView>(R.id.deadlineListView)

        FirebaseFirestore.getInstance().clearPersistence()

        val deadlineRepository = FirebaseDeadlineRepository()
        viewModel = DeadlineListViewModel(deadlineRepository)

        //put them on the listview.
        val adapter = DeadlineAdapter(this)
        listView.adapter = adapter
        viewModel.deadlines.observe(this) { deadlines ->
            Log.d("deadlines", deadlines.toString())
            adapter.submitList(deadlines)
        }

        createNotificationChannel()
    }

    /*
    Create a notification channel for reminder notifications
    Creating an existing notification channel with its original values performs no operation,
    so it's safe to call this code when starting an app.
    */
    private fun createNotificationChannel() {
        val channelName: CharSequence = "reminders channel"
        val description = "channel for reminders notifications"
        val channel = NotificationChannel(
            "remindersChannel",
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager = getSystemService(NotificationManager::class.java)

        channel.description = description

        notificationManager.createNotificationChannel(channel)
    }

    /*
    Set a notification that will be triggered in a given time in ms.
    you can pass a title/description and Id in parameter
    */
    private fun setNotification(timeMS: Long, title: String, description: String, id: Int) {
        alarmManager =
            getSystemService(ALARM_SERVICE) as AlarmManager  //this get an service instance of AlarmManager
        val intent = Intent(
            this,
            ReminderBroadcastReceiver::class.java
        ) //this create an intent of broadcast receiver
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
    fun triggerNotification(view: View) {
        var id = System.currentTimeMillis().toInt()
        setNotification(System.currentTimeMillis() + 4000, "asdf", "ouafouaf", id)
    }

    fun goQRGenerator(view: View) {
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

    fun goToLoginScreen(view: View) {
        val intent = Intent(this, AccountActivity::class.java)
        startActivity(intent)
    }

    fun openCalendar(view: View) {
        val intent = Intent(this, CalendarActivity::class.java)
        startActivity(intent)
    }

}
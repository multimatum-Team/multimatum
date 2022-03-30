package com.github.multimatum_team.multimatum

import android.annotation.SuppressLint
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
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.ListView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.multimatum_team.multimatum.model.DeadlineAdapter
import com.github.multimatum_team.multimatum.repository.DeadlineRepository
import com.github.multimatum_team.multimatum.viewmodel.DeadlineListViewModel
import com.hudomju.swipe.SwipeToDismissTouchListener
import com.hudomju.swipe.adapter.ListViewAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var deadlineRepository: DeadlineRepository

    private val viewModel: DeadlineListViewModel by viewModels()

    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val listView = findViewById<ListView>(R.id.deadlineListView)

        val adapter = DeadlineAdapter(this)

        listView.adapter = adapter
        viewModel.getDeadlines().observe(this) { deadlines ->
            Log.d("deadlines", deadlines.toString())
            adapter.setDeadlines(deadlines)
        }

        // Set when you maintain your finger on an item of the list, launch the detail activity
        listView.setOnItemLongClickListener { _, _, position, _ ->
            val (id, selectedDeadline) = adapter.getItem(position)
            val detailIntent = DeadlineDetailsActivity.newIntent(this, id, selectedDeadline)
            startActivity(detailIntent)
            // Last line necessary to use this function
            true
        }
        setDeleteOnSweep(listView, viewModel)
        createNotificationChannel()
    }

    // Set the ListView to delete an item by sweeping it
    // Based on the tutorial:
    // https://demonuts.com/android-listview-swipe-delete/
    @SuppressLint("ClickableViewAccessibility")
    private fun setDeleteOnSweep(lv : ListView, viewModel: DeadlineListViewModel){

        // Create a Listener who will delete the given deadline if swept
        val touchListener = SwipeToDismissTouchListener(
            ListViewAdapter(lv),
            object : SwipeToDismissTouchListener.DismissCallbacks<ListViewAdapter?> {
                override fun canDismiss(position: Int): Boolean {
                    return true
                }
                override fun onDismiss(view: ListViewAdapter?, position: Int) {
                        val adapter: DeadlineAdapter = lv.adapter as DeadlineAdapter
                        val (idToDelete, deadlineToDelete) = adapter.getItem(position)
                        viewModel.deleteDeadline(idToDelete)
                        adapter.setDeadlines(viewModel.getDeadlines().value!!)

                }
            })

        // Set it on the ListView
        lv.setOnTouchListener(touchListener)
        lv.setOnScrollListener(touchListener.makeScrollListener() as AbsListView.OnScrollListener)

        // If the Undo text is clicked, undo the deletion
        lv.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, _, _ ->
                if (touchListener.existPendingDismisses()) {
                    touchListener.undoPendingDismiss()
                }
            }
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

        //Adding extra parameter that will be used in the broadcast receiver to create the notification
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
        val id = System.currentTimeMillis().toInt()
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
package com.github.multimatum_team.multimatum


import android.content.Intent
import android.os.Build
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ListView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.google.firebase.FirebaseApp
import java.time.LocalDate

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var demoList: List<Deadline>

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val listView = findViewById<ListView>(R.id.deadlineListView)

        // Put them on the listview.
        val adapter = DeadlineAdapter(this, demoList)
        listView.adapter = adapter

        //create notification channel
        DeadlineNotification().createNotificationChannel(this)

        // Set when you maintain your finger on an item of the list, launch the detail activity
        listView.setOnItemLongClickListener { _, _, position, _ ->
            val selectedDeadline = demoList[position]
            val detailIntent = DeadlineDetailsActivity.newIntent(this, selectedDeadline)
            startActivity(detailIntent)
            // Last line necessary to use this function
            true
        }
    }

    /*
    This button trigger a basics notification in 1 sec
    here we use an id based on current time. We may use some parsed part of the corresponding deadline later.
    */
    fun triggerNotification(view:View) {
        DeadlineNotification().setNotification(Deadline("notifDeadline", DeadlineState.TODO, LocalDate.now()), this)
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

    fun openCalendar(view: View){
        val intent = Intent(this, CalendarActivity::class.java)
        startActivity(intent)
    }

}
package com.github.multimatum_team.multimatum.activity

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Spinner
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.multimatum_team.multimatum.R
import com.github.multimatum_team.multimatum.adaptater.DeadlineAdapter
import com.github.multimatum_team.multimatum.adaptater.DeadlineFilterAdapter
import com.github.multimatum_team.multimatum.adaptater.NoFilter
import com.github.multimatum_team.multimatum.repository.DeadlineRepository
import com.github.multimatum_team.multimatum.util.DeadlineNotification
import com.github.multimatum_team.multimatum.viewmodel.AuthViewModel
import com.github.multimatum_team.multimatum.viewmodel.DeadlineListViewModel
import com.github.multimatum_team.multimatum.viewmodel.GroupViewModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.hudomju.swipe.SwipeToDismissTouchListener
import com.hudomju.swipe.adapter.ListViewAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var deadlineRepository: DeadlineRepository

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private val deadlineListViewModel: DeadlineListViewModel by viewModels()
    private val groupViewModel: GroupViewModel by viewModels()
    private val userViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Firebase.initialize(this)
        setContentView(R.layout.activity_main)

        //access listview for deadline
        val listView = findViewById<ListView>(R.id.deadlineListView)
        val listViewAdapter =
            DeadlineAdapter(this, deadlineListViewModel)

        //set up observer on deadline list
        listView.adapter = listViewAdapter
        deadlineListViewModel.getDeadlines().observe(this) { deadlines ->
            listViewAdapter.setDeadlines(deadlines)
        }

        //access the spinner
        val filterSpinner = findViewById<Spinner>(R.id.filter)
        val filterAdapter = DeadlineFilterAdapter(this)
        filterSpinner.adapter = filterAdapter
        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) =
                listViewAdapter.setFilter(filterAdapter.getItem(position))

            override fun onNothingSelected(parent: AdapterView<*>?) {
                listViewAdapter.setFilter(NoFilter)
            }
        }

        groupViewModel.getGroups().observe(this) { groups ->
            filterAdapter.setGroups(groups.values.toList())
        }

        //create notification channel
        DeadlineNotification.createNotificationChannel(this)

        // Set when you maintain your finger on an item of the list, launch the detail activity
        listView.setOnItemLongClickListener { _, _, position, _ ->
            val (id, _) = listViewAdapter.getItem(position)
            val detailIntent = DeadlineDetailsActivity.newIntent(this, id)
            startActivity(detailIntent)
            // Last line necessary to use this function
            true
        }

        setDeleteOnSweep(listView, deadlineListViewModel)
        setCurrentTheme()
    }

    // Set the ListView to delete an item by sweeping it
    // Based on the tutorial:
    // https://demonuts.com/android-listview-swipe-delete/
    private fun setDeleteOnSweep(lv: ListView, viewModel: DeadlineListViewModel) {
        // Create a Listener who will delete the given deadline if swept
        val touchListener = SwipeToDismissTouchListener(
            ListViewAdapter(lv),
            object : SwipeToDismissTouchListener.DismissCallbacks<ListViewAdapter?> {
                override fun canDismiss(position: Int): Boolean {
                    return true
                }

                override fun onDismiss(view: ListViewAdapter?, position: Int) {
                    val adapter: DeadlineAdapter = lv.adapter as DeadlineAdapter
                    val (idToDelete, _) = adapter.getItem(position)
                    viewModel.deleteDeadline(idToDelete) {
                        DeadlineNotification.deleteNotification(it, this@MainActivity)
                    }
                    adapter.setDeadlines(viewModel.getDeadlines().value!!)
                }
            })
        // Set it on the ListView
        with(lv) { setOnTouchListener(touchListener) }

        // If the Undo text is clicked, undo the deletion
        lv.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, _, _ ->
                if (touchListener.existPendingDismisses()) {
                    touchListener.undoPendingDismiss()
                }
            }
    }

    /*
    Helper function to restore the last theme preference at startup
     */
    private fun setCurrentTheme() {
        val isNightMode =
            sharedPreferences.getBoolean(MainSettingsActivity.DARK_MODE_PREF_KEY, false)
        AppCompatDelegate.setDefaultNightMode(
            if (isNightMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }


    fun goToAddDeadline(view: View) {
        val intent = Intent(this, AddDeadlineActivity::class.java)
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
                CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
            val intent = Intent(this, QRCodeReaderActivity::class.java)
            startActivity(intent)
        }
    }

    /*
    Overriding this function allows the app to start the QR-Code scanner immediately
    if the camera permission is granted.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(this, QRCodeReaderActivity::class.java)
                startActivity(intent)
            }
        }
    }

    fun launchSettingsActivity(view: View) {
        val intent = Intent(this, MainSettingsActivity::class.java)
        startActivity(intent)
    }

    fun openCalendar(view: View) {
        val intent = Intent(this, CalendarActivity::class.java)
        startActivity(intent)
    }

    fun goToGroups(view: View) {
        val intent = Intent(this, GroupsActivity::class.java)
        startActivity(intent)
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 123
    }
}
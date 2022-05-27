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
import com.github.multimatum_team.multimatum.AlertDialogBuilderProducer
import com.github.multimatum_team.multimatum.R
import com.github.multimatum_team.multimatum.adaptater.DeadlineAdapter
import com.github.multimatum_team.multimatum.adaptater.DeadlineFilterAdapter
import com.github.multimatum_team.multimatum.adaptater.NoFilter
import com.github.multimatum_team.multimatum.model.AnonymousUser
import com.github.multimatum_team.multimatum.model.SignedInUser
import com.github.multimatum_team.multimatum.repository.DeadlineRepository
import com.github.multimatum_team.multimatum.repository.PdfRepository
import com.github.multimatum_team.multimatum.util.DeadlineNotification
import com.github.multimatum_team.multimatum.viewmodel.DeadlineListViewModel
import com.github.multimatum_team.multimatum.viewmodel.GroupViewModel
import com.google.firebase.firestore.ktx.firestore
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

    @Inject
    lateinit var pdfRepository: PdfRepository

    @Inject
    lateinit var alertDialogBuilderProducer: AlertDialogBuilderProducer

    private val deadlineListViewModel: DeadlineListViewModel by viewModels()
    private val groupViewModel: GroupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.initialize(this)

        setContentView(R.layout.activity_main)

        //access listview for deadline
        val listView = findViewById<ListView>(R.id.deadlineListView)
        val listViewAdapter = DeadlineAdapter(this, deadlineListViewModel)

        //set up observer on deadline list
        listView.adapter = listViewAdapter
        deadlineListViewModel.getDeadlines().observe(this, listViewAdapter::setDeadlines)

        //access the spinner
        val filterSpinner = findViewById<Spinner>(R.id.filter)
        val filterAdapter = DeadlineFilterAdapter(this)
        groupViewModel.getGroups().observe(this, filterAdapter::setGroups)
        filterSpinner.adapter = filterAdapter
        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) =
                listViewAdapter.setFilter(filterAdapter.getItem(position))

            override fun onNothingSelected(parent: AdapterView<*>?) =
                listViewAdapter.setFilter(NoFilter)
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
                    onDismissOverride(view, position, lv, viewModel)
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

    private fun onDismissOverride(
        view: ListViewAdapter?,
        position: Int,
        lv: ListView,
        viewModel: DeadlineListViewModel
    ) {
        val adapter: DeadlineAdapter = lv.adapter as DeadlineAdapter
        val (idToDelete, deadline) = adapter.getItem(position)
        if (deadline.pdfPath != "") {
            pdfRepository.delete(deadline.pdfPath)
        }
        viewModel.deleteDeadline(idToDelete) {
            DeadlineNotification.deleteNotification(it, this@MainActivity)
        }
        adapter.setDeadlines(viewModel.getDeadlines().value!!)
    }

    /**
     * Helper function to restore the last theme preference at startup
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
        when (authViewModel.getUser().value!!) {
            is SignedInUser -> {
                val intent = Intent(this, GroupsActivity::class.java)
                startActivity(intent)
            }
            is AnonymousUser ->
                alertDialogBuilderProducer
                    .produce(this)
                    .setTitle(getString(R.string.main_activity_go_to_groups_error_dialog_title))
                    .setMessage(getString(R.string.main_activity_go_to_groups_error_dialog_message))
                    .setPositiveButton(getString(R.string.main_activity_go_to_groups_error_dialog_go_to_sign_in_page)) { dialog, _ ->
                        val intent = Intent(this, SignInActivity::class.java)
                        startActivity(intent)
                        dialog.dismiss()
                    }
                    .setNegativeButton(R.string.main_activity_go_to_groups_error_dialog_cancel) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 123
    }
}

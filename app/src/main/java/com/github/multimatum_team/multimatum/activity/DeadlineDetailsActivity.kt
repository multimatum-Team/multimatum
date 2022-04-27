package com.github.multimatum_team.multimatum.activity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.multimatum_team.multimatum.R
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.github.multimatum_team.multimatum.repository.DeadlineID
import com.github.multimatum_team.multimatum.service.ClockService
import com.github.multimatum_team.multimatum.util.DeadlineNotification
import com.github.multimatum_team.multimatum.viewmodel.DeadlineListViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Classes used when you select a deadline in the list, displaying its details.
 * In the future, It should have a delete and modify button to change the deadline.
 */
@AndroidEntryPoint
class DeadlineDetailsActivity : AppCompatActivity() {

    @Inject
    lateinit var clockService: ClockService

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    lateinit var id: DeadlineID
    private var editMode: Boolean = true
    private val deadlineListViewModel: DeadlineListViewModel by viewModels()

    private lateinit var titleView: EditText
    private lateinit var dateView: TextView
    private lateinit var detailView: TextView
    private lateinit var doneButton: CheckBox

    // Set them on default value, waiting the fetch of the deadlines
    private var dateTime: LocalDateTime = LocalDateTime.of(2022, 10, 10, 10, 10)
    private var state: DeadlineState = DeadlineState.TODO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deadline_details)

        // Recuperate the necessary TextView
        titleView = findViewById(R.id.deadline_details_activity_title)
        dateView = findViewById(R.id.deadline_details_activity_date)
        detailView = findViewById(R.id.deadline_details_activity_done_or_due)
        doneButton = findViewById(R.id.deadline_details_activity_set_done)

        // Recuperate the id of the deadline
        id = intent.getStringExtra(EXTRA_ID) as DeadlineID

        // As the viewModel doesn't recuperate immediately the deadlines,
        // we need an update the moment they are fetched
        setDeadlineObserver()

        // Set the View to be unmodifiable at the start and remove displacement of the texts
        fixArrangement()

        // Setup the CheckBox to be checked if done
        doneButton.setOnCheckedChangeListener { _, isChecked ->
            state = if (isChecked) DeadlineState.DONE else DeadlineState.TODO
            updateDetail()
        }

        // Set the detail text to inform the user if it is due, done or the remaining time
        updateDetail()

    }

    // This display a DatePickerDialog and afterward a TimePickerDialog to modify the date
    fun changeDateAndTime(view: View) {
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                dateTime = LocalDateTime.of(
                    // monthOfYear is based on an enum that begin with 0
                    // thus we need to increase it by one to have the true date
                    year, monthOfYear + 1, dayOfMonth,
                    dateTime.hour, dateTime.minute
                )
                selectTime()
            }

        // Prepare the datePickerDialog
        val datePickerDialog = DatePickerDialog(
            this, dateSetListener,
            dateTime.year, dateTime.month.ordinal, dateTime.dayOfMonth
        )
        // Show the Dialog on the screen
        datePickerDialog.show()
    }

    // Function that put the activity to the Edit Mode or to the Uneditable Mode
    fun goToModifyOrBack(view: View) {
        editTitle(editMode)
        findViewById<ImageButton>(R.id.deadline_details_activity_modify)
            .setImageResource(
                if (editMode) android.R.drawable.checkbox_on_background
                else android.R.drawable.ic_menu_manage
            )

        dateView.setBackgroundResource(
            if (editMode) android.R.drawable.btn_default
            else android.R.color.transparent
        )

        adaptToCurrentTheme()

        dateView.isClickable = editMode

        doneButton.isClickable = editMode
        doneButton.visibility = if (editMode) View.VISIBLE else View.GONE

        // Modify the deadline in the database when you quit the edition mode
        if (!editMode) {
            val newDeadline = Deadline(titleView.text.toString(), state, dateTime)
            deadlineListViewModel.modifyDeadline(
                id,
                newDeadline
            )
            val deadlineNotification = DeadlineNotification(this)
            deadlineNotification.editNotification(id, newDeadline, deadlineNotification.listDeadlineNotification(id))
        }

        editMode = editMode.not()
    }

    // Shift the TitleView to a modify state or to a uneditable state
    private fun editTitle(edit: Boolean) {
        titleView.isEnabled = edit
        titleView.setBackgroundResource(
            if (edit) android.R.drawable.edit_text
            else android.R.color.transparent
        )
    }

    // When we go the first time to the Modify Mode, it happened that the TextView
    // shifted a little. This function do the shift at the very beginning of the
    // activity to fix it
    // TODO: Find a way to setup the layout to remove this problem
    private fun fixArrangement() {
        editTitle(true)
        editTitle(false)
        dateView.isClickable = false
        doneButton.isClickable = false
        dateView.setBackgroundResource(android.R.drawable.btn_default)
        dateView.setBackgroundResource(android.R.color.transparent)
    }

    // This function setup the observer to update the information shown when the
    // deadline are updated
    private fun setDeadlineObserver(){
        deadlineListViewModel.getDeadlines().observe(this) { deadlines ->
            // Recuperate the data from the deadline
            val deadline = deadlines[id]!!
            dateTime = deadline.dateTime
            state = deadline.state
            val title = deadline.title

            // Update the data shown
            dateView.text =
                getString(R.string.DueTheXatX, dateTime.toLocalDate(), dateTime.toLocalTime())
            titleView.text = SpannableStringBuilder(title)
            doneButton.isChecked = (state == DeadlineState.DONE)
            updateDetail()
        }

    }

    // Update the information shown in the TextView detailView
    private fun updateDetail() {
        val actualDate = clockService.now()
        when {
            state == DeadlineState.DONE -> {
                detailView.text = getString(R.string.done)
            }
            dateTime.isBefore(actualDate) -> {
                detailView.text = getString(R.string.isAlreadyDue)
            }
            else -> {
                val remainingTime = actualDate.until(dateTime, ChronoUnit.DAYS)
                detailView.text = if (remainingTime <= 0) {
                    getString(
                        R.string.DueInXHours,
                        actualDate.until(dateTime, ChronoUnit.HOURS).toString()
                    )
                } else {
                    getString(
                        R.string.DueInXDays,
                        actualDate.until(dateTime, ChronoUnit.DAYS).toString()
                    )
                }
            }
        }
    }

    // Setup a TimePickerDialog that will select a time for the deadline, show it and update the details
    private fun selectTime() {
        // Set what will happen when a time is selected
        val timeSetListener =
            TimePickerDialog.OnTimeSetListener { _, hoursOfDay, minutes ->
                dateTime = LocalDateTime.of(
                    dateTime.year, dateTime.monthValue, dateTime.dayOfMonth,
                    hoursOfDay, minutes
                )

                dateView.text =
                    getString(R.string.DueTheXatX, dateTime.toLocalDate(), dateTime.toLocalTime())

                updateDetail()
            }

        // Prepare the datePickerDialog
        val timePickerDialog = TimePickerDialog(
            this, timeSetListener,
            dateTime.hour, dateTime.minute, true
        )

        // Show the Dialog on the screen
        timePickerDialog.show()

    }

    // Change the color of the date and title views according to the theme
    private fun adaptToCurrentTheme() {
        val isNightMode =
            sharedPreferences.getBoolean(MainSettingsActivity.DARK_MODE_PREF_KEY, false)
        if (isNightMode) {
            dateView.setTextColor(
                if (editMode) Color.BLACK
                else Color.WHITE
            )
            titleView.setTextColor(
                if (editMode) Color.BLACK
                else Color.WHITE
            )
        }
    }

    /**
     * @param View: the current view
     * when clicking ont the button create an intent to launch the QR activity
     */
    fun goQRGenerator(view: View) {
        val intent = Intent(this, QRGeneratorActivity::class.java)
        intent.putExtra(EXTRA_ID, id)
        startActivity(intent)
    }

    companion object {
        private const val EXTRA_ID =
            "com.github.multimatum_team.deadline.details.id"

        // Launch an Intent to access this activity with a Deadline data
        fun newIntent(context: Context, id: DeadlineID): Intent {
            val detailIntent = Intent(context, DeadlineDetailsActivity::class.java)
            detailIntent.putExtra(EXTRA_ID, id)
            return detailIntent
        }

    }
}
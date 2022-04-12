package com.github.multimatum_team.multimatum

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.github.multimatum_team.multimatum.repository.DeadlineID
import com.github.multimatum_team.multimatum.service.ClockService
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

    lateinit var id: DeadlineID
    private var editMode: Boolean = true
    private val deadlineListViewModel: DeadlineListViewModel by viewModels()

    private lateinit var titleView: EditText
    private lateinit var dateView: TextView
    private lateinit var detailView: TextView
    private lateinit var doneButton: CheckBox

    private lateinit var date: LocalDateTime
    private lateinit var state: DeadlineState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deadline_details)

        // Recuperate the Deadline from the intent
        id = intent.getStringExtra(EXTRA_ID) as DeadlineID
        val deadline = deadlineListViewModel.getDeadlines().value!![id]!!
        date = deadline.dateTime
        state = deadline.state
        val title = deadline.title

        // Recuperate the necessary TextView
        titleView = findViewById(R.id.deadline_details_activity_title)
        dateView = findViewById(R.id.deadline_details_activity_date)
        detailView = findViewById(R.id.deadline_details_activity_done_or_due)
        doneButton = findViewById(R.id.deadline_details_activity_set_done)

        // Set the texts for the title and the date of the deadline
        titleView.text = SpannableStringBuilder(title)
        dateView.text = getString(R.string.DueTheXatX, date.toLocalDate(), date.toLocalTime())

        // Set the View to be unmodifiable at the start and remove displacement of the texts
        fixArrangement()

        // Setup the CheckBox to be checked if done
        doneButton.isChecked = (state == DeadlineState.DONE)
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
                date = LocalDateTime.of(
                    // monthOfYear is based on an enum that begin with 0
                    // thus we need to increase it by one to have the true date
                    year, monthOfYear + 1, dayOfMonth,
                    date.hour, date.minute
                )
                selectTime()
            }

        // Prepare the datePickerDialog
        val datePickerDialog = DatePickerDialog(
            this, dateSetListener,
            date.year, date.month.ordinal, date.dayOfMonth
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

        dateView.isClickable = editMode

        doneButton.isClickable = editMode
        doneButton.visibility = if (editMode) View.VISIBLE else View.GONE

        // Modify the deadline in the database when you quit the edition mode
        if (editMode) {
            deadlineListViewModel.modifyDeadline(
                id,
                Deadline(titleView.text.toString(), state, date)
            )
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
        titleView.setTextColor(Color.BLACK)


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

    // Update the information shown in the TextView detailView
    private fun updateDetail() {
        val actualDate = clockService.now()
        when {
            state == DeadlineState.DONE -> {
                detailView.text = getString(R.string.done)
            }
            date.isBefore(actualDate) -> {
                detailView.text = getString(R.string.isAlreadyDue)
            }
            else -> {
                val remainingTime = actualDate.until(date, ChronoUnit.DAYS)
                detailView.text = if (remainingTime <= 0) {
                    getString(
                        R.string.DueInXHours,
                        actualDate.until(date, ChronoUnit.HOURS).toString()
                    )
                } else {
                    getString(
                        R.string.DueInXDays,
                        actualDate.until(date, ChronoUnit.DAYS).toString()
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
                date = LocalDateTime.of(
                    date.year, date.monthValue, date.dayOfMonth,
                    hoursOfDay, minutes
                )

                dateView.text =
                    getString(R.string.DueTheXatX, date.toLocalDate(), date.toLocalTime())

                updateDetail()
            }

        // Prepare the datePickerDialog
        val timePickerDialog = TimePickerDialog(
            this, timeSetListener,
            date.hour, date.minute, true
        )

        // Show the Dialog on the screen
        timePickerDialog.show()

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
package com.github.multimatum_team.multimatum

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.github.multimatum_team.multimatum.repository.DeadlineID
import com.github.multimatum_team.multimatum.service.ClockService
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

    private var modifyMode: Boolean = true

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
        val title = intent.getStringExtra(EXTRA_TITLE)
        date = intent.getSerializableExtra(EXTRA_DATE) as LocalDateTime
        state = intent.getSerializableExtra(EXTRA_STATE) as DeadlineState

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

        // Setup the CheckBox to set done
        doneButton.isChecked = (state == DeadlineState.DONE)
        doneButton.setOnCheckedChangeListener { _, isChecked ->
            state = if (isChecked) DeadlineState.DONE else DeadlineState.TODO
            updateDetail()
        }

        // Set the detail text to inform the user if it is due, done or the remaining time
        updateDetail()

    }


    fun changeDateAndTime(view: View) {
        selectDate()

    }

    fun goToModifyOrBack(view: View) {
        if (modifyMode) {
            editTitle(true)
            findViewById<ImageButton>(R.id.deadline_details_activity_modify)
                .setImageResource(android.R.drawable.checkbox_on_background)

            dateView.setBackgroundResource(android.R.drawable.btn_default)
            dateView.isClickable = true

            doneButton.isClickable = true
            doneButton.visibility = View.VISIBLE

        } else {
            editTitle(false)
            findViewById<ImageButton>(R.id.deadline_details_activity_modify)
                .setImageResource(android.R.drawable.ic_menu_manage)

            dateView.isClickable = false
            dateView.setBackgroundResource(android.R.color.transparent)

            doneButton.isClickable = false
            doneButton.visibility = View.GONE
            //TODO: modify the deadline in the repository

        }
        modifyMode = modifyMode.not()

    }

    private fun editTitle(edit: Boolean) {
        if (edit) {
            titleView.isEnabled = true
            titleView.setBackgroundResource(android.R.drawable.edit_text)
            titleView.setTextColor(Color.BLACK)
        } else {
            titleView.isEnabled = false
            titleView.setBackgroundResource(android.R.color.transparent)
            titleView.setTextColor(Color.BLACK)
        }

    }

    // Remove the displacement of texts that can happen
    // and setup them for the creation of the Activity
    private fun fixArrangement() {
        editTitle(true)
        editTitle(false)
        dateView.isClickable = false
        dateView.setBackgroundResource(android.R.drawable.btn_default)
        dateView.setBackgroundResource(android.R.color.transparent)
    }

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
                detailView.text =
                    getString(
                        R.string.DueInXDays,
                        actualDate.until(date, ChronoUnit.DAYS).toString()
                    )
            }
        }
    }

    // Setup a DatePickerDialog that will select a date for the deadline and show it
    private fun selectDate() {
        // Set what will happen when a date is selected
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

    //Setup a TimePickerDialog that will select a time for the deadline, show it and update the details
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

    companion object {
        private const val EXTRA_ID =
            "com.github.multimatum_team.deadline.details.id"
        private const val EXTRA_TITLE =
            "com.github.multimatum_team.multimatum.deadline.details.title"
        private const val EXTRA_DATE = "com.github.multimatum_team.multimatum.deadline.details.date"
        private const val EXTRA_STATE =
            "com.github.multimatum_team.multimatum.deadline.details.state"

        // Launch an Intent to access this activity with a Deadline data
        fun newIntent(context: Context, id: DeadlineID, deadline: Deadline): Intent {
            val detailIntent = Intent(context, DeadlineDetailsActivity::class.java)

            detailIntent.putExtra(EXTRA_ID, id)
            detailIntent.putExtra(EXTRA_TITLE, deadline.title)
            detailIntent.putExtra(EXTRA_DATE, deadline.dateTime)
            detailIntent.putExtra(EXTRA_STATE, deadline.state)

            return detailIntent
        }
    }
}
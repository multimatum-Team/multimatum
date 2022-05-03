package com.github.multimatum_team.multimatum.activity

import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.multimatum_team.multimatum.R
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.github.multimatum_team.multimatum.service.ClockService
import com.github.multimatum_team.multimatum.util.DeadlineNotification
import com.github.multimatum_team.multimatum.util.PDFUtil
import com.github.multimatum_team.multimatum.viewmodel.DeadlineListViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 *  Activity who create a deadline using a DatePickerDialog and a TimePickerDialog
 */
@AndroidEntryPoint
class AddDeadlineActivity : AppCompatActivity() {
    @Inject
    lateinit var clockService: ClockService

    private lateinit var selectedDate: LocalDateTime


    private val deadlineListViewModel: DeadlineListViewModel by viewModels()
    private lateinit var textDate: TextView
    private lateinit var textTime: TextView
    private lateinit var pdfTextView: TextView
    private var pdfUri: Uri = Uri.EMPTY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_deadline)
        textDate = findViewById(R.id.add_deadline_text_date)
        textTime = findViewById(R.id.add_deadline_text_time)
        findViewById<CheckBox>(R.id.radio_notification_1h).text =
            getString(R.string.notify_before, "1 hour")
        findViewById<CheckBox>(R.id.radio_notification_5h).text =
            getString(R.string.notify_before, "5 hours")
        findViewById<CheckBox>(R.id.radio_notification_1d).text =
            getString(R.string.notify_before, "1 day")
        findViewById<CheckBox>(R.id.radio_notification_3d).text =
            getString(R.string.notify_before, "3 days")
        selectedDate = clockService.now().truncatedTo(ChronoUnit.MINUTES)
        textDate.text = selectedDate.toLocalDate().toString()
        textTime.text = selectedDate.toLocalTime().toString()

        pdfTextView = findViewById(R.id.selectedPdf)
        // Setting click listener to the PDF TextView
        pdfTextView.setOnClickListener {
            selectPDF()
        }
    }

    /**
     * Setup a DatePickerDialog that will select a date for the deadline and show it
     */
    fun selectDate(view: View) {
        // Set what will happen when a date is selected
        val dateSetListener =
            OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                selectedDate = LocalDateTime.of(
                    // monthOfYear is based on an enum that begin with 0
                    // thus we need to increase it by one to have the true date
                    year, monthOfYear + 1, dayOfMonth,
                    selectedDate.hour, selectedDate.minute
                )

                textDate.text = selectedDate.toLocalDate().toString()
            }

        // Prepare the datePickerDialog
        val datePickerDialog = DatePickerDialog(
            this, dateSetListener,
            selectedDate.year, selectedDate.month.ordinal, selectedDate.dayOfMonth
        )

        // Show the Dialog on the screen
        datePickerDialog.show()
    }

    /**
     * Setup a TimePickerDialog that will select a time for the deadline and show it
     */
    fun selectTime(view: View) {
        // Set what will happen when a time is selected
        val timeSetListener =
            TimePickerDialog.OnTimeSetListener { _, hoursOfDay, minutes ->
                selectedDate = LocalDateTime.of(
                    selectedDate.year, selectedDate.monthValue, selectedDate.dayOfMonth,
                    hoursOfDay, minutes
                )

                textTime.text = selectedDate.toLocalTime().toString()
            }

        // Prepare the datePickerDialog
        val timePickerDialog = TimePickerDialog(
            this, timeSetListener,
            selectedDate.hour, selectedDate.minute, true
        )

        // Show the Dialog on the screen
        timePickerDialog.show()

    }

    private val checkBoxIdTime = mapOf(
        R.id.radio_notification_1h to Duration.ofHours(1).toMillis(),
        R.id.radio_notification_5h to Duration.ofHours(5).toMillis(),
        R.id.radio_notification_1d to Duration.ofDays(1).toMillis(),
        R.id.radio_notification_3d to Duration.ofDays(3).toMillis()
    )

    private fun retrieveNotificationsTimes(): ArrayList<Long> {
        val res = ArrayList<Long>()
        for (checkBox in checkBoxIdTime) {
            if (findViewById<CheckBox>(checkBox.key).isChecked) res.add(checkBox.value)
        }
        return res
    }

    /**
     *  Add a deadline based on the data recuperated on the other TextViews
     */
    fun addDeadline(view: View) {
        // Getting the necessary views
        val editText = findViewById<TextView>(R.id.add_deadline_select_title)

        // Getting the entered text
        val titleDeadline = editText.text.toString()

        // Check if the title is not empty
        if (titleDeadline == "") {
            Toast.makeText(this, getString(R.string.enter_a_title), Toast.LENGTH_SHORT).show()
        } else {
            // Add the deadline
            val deadline = Deadline(
                titleDeadline,
                DeadlineState.TODO,
                selectedDate,
                pdfURI = pdfUri.toString()
            )

            val notificationsTimes = retrieveNotificationsTimes()



            Toast.makeText(this, getString(R.string.deadline_created), Toast.LENGTH_SHORT).show()

            // Reset the text input for future use
            editText.text = ""

            deadlineListViewModel.addDeadline(deadline) {
                DeadlineNotification.editNotification(it, deadline, notificationsTimes, this)
                finish()
            }
        }
    }

    private fun selectPDF() {
        PDFUtil.selectPdfIntent() {
            startForResult.launch(it)
        }
    }

    //handle result of startActivity
    val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result?.data!!
                pdfUri = intent.data!!
                val uriString: String = pdfUri.toString()
                var pdfName: String? = null
                if (uriString.startsWith("content://")) {
                    var myCursor: Cursor? = null
                    try {
                        // Setting the PDF to the TextView
                        myCursor =
                            applicationContext!!.contentResolver.query(
                                pdfUri,
                                null,
                                null,
                                null,
                                null
                            )
                        if (myCursor != null && myCursor.moveToFirst()) {
                            pdfName =
                                myCursor.getString(myCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                            pdfTextView.text = pdfName
                        }
                    } finally {
                        myCursor?.close()
                    }
                }
            }
        }


}
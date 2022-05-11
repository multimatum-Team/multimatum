package com.github.multimatum_team.multimatum.activity

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.multimatum_team.multimatum.R
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.github.multimatum_team.multimatum.model.datetime_parser.DateTimeExtractor
import com.github.multimatum_team.multimatum.service.ClockService
import com.github.multimatum_team.multimatum.util.DeadlineNotification
import com.github.multimatum_team.multimatum.util.PDFUtil
import com.github.multimatum_team.multimatum.viewmodel.DeadlineListViewModel
import com.mapbox.search.result.SearchResult
import com.mapbox.search.ui.view.SearchBottomSheetView
import dagger.hilt.android.AndroidEntryPoint
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
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

    private val dateTimeExtractor = DateTimeExtractor { clockService.now().toLocalDate() }
    private val deadlineListViewModel: DeadlineListViewModel by viewModels()
    private lateinit var textDate: TextView
    private lateinit var textTime: TextView
    private lateinit var editText: TextView
    private lateinit var pdfTextView: TextView
    private lateinit var textTitle: TextView
    private lateinit var textDescription: TextView

    // Memorisation of which checkBox is selected for the notifications
    private val notificationSelected = booleanArrayOf(false, false, false, false)
    private val nameCheckBox = arrayOf("1 hour", "5 hours", "1 day", "3 days")
    private val checkBoxIdTime = mapOf(
        "1 hour" to Duration.ofHours(1).toMillis(),
        "5 hours" to Duration.ofHours(5).toMillis(),
        "1 day" to Duration.ofDays(1).toMillis(),
        "3 days" to Duration.ofDays(3).toMillis()
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_deadline)
        textTitle = findViewById(R.id.add_deadline_select_title)
        textDate = findViewById(R.id.add_deadline_text_date)
        textTime = findViewById(R.id.add_deadline_text_time)
        pdfTextView = findViewById(R.id.selectedPdf)

        textDescription = findViewById(R.id.add_deadline_select_description)
        selectedDate = clockService.now().truncatedTo(ChronoUnit.MINUTES)
        updateDisplayedDateAndTime()
        KeyboardVisibilityEvent.setEventListener(this, object : KeyboardVisibilityEventListener {
            override fun onVisibilityChanged(isOpen: Boolean) {
                if (!isOpen) {
                    updateDisplayedInfoAfterTitleChange()
                }
            }
        })
        // Initialize the location search view
        initializeLocationSearchView(savedInstanceState)
    }

    /**
     * Analyses the title and tries to find date or time information,
     * then uses it to update the displayed fields
     */
    private fun updateDisplayedInfoAfterTitleChange() {
        val dateTimeExtractionResult = dateTimeExtractor.parse(textTitle.text.toString())
        dateTimeExtractionResult.date?.also { foundDate ->
            selectedDate = selectedDate
                .withYear(foundDate.year)
                .withDayOfYear(foundDate.dayOfYear)
        }
        dateTimeExtractionResult.time?.also { foundTime ->
            selectedDate = selectedDate
                .withHour(foundTime.hour)
                .withMinute(foundTime.minute)
                .withSecond(foundTime.second)
        }
        updateDisplayedDateAndTime()
        textTitle.text = dateTimeExtractionResult.text
    }

    private fun updateDisplayedDateAndTime() {
        textDate.text = selectedDate.toLocalDate().toString()
        textTime.text = selectedDate.toLocalTime().toString()
    }

    /**
     * Initialize the location search view with the chosen parameters
     */
    private fun initializeLocationSearchView(savedInstanceState: Bundle?) {
        val searchBottomSheetView = findViewById<SearchBottomSheetView>(R.id.search_view)
        val locationTextView = findViewById<TextView>(R.id.coordinates)

        searchBottomSheetView.initializeSearch(
            savedInstanceState,
            SearchBottomSheetView.Configuration()
        )
        // Hide the search bar at the beginning
        searchBottomSheetView.hide()

        // Add a listener for an eventual place selection
        searchBottomSheetView.addOnHistoryClickListener { result ->
            // We get only the name for now, the coordinates can also be extracted here.
            locationTextView.text = result.name
            searchBottomSheetView.hide()
        }
        // Add a listener for an eventual place selection in the history
        searchBottomSheetView.addOnSearchResultClickListener { result, _ ->
            locationTextView.text = result.name
            searchBottomSheetView.hide()
            updateLocationTextAndHideBar(locationTextView, searchBottomSheetView, result)
        }
        searchBottomSheetView.isHideableByDrag = true
    }

    /**
     * Helper function to update the location text view on
     * the "AddDeadlineActivity" main screen
     */
    private fun updateLocationTextAndHideBar(
        locationTextView: TextView,
        searchBottomSheetView: SearchBottomSheetView,
        result: SearchResult
    ) {
        locationTextView.text = result.name
        searchBottomSheetView.hide()
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

    /**
     * Setup an AlertDialog that will select when there will be notifications for the deadline
     */
    fun selectNotifications(view: View) {
        val alertDialogBuilder = AlertDialog.Builder(this)

        // Set the title
        alertDialogBuilder.setTitle("Notify Me:")

        // Set the checkbox, their name in the dialog and what happen when checked
        alertDialogBuilder.setMultiChoiceItems(
            nameCheckBox.map { s ->
                getString(R.string.notify_before, s)
            }.toTypedArray(),
            notificationSelected
        ) { _, which, isChecked ->
            notificationSelected[which] = isChecked
        }

        // Set the name of the done button
        alertDialogBuilder.setPositiveButton(getString(R.string.done), null)
        alertDialogBuilder.create()
        alertDialogBuilder.show()
    }

    /**
     *  Add a deadline based on the data recuperated on the other TextViews
     */
    fun addDeadline(view: View) {
        // Getting the entered text
        val titleDeadline = textTitle.text.toString()

        // Check if the title is not empty
        if (titleDeadline == "") {
            Toast.makeText(this, getString(R.string.enter_a_title), Toast.LENGTH_SHORT).show()
        } else {
            // Add the deadline
            val deadline = Deadline(
                titleDeadline,
                DeadlineState.TODO,
                selectedDate,
                textDescription.text.toString()
            )

            val notificationsTimes = retrieveNotificationsTimes()

            Toast.makeText(this, getString(R.string.deadline_created), Toast.LENGTH_SHORT).show()

            // Reset the text input for future use
            textTitle.text = ""

            deadlineListViewModel.addDeadline(deadline) {
                DeadlineNotification.editNotification(it, deadline, notificationsTimes, this)
                finish()
            }
        }
    }

    /**
     *  Go to SearchLocationActivity, which allows the user to select a location
     *  for a deadline.
     */
    fun searchLocation(view: View) {
        val searchBottomSheetView = findViewById<SearchBottomSheetView>(R.id.search_view)
        searchBottomSheetView.expand()
    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result?.data!!
                val pdfUri = intent.data!!
                val path: String = pdfUri.toString()
                val lastSlashIndex = path.lastIndexOf("/")
                pdfTextView.text = path.substring(lastSlashIndex + 1, path.length)
            }
        }

    fun selectPDF(view: View) {
        PDFUtil.selectPdfIntent() {
            startForResult.launch(it)
        }
    }

    // Recuperate the information on which notification must be set before returning it in an array
    private fun retrieveNotificationsTimes(): ArrayList<Long> {
        val res = ArrayList<Long>()
        for (i in 0 until checkBoxIdTime.count()) {
            if (notificationSelected[i]) res.add(checkBoxIdTime[nameCheckBox[i]]!!)
        }
        return res
    }
}
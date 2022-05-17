package com.github.multimatum_team.multimatum.activity

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.multimatum_team.multimatum.R
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.github.multimatum_team.multimatum.model.GroupID
import com.github.multimatum_team.multimatum.model.GroupOwned
import com.github.multimatum_team.multimatum.model.datetime_parser.DateTimeExtractionResult
import com.github.multimatum_team.multimatum.model.datetime_parser.DateTimeExtractor
import com.github.multimatum_team.multimatum.repository.FirebasePdfRepository
import com.github.multimatum_team.multimatum.repository.PdfRepository
import com.github.multimatum_team.multimatum.service.ClockService
import com.github.multimatum_team.multimatum.util.DeadlineNotification
import com.github.multimatum_team.multimatum.util.PDFUtil
import com.github.multimatum_team.multimatum.viewmodel.DeadlineListViewModel
import com.github.multimatum_team.multimatum.viewmodel.GroupViewModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.google.firebase.storage.FirebaseStorage
import com.hamsa.twosteppickerdialog.OnStepPickListener
import com.hamsa.twosteppickerdialog.TwoStepPickerDialog
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

    @Inject
    lateinit var firebasePdfRepository: PdfRepository

    private lateinit var selectedDate: LocalDateTime

    private val dateTimeExtractor = DateTimeExtractor { clockService.now().toLocalDate() }
    private val deadlineListViewModel: DeadlineListViewModel by viewModels()
    private val groupViewModel: GroupViewModel by viewModels()
    private lateinit var textDate: TextView
    private lateinit var textTime: TextView
    private lateinit var pdfTextView: TextView
    private lateinit var textTitle: TextView
    private lateinit var textDescription: TextView
    private lateinit var progressBar: ProgressBar

    private var pdfData = Uri.EMPTY

    // Memorisation of which checkBox is selected for the notifications
    private var notificationSelected = booleanArrayOf(false, false, false, false)
    private var nameNotifications = arrayOf("1 hour", "5 hours", "1 day", "3 days")
    private var timeNotifications = arrayOf(
        Duration.ofHours(1).toMillis(),
        Duration.ofHours(5).toMillis(),
        Duration.ofDays(1).toMillis(),
        Duration.ofDays(3).toMillis()
    )

    // Memorisation of which group is selected for the deadline
    private var groupSelected = 0
    private var nameGroups = arrayOf("No group")
    private var idGroups = arrayOf<GroupID>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Firebase.initialize(this)
        firebasePdfRepository = FirebasePdfRepository(FirebaseStorage.getInstance())

        setContentView(R.layout.activity_add_deadline)

        // Recuperate all the necessary Views
        textTitle = findViewById(R.id.add_deadline_select_title)
        textDate = findViewById(R.id.add_deadline_text_date)
        textTime = findViewById(R.id.add_deadline_text_time)
        pdfTextView = findViewById(R.id.selectedPdf)
        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.INVISIBLE
        progressBar.isIndeterminate = true
        textDescription = findViewById(R.id.add_deadline_select_description)

        selectedDate = clockService.now().truncatedTo(ChronoUnit.MINUTES)
        updateDisplayedDateAndTime()
        textTitle.setOnKeyListener { _, keyCode, keyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                updateDisplayedInfoAfterTitleChange()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

        // Initialize the location search view
        // TODO: Temporarily removed until the inflate exception thrown by the SearchView layout is solved
        //initializeLocationSearchView(savedInstanceState)

        setGroupObserver()

    }

    /**
     *  Update the groups owned by the user to be able to select them
     *  in the Dialog
     */
    private fun setGroupObserver() {
        groupViewModel.getGroups().observe(this) {
            nameGroups = arrayOf("No group")
            idGroups = arrayOf()
            groupViewModel.getOwnedGroups().map { (id, group) ->
                nameGroups = nameGroups.plus(group.name)
                idGroups = idGroups.plus(id)
            }
        }
    }

    /**
     * Analyses the title and tries to find date or time information,
     * then uses it to update the displayed fields
     */
    private fun updateDisplayedInfoAfterTitleChange() {
        val dateTimeExtractionResult = dateTimeExtractor.parse(textTitle.text.toString())
        if (dateTimeExtractionResult.date != null || dateTimeExtractionResult.time != null) {
            launchParsingValidationAlert(dateTimeExtractionResult)
        }
    }

    private fun applyParsing(dateTimeExtractionResult: DateTimeExtractionResult) {
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

    private fun launchParsingValidationAlert(res: DateTimeExtractionResult) {
        val alertBuilder = AlertDialog.Builder(this)
        var message = "title: " + res.text
        if (res.date != null) (message + "\ndate: " + res.date.toString()).also { message = it }
        if (res.time != null) (message + "\ntime: " + res.time.toString()).also { message = it }
        alertBuilder.setCancelable(true).setTitle(R.string.parsing_validation_title)
            .setMessage(message)
        alertBuilder.setNegativeButton(
            "Cancel"
        ) { dialogInterface, _ -> dialogInterface.cancel() }
        alertBuilder.setPositiveButton(
            "OK"
        ) { _, _ -> applyParsing(res) }
        alertBuilder.show()
    }

    private fun updateDisplayedDateAndTime() {
        textDate.text = selectedDate.toLocalDate().toString()
        textTime.text = selectedDate.toLocalTime().toString()
    }

    /**
     * Initialize the location search view with the chosen parameters
     */
    /*
    // TODO: Temporarily removed until the inflate exception thrown by the SearchView layout is solved
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
        searchBottomSheetView.addOnHistoryClickListener { history_record ->
            // We get only the name for now, the coordinates can also be extracted here.
            locationTextView.text = history_record.name
            searchBottomSheetView.hide()
        }
        // Add a listener for an eventual place selection in the history
        searchBottomSheetView.addOnSearchResultClickListener { result, _ ->
            locationTextView.text = result.name
            searchBottomSheetView.hide()
        }
        searchBottomSheetView.isHideableByDrag = true
        searchBottomSheetView.visibility = View.GONE
        searchBottomSheetView.isClickable = false
    }
    */

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

        alertDialogBuilder.setTitle(getString(R.string.notify_me))

        // Set the checkbox, their name in the dialog and what happen when checked
        alertDialogBuilder.setMultiChoiceItems(
            nameNotifications.map { s ->
                getString(R.string.notify_before, s)
            }.toTypedArray(),
            notificationSelected
        ) { _, which, isChecked ->
            notificationSelected[which] = isChecked
        }

        // Set the name of the done button
        alertDialogBuilder.setPositiveButton(getString(R.string.done), null)
        alertDialogBuilder.setNeutralButton("Add Custom Notification"){_, _ ->
            addNotifications(view)
        }
        alertDialogBuilder.create()
        alertDialogBuilder.show()
    }

    /**
     * Setup an AlertDialog that will select the group of the deadline
     */
    fun selectGroups(view: View) {
        val alertDialogBuilder = AlertDialog.Builder(this)

        alertDialogBuilder.setTitle("Select Group:")

        // Set the options, their names in the dialog and what happen when selected
        alertDialogBuilder.setSingleChoiceItems(
            nameGroups,
            groupSelected
        ) { _, which ->
            groupSelected = which
        }

        // Set the name of the done button
        alertDialogBuilder.setPositiveButton(getString(R.string.done), null)
        alertDialogBuilder.show()

    }

    /**
     * Setup an AlertDialog that will allow the user to add custom Dialog
     */
    private fun addNotifications(view: View) {
        val twoStepPickerDialog = TwoStepPickerDialog.Builder(this)
        twoStepPickerDialog.withBaseData(mutableListOf("hours before", "days before"))
            .withOkButton("Done").withCancelButton("Cancel")
            .withBaseOnLeft(false)
            .withInitialBaseSelected(0)
            .withInitialStepSelected(0)
            .withOnStepDataRequested { baseDataPos ->
                if (baseDataPos == 0){
                    (1 until 24).toList().map { i -> i.toString() }
                } else {
                    (1 until 30).toList().map { i -> i.toString() }
                }

            }.withDialogListener(object : OnStepPickListener {
                override fun onStepPicked(step: Int, pos: Int) {
                    if (step == 0){
                        nameNotifications = nameNotifications.plus( (pos+1).toString() + " hours")
                        timeNotifications = timeNotifications.plus(Duration.ofHours((pos+1).toLong()).toMillis())
                    } else {
                        nameNotifications = nameNotifications.plus( (pos+1).toString() + " days")
                        timeNotifications = timeNotifications.plus(Duration.ofDays((pos+1).toLong()).toMillis())
                    }
                    notificationSelected = notificationSelected.plus(true)

                    selectNotifications(view)
                }

                override fun onDismissed() {
                    selectNotifications(view)
                }
            }).build()
            .show()


    }

    /**
     *  Add a deadline based on the data recuperated on the other TextViews
     */
    fun addDeadline(view: View) {

        // Check if the title is not empty
        if (textTitle.text.toString() == "") {
            Toast.makeText(this, getString(R.string.enter_a_title), Toast.LENGTH_SHORT).show()
        } else {
            //loading bar
            progressBar.visibility = View.VISIBLE
            // Start upload
            firebasePdfRepository.uploadPdf(pdfData, this) { ref ->
                // Hide loading bar
                progressBar.visibility = View.GONE

                // Create the deadline
                val deadline = recuperateDeadlineFromInputText(ref)
                // Get notification setting
                val notificationsTimes = retrieveNotificationsTimes()

                // Send toast
                Toast.makeText(this, getString(R.string.deadline_created), Toast.LENGTH_SHORT)
                    .show()

                // Reset the text input for future use
                textTitle.text = ""

                deadlineListViewModel.addDeadline(deadline) {
                    DeadlineNotification.editNotification(it, deadline, notificationsTimes, this)
                    finish()
                }
            }
        }
    }

    /**
     *  Go to SearchLocationActivity, which allows the user to select a location
     *  for a deadline.
     */
    fun searchLocation(view: View) {
        /*
        // TODO: Temporarily removed until the inflate exception thrown by the SearchView layout is solved
        val searchBottomSheetView = findViewById<SearchBottomSheetView>(R.id.search_view)
        searchBottomSheetView.visibility = View.VISIBLE
        searchBottomSheetView.isClickable = true
        searchBottomSheetView.expand()
        */
    }

    /**
     * Recuperate the deadline from all the text input in the activity
     * WARNING: the textTile must not be empty
     */
    private fun recuperateDeadlineFromInputText(ref: String): Deadline {
        // Putting the deadline in the corresponding group or
        // in none if no group selected
        if (groupSelected == 0) {
            return Deadline(
                textTitle.text.toString(),
                DeadlineState.TODO,
                selectedDate,
                textDescription.text.toString(),
                pdfPath = ref
            )
        } else {
            return Deadline(
                textTitle.text.toString(),
                DeadlineState.TODO,
                selectedDate,
                textDescription.text.toString(),
                GroupOwned(idGroups[groupSelected - 1]),
                pdfPath = ref
            )
        }
    }

    // Code to be executed when a pdf has been chosen
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                pdfData = result.data!!.data!!
                pdfTextView.text = PDFUtil.getFileNameFromUri(pdfData)
            }
        }


    /**
     * Launch pdf selection menu
     */
    fun selectPDF(view: View) {
        PDFUtil.selectPdfIntent {
            startForResult.launch(it)
        }
    }

    // Recuperate the information on which notification must be set before returning it in an array
    private fun retrieveNotificationsTimes(): ArrayList<Long> {
        val res = ArrayList<Long>()
        for (i in 0 until timeNotifications.count()) {
            if (notificationSelected[i]) res.add(timeNotifications[i])
        }
        return res
    }
}
package com.github.multimatum_team.multimatum.activity

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.multimatum_team.multimatum.LogUtil
import com.github.multimatum_team.multimatum.R
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.github.multimatum_team.multimatum.model.datetime_parser.DateTimeExtractionResult
import com.github.multimatum_team.multimatum.model.datetime_parser.DateTimeExtractor
import com.github.multimatum_team.multimatum.service.ClockService
import com.github.multimatum_team.multimatum.util.DeadlineNotification
import com.github.multimatum_team.multimatum.util.PDFUtil
import com.github.multimatum_team.multimatum.viewmodel.DeadlineListViewModel
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
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

    private val dateTimeExtractor = DateTimeExtractor { clockService.now().toLocalDate() }
    private val deadlineListViewModel: DeadlineListViewModel by viewModels()
    private lateinit var storageRef: StorageReference
    private lateinit var textDate: TextView
    private lateinit var textTime: TextView
    private lateinit var pdfTextView: TextView
    private lateinit var textTitle: TextView
    private lateinit var textDescription: TextView
    private lateinit var progressBar: ProgressBar

    private var pdfData = Uri.EMPTY

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
        storageRef = FirebaseStorage.getInstance().reference

        setContentView(R.layout.activity_add_deadline)
        textTitle = findViewById(R.id.add_deadline_select_title)
        textDate = findViewById(R.id.add_deadline_text_date)
        textTime = findViewById(R.id.add_deadline_text_time)
        pdfTextView = findViewById(R.id.selectedPdf)
        progressBar = findViewById(R.id.progressBar)
            progressBar.visibility = View.INVISIBLE
            progressBar.isIndeterminate=true



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

        initializePlacesAutocomplete()
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
        alertBuilder.setNegativeButton("Cancel",
            DialogInterface.OnClickListener { dialogInterface, _ -> dialogInterface.cancel() })
        alertBuilder.setPositiveButton("OK",
            DialogInterface.OnClickListener { _, _ -> applyParsing(res) })
        alertBuilder.show()
    }

    private fun updateDisplayedDateAndTime() {
        textDate.text = selectedDate.toLocalDate().toString()
        textTime.text = selectedDate.toLocalTime().toString()
    }

    /**
     * Setup the PlacesAutocompleteClient used to select a location for a deadline
     */
    private fun initializePlacesAutocomplete() {
        Places.initialize(applicationContext, getString(R.string.places_key))
        // Must be commented for now
        /*
        Places.createClient(this)
        */
        initializeAutocompleteFragment()
    }

    /**
     * Setup the AutocompleteSupportFragment with all its parameters
     */
    private fun initializeAutocompleteFragment() {
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment

        autocompleteFragment
            .setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME)) //Returned fields
            .setCountries(listOf("CH")) // Default startup country

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // TODO: Define here what we do with the returned place
                Log.i(ContentValues.TAG, "Place: ${place.name}, ${place.id}")
            }

            override fun onError(status: Status) {
                Log.e(ContentValues.TAG, "An error occurred: $status")
            }
        })
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
            // Reset the text input for future use
            textTitle.text = ""
            //loading bar
            progressBar.visibility = View.VISIBLE
            //start upload
            uploadPdfToFirebase(pdfData) { ref ->
                //hide loading bar
                progressBar.visibility = View.GONE;

                // create the deadline
                val deadline = Deadline(
                    titleDeadline,
                    DeadlineState.TODO,
                    selectedDate,
                    textDescription.text.toString(),
                    pdfPath = ref
                )
                //get notification setting
                val notificationsTimes = retrieveNotificationsTimes()

                //send toast
                Toast.makeText(this, getString(R.string.deadline_created), Toast.LENGTH_SHORT)
                    .show()

                //add the deadline and finish the activity
                deadlineListViewModel.addDeadline(deadline) {
                    DeadlineNotification.editNotification(it, deadline, notificationsTimes, this)
                    finish()
                }
            }

        }
    }

    //code to be executed when a pdf has been chosen
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                pdfData = result.data!!.data!!
                val path: String = pdfData.toString()
                val lastSlashIndex = path.lastIndexOf("/")
                pdfTextView.text = path.substring(lastSlashIndex + 1, path.length)
            }
        }

    /**
     * Launch pdf selection menu
     */
    fun selectPDF(view: View) {
        PDFUtil.selectPdfIntent() {
            startForResult.launch(it)
        }
    }

    /**
     * upload a pdf file
     */
    private fun uploadPdfToFirebase(data: Uri, callback: (String) -> Unit) {
        if (data != Uri.EMPTY) {
            val ref =
                storageRef.child(FirebaseAuth.getInstance().uid + "/upload" + clockService.now() + ".pdf")
            ref.putFile(data).addOnSuccessListener {
                callback(ref.path)
            }.addOnFailureListener {
                val failureDialog = AlertDialog.Builder(this).setTitle("pdf upload failed").show()
                callback("")
            }
        } else {
            callback("")
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
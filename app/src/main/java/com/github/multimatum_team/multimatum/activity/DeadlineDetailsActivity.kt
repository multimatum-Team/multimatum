package com.github.multimatum_team.multimatum.activity

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.Intent.EXTRA_TEXT
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.github.multimatum_team.multimatum.R
import com.github.multimatum_team.multimatum.model.DeadlineOwner
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.github.multimatum_team.multimatum.model.GroupOwned
import com.github.multimatum_team.multimatum.model.UserOwned
import com.github.multimatum_team.multimatum.repository.DeadlineID
import com.github.multimatum_team.multimatum.service.ClockService
import com.github.multimatum_team.multimatum.util.DeadlineNotification
import com.github.multimatum_team.multimatum.util.JsonDeadlineConverter
import com.github.multimatum_team.multimatum.viewmodel.DeadlineListViewModel
import com.github.multimatum_team.multimatum.viewmodel.GroupViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.Duration
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
    private val groupViewModel: GroupViewModel by viewModels()

    private lateinit var titleView: EditText
    private lateinit var dateView: TextView
    private lateinit var detailView: TextView
    private lateinit var doneButton: CheckBox
    private lateinit var notificationView: TextView
    private lateinit var descriptionView: EditText

    // Set them on default value, waiting the fetch of the deadlines
    private var notificationSelected: BooleanArray = booleanArrayOf(false, false, false, false)
    private var dateTime: LocalDateTime = LocalDateTime.of(2022, 10, 10, 10, 10)
    private var state: DeadlineState = DeadlineState.TODO


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deadline_details)

        // Recuperate the necessary TextViews
        titleView = findViewById(R.id.deadline_details_activity_title)
        dateView = findViewById(R.id.deadline_details_activity_date)
        detailView = findViewById(R.id.deadline_details_activity_done_or_due)
        doneButton = findViewById(R.id.deadline_details_activity_set_done)
        notificationView = findViewById(R.id.deadline_details_activity_notifications)
        descriptionView = findViewById(R.id.deadline_details_activity_description)

        // Recuperate the id of the deadline
        id = intent.getStringExtra(EXTRA_ID) as DeadlineID

        // As the viewModel doesn't recuperate immediately the deadlines,
        // we need an update the moment they are fetched
        setDeadlineObserver()

        // Setup the CheckBox Done to be checked if done
        doneButton.setOnCheckedChangeListener { _, isChecked ->
            state = if (isChecked) DeadlineState.DONE else DeadlineState.TODO
            updateDetail()
        }

        // Set the detail text to inform the user if it is due, done or the remaining time
        updateDetail()

    }

    /**
     * @param View: the current view
     * when clicking ont the button create an intent to launch the QR activity
     */
    fun goQRGenerator(view: View) {
        val intent = Intent(this, QRGeneratorActivity::class.java)
        val deadline = deadlineListViewModel.getDeadline(id)

        // Need to create a custom json builder to convert LocalDateTime to Json
        val jsonConverter = JsonDeadlineConverter()

        val json = jsonConverter.toJson(deadline)
        intent.putExtra(EXTRA_TEXT, json)
        startActivity(intent)
    }

    /**
     * This display a DatePickerDialog and afterward a TimePickerDialog to modify the date
     */
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

    /**
     * Setup a TimePickerDialog that will select a time for the deadline, show it and update the details
     */
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

    /**
     * Edit the notification preferences with an AlertDialog
     */
    fun updateNotifications(view: View) {
        val alertDialogBuilder = AlertDialog.Builder(this)

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
        alertDialogBuilder.setPositiveButton(getString(R.string.done)) { _, _ ->
            notificationView.text = textNotification()
        }
        alertDialogBuilder.show()
    }

    /**
     * Function that put the activity to the Edit Mode or to the Uneditable Mode
     */
    fun goToEditOrNormalMode(view: View) {
        // Put the title, the date, the done button and the notification text to the edit mode or not
        editTitle(editMode)
        editDate(editMode)
        editDone(editMode)
        editNotification(editMode)
        editDescription(editMode)

        // Put the setting image or the tick image on the button to show in which mode we are
        findViewById<ImageButton>(R.id.deadline_details_activity_modify)
            .setImageResource(
                if (editMode) android.R.drawable.checkbox_on_background
                else android.R.drawable.ic_menu_manage
            )

        // Set the color of the text in white if we are in black mode
        adaptToCurrentTheme()

        // Update the data of the deadline in the viewModel
        if (!editMode) updateDeadlineAfterEditionModeExit()

        // Inverse the actual mode (read-only mode, edit mode)
        editMode = editMode.not()
    }

    /**
     * Shift the titleView to a modify state or to a uneditable state
     */
    private fun editTitle(edit: Boolean) {
        titleView.isEnabled = edit
        titleView.setBackgroundResource(
            if (edit) android.R.drawable.edit_text
            else android.R.color.transparent
        )
    }

    /**
     * Modify the deadline in the database when you quit the edition mode
     */
    private fun updateDeadlineAfterEditionModeExit() {
        if (!editMode) {
            val newDeadline = deadlineListViewModel.getDeadline(id).copy(
                title = titleView.text.toString(),
                state = state,
                dateTime = dateTime,
                description = descriptionView.text.toString()
            )
            deadlineListViewModel.modifyDeadline(
                id,
                newDeadline
            )
            if (state == DeadlineState.DONE) {
                DeadlineNotification.deleteNotification(id, this)
            }
        }
    }

    /**
     * Shift the dateView to a modify state or to a uneditable state
     */
    private fun editDate(edit: Boolean) {
        dateView.isClickable = edit
        dateView.setBackgroundResource(
            if (edit) android.R.drawable.btn_default
            else android.R.color.transparent
        )
    }

    /**
     * Shift the doneView to a modify state or to a uneditable state
     */
    private fun editDone(edit: Boolean) {
        doneButton.isClickable = edit
        doneButton.visibility = if (edit) View.VISIBLE else View.GONE
    }

    /**
     * Shift the notificationView to a modify state or to a uneditable state
     */
    private fun editNotification(edit: Boolean) {
        notificationView.isClickable = edit
        notificationView.setBackgroundResource(
            if (edit) android.R.drawable.btn_default
            else android.R.color.transparent
        )
    }

    /**
     * Shift the descriptionView to a modify state or to a uneditable state
     */
    private fun editDescription(edit: Boolean) {
        descriptionView.isEnabled = edit
        descriptionView.setBackgroundResource(
            if (edit) android.R.drawable.edit_text
            else android.R.color.transparent
        )
    }

    /**
     * This function setup the observer to update the information shown when the
     * deadline are updated
     */
    private fun setDeadlineObserver() {
        deadlineListViewModel.getDeadlines().observe(this) { deadlines ->
            // Recuperate the data from the deadline
            val deadline = deadlines[id]!!
            dateTime = deadline.dateTime
            state = deadline.state
            val title = deadline.title
            val description = deadline.description
            val group = deadline.owner

            // Update the information of the notification
            val notifications = DeadlineNotification.listDeadlineNotification(id, this)
            for (id in checkBoxIdTime.keys) {
                notificationSelected[nameCheckBox.indexOf(id)] =
                    notifications.contains(checkBoxIdTime[id])
            }
            notificationView.text = textNotification()

            // Update the data shown
            dateView.text =
                getString(R.string.DueTheXatX, dateTime.toLocalDate(), dateTime.toLocalTime())
            titleView.text = SpannableStringBuilder(title)
            descriptionView.text = SpannableStringBuilder(description)
            doneButton.isChecked = (state == DeadlineState.DONE)
            updateDetail()
            findViewById<TextView>(R.id.deadline_details_activity_group).text =
                getGroupTextAndSetModifyButton(group)

            // Set the View to be unmodifiable at the start and remove displacement of the texts
            normalSetup()
        }
    }

    /**
     * Function that recuperate the name of the group of the deadline, if any,
     * and hide the modify button if the user is not the owner of the group
     */
    private fun getGroupTextAndSetModifyButton(group: DeadlineOwner): String {
        return if (group is UserOwned) {
            getString(R.string.not_in_any_group)
        } else {
            val groupDeadline = groupViewModel.getGroup((group as GroupOwned).groupID)
            val modifyButton =
                findViewById<ImageButton>(R.id.deadline_details_activity_modify)
            // If the group of the deadline is not owned by the user,
            // they can't modify it
            modifyButton.isClickable = groupViewModel.getOwnedGroups().values.contains(groupDeadline)
            modifyButton.isVisible = groupViewModel.getOwnedGroups().values.contains(groupDeadline)

            getString(R.string.in_the_group_X, groupDeadline.name)
        }
    }

    /**
     * Give the text that must be shown in function on how many notifications were selected
     */
    private fun textNotification(): String {
        val notifications =
            nameCheckBox.filter { t -> notificationSelected[nameCheckBox.indexOf(t)] }
        if (notifications.isEmpty()) return getString(R.string.no_alarm_planned)
        return getString(
            R.string.alarm_X_before, when (notifications.size) {
                1 -> notifications[0]
                2 -> notifications[0] + " and " + notifications[1]
                3 -> notifications[0] + ", " + notifications[1] + " and " + notifications[2]
                else -> notifications[0] + ", " + notifications[1] + ", " + notifications[2] + " and " + notifications[3]
            }
        )
    }

    /**
     * Update the information shown in the TextView detailView
     */
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

    /**
     * Change the color of the date and title views according to the theme
     */
    private fun adaptToCurrentTheme() {
        val isNightMode = sharedPreferences.getBoolean(
            MainSettingsActivity.DARK_MODE_PREF_KEY,
            IS_DEFAULT_DARK_MODE_ENABLED
        )
        val textColor = if (editMode) Color.BLACK else Color.WHITE

        if (isNightMode) {
            dateView.setTextColor(textColor)
            titleView.setTextColor(textColor)
            notificationView.setTextColor(textColor)
            descriptionView.setTextColor(textColor)
        }
    }

    /**
     * Assure the everything is unmodifiable and fix arrangement
     */
    private fun normalSetup() {
        editTitle(true)
        editTitle(false)
        editDate(true)
        editDate(false)
        doneButton.isClickable = false
        editNotification(true)
        editNotification(false)
        editDescription(true)
        editDescription(false)
    }

    companion object {
        private const val EXTRA_ID =
            "com.github.multimatum_team.deadline.details.id"
        private const val IS_DEFAULT_DARK_MODE_ENABLED = false

        private val nameCheckBox = arrayOf("1 hour", "5 hours", "1 day", "3 days")
        private val checkBoxIdTime = mapOf(
            "1 hour" to Duration.ofHours(1).toMillis(),
            "5 hours" to Duration.ofHours(5).toMillis(),
            "1 day" to Duration.ofDays(1).toMillis(),
            "3 days" to Duration.ofDays(3).toMillis()
        )

        // Launch an Intent to access this activity with a Deadline data
        fun newIntent(context: Context, id: DeadlineID): Intent {
            val detailIntent = Intent(context, DeadlineDetailsActivity::class.java)
            detailIntent.putExtra(EXTRA_ID, id)
            return detailIntent
        }
    }
}

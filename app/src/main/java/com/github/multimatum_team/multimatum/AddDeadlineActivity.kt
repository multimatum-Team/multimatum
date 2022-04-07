package com.github.multimatum_team.multimatum

import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.github.multimatum_team.multimatum.viewmodel.DeadlineListViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime

/**
 *  Activity who create a deadline using a DatePicker
 */
@AndroidEntryPoint
class AddDeadlineActivity : AppCompatActivity() {

    companion object {
        // Temporary value that will be link later to the actual day
        private val initDate: LocalDateTime = LocalDateTime.of(2022, 1, 1, 0, 0)
    }

    private val deadlineListViewModel: DeadlineListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_deadline)
        val datePicker = findViewById<DatePicker>(R.id.add_deadline_date_picker)
        datePicker.init(initDate.year, initDate.month.value, initDate.dayOfMonth, null)
    }

    fun addDeadline(view: View) {
        // Getting the necessary views
        val datePicker = findViewById<DatePicker>(R.id.add_deadline_date_picker)
        val editText = findViewById<TextView>(R.id.add_deadline_select_title)

        // Getting the entered text and the selected date
        val titleDeadline = editText.text.toString()
        val dateDeadline =
            LocalDateTime.of(datePicker.year, datePicker.month + 1, datePicker.dayOfMonth, 23, 59)

        // Check if the title is not empty
        if (titleDeadline == "") {
            Toast.makeText(this, getString(R.string.enter_a_title), Toast.LENGTH_SHORT).show()
        } else {
            // Add the deadline
            val deadline = Deadline(titleDeadline, DeadlineState.TODO, dateDeadline)
            deadlineListViewModel.addDeadline(deadline)

            Toast.makeText(this, getString(R.string.deadline_created), Toast.LENGTH_SHORT).show()

            // Reset the text input for future use
            editText.text = ""
        }
    }
}
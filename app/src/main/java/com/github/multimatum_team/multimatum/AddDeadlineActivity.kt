package com.github.multimatum_team.multimatum

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import android.widget.TextView
import android.widget.Toast
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.github.multimatum_team.multimatum.repository.DeadlineRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import javax.inject.Inject

/**
 *  Activity who create a deadline using a DatePick
 */
@AndroidEntryPoint
class AddDeadlineActivity : AppCompatActivity() {
    @Inject
    lateinit var deadlineRepository: DeadlineRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_deadline)

        val datePicker = findViewById<DatePicker>(R.id.add_deadline_date_picker)
        datePicker.init(2022, 1, 1, null)
    }

    fun addDeadline(view: View) {
        // Getting the necessary views
        val datePicker = findViewById<DatePicker>(R.id.add_deadline_date_picker)
        val editText = findViewById<TextView>(R.id.add_deadline_select_title)

        // Getting the entered text and the selected date
        val titleDeadline = editText.text.toString()
        val dateDeadline = LocalDate.of(datePicker.year, datePicker.month, datePicker.dayOfMonth)

        // Check if the title is not empty
        if (titleDeadline == "") {
            Toast.makeText(this, "Enter a title", Toast.LENGTH_SHORT).show()
        } else {
            // Add the deadline
            val deadline = Deadline(titleDeadline, DeadlineState.TODO, dateDeadline)
            runBlocking { deadlineRepository.put(deadline) }

            Toast.makeText(this, "Deadline created.", Toast.LENGTH_SHORT).show()

            // Reset the text input for future use
            editText.text = ""
        }


    }


}
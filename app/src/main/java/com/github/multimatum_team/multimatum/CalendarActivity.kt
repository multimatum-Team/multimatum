package com.github.multimatum_team.multimatum

import android.os.Bundle
import android.view.View
import android.widget.CalendarView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.github.multimatum_team.multimatum.repository.DeadlineRepository
import com.github.multimatum_team.multimatum.viewmodel.DeadlineListViewModel
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

@AndroidEntryPoint
class CalendarActivity : AppCompatActivity() {
    @Inject
    lateinit var deadlineRepository: DeadlineRepository

    private val viewModel: DeadlineListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initCalendar()
    }

    private fun initCalendar() {
        setContentView(R.layout.activity_calendar)
    }

    /*
    The purpose of this function is to provide the user the ability to add a new
    deadline easily using directly the calendar to select the date.
    ***TO DO***: store deadline and display them on the calendar.
     */
    fun addNewDeadlineCalendar(view: View) {
        // Getting the necessary views
        val editText = findViewById<TextInputEditText>(R.id.textInputEditCalendar)
        val calendar = findViewById<CalendarView>(R.id.calendar_view)

        // Getting the entered text and the selected date
        val deadlineTitle = editText.text.toString()
        val selectedDate =
            Instant.ofEpochMilli(calendar.date).atZone(ZoneId.systemDefault()).toLocalDate()

        // Add the new event to the deadline list
        val deadline = Deadline(deadlineTitle, DeadlineState.TODO, selectedDate)
        viewModel.addDeadline(deadline)

        // Reset the text input for future use
        editText.setText("")
    }
}
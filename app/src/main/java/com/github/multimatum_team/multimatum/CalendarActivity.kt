package com.github.multimatum_team.multimatum

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.CalendarView
import android.widget.ListView
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineAdapter
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.google.android.material.textfield.TextInputEditText
import java.time.Instant
import java.time.ZoneId

class CalendarActivity : AppCompatActivity() {

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
        val listView = findViewById<ListView>(R.id.calendar_deadline_listview)
        val editText = findViewById<TextInputEditText>(R.id.textInputEditCalendar)
        val calendar = findViewById<CalendarView>(R.id.calendar_view)

        // Getting the entered text and the selected date
        val deadlineTitle = editText.text.toString()
        val selectedDate =
            Instant.ofEpochMilli(calendar.date).atZone(ZoneId.systemDefault()).toLocalDate()

        // Add the new event to the deadline list
        val deadline = Deadline(deadlineTitle, DeadlineState.TODO, selectedDate)
        val adapter = DeadlineAdapter(this)
        adapter.submitList(listOf(deadline))
        listView.adapter = adapter

        // Reset the text input for future use
        editText.setText("")
    }
}
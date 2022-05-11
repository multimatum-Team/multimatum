package com.github.multimatum_team.multimatum.activity

import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.CalendarView
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.multimatum_team.multimatum.R
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.github.multimatum_team.multimatum.util.hideKeyboardWhenClickingInTheVoid
import com.github.multimatum_team.multimatum.util.setOnIMEActionDone
import com.github.multimatum_team.multimatum.viewmodel.DeadlineListViewModel
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@AndroidEntryPoint
class CalendarActivity : AppCompatActivity() {
    private val viewModel: DeadlineListViewModel by viewModels()
    private var selectedDate: LocalDateTime =
        Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initCalendar()
        initTextInput()
    }

    /**
     * This function initialize the calender, it allows the app to listen to
     * an eventual date selection by the user.
     */
    private fun initCalendar() {
        setContentView(R.layout.activity_calendar)

        // Get the calendar view
        val calendar = findViewById<CalendarView>(R.id.calendar_view)

        // Update the selected date in case of a selection in the calendar
        calendar.setOnDateChangeListener { _, year, month, day ->
            selectedDate = LocalDateTime.of(year, month + 1, day, 0, 0)
        }
    }

    /**
     * This function allows the user to add a new deadline directly, using the "ENTER" key (more intuitive).
     */
    private fun initTextInput() {
        val edit = findViewById<TextInputEditText>(R.id.textInputEditCalendar)

        edit.setOnIMEActionDone(this) { deadlineTitle ->
            val deadline = Deadline(deadlineTitle, DeadlineState.TODO, selectedDate)
            viewModel.addDeadline(deadline) {}
            // Reset the text input for future use
            edit.setText("")
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        hideKeyboardWhenClickingInTheVoid(event)
        return super.dispatchTouchEvent(event)
    }
}
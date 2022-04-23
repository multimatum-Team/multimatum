package com.github.multimatum_team.multimatum

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.CalendarView
import android.widget.EditText
import android.widget.TextView.OnEditorActionListener
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.github.multimatum_team.multimatum.util.DeadlineNotification
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

    /*
    This function initialize the calender, it allows the app to listen to
    an eventual date selection by the user.
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

    /*
    This function extract the text from the input, clear the field, add
    a new deadline and close the keyboard.
     */
    private fun clearTextInputAndAddDeadline(edit: TextInputEditText, v: View) {
        // Add a new deadline using the entered text
        addNewDeadlineCalendar(v)

        // Hide the keyboard and clear the focus on the text input
        edit.clearFocus()
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    /*
    This function allows the user to add a new deadline directly, using the "ENTER" key (more intuitive).
     */
    private fun initTextInput() {
        val edit = findViewById<TextInputEditText>(R.id.textInputEditCalendar)

        // Adding a listener to handle the "ENTER" key pressed.
        edit.setOnKeyListener { v, keycode, event ->
            if ((keycode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                clearTextInputAndAddDeadline(edit, v)
                // The listener has consumed the event
                return@setOnKeyListener true
            }
            false
        }

        // Adding a listener to handle the "DONE" key pressed.
        edit.setOnEditorActionListener(OnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                clearTextInputAndAddDeadline(edit, v)
                // The listener has consumed the event
                return@OnEditorActionListener true
            }
            false
        })
    }

    /*
    This function allows the user to exit the text input intuitively, just by clicking outside
     */
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            // We are in the case were the user has touched outside
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    // If the user has touched a place outside the keyboard, remove the focus and keyboard
                    v.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    /*
    The purpose of this function is to provide the user the ability to add a new
    deadline easily using directly the calendar to select the date.
     */
    fun addNewDeadlineCalendar(view: View) {
        // Getting the necessary views
        val editText = findViewById<TextInputEditText>(R.id.textInputEditCalendar)

        // Getting the entered text and the selected date
        val deadlineTitle = editText.text.toString()


        // Add the new event to the deadline list
        val deadline = Deadline(deadlineTitle, DeadlineState.TODO, selectedDate)

        viewModel.addDeadline(deadline){}

        // Reset the text input for future use
        editText.setText("")
    }
}
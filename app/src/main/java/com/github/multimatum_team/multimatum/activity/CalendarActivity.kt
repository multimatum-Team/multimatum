package com.github.multimatum_team.multimatum.activity

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.events.calendar.utils.EventsCalendarUtil
import com.events.calendar.views.EventsCalendar
import com.github.multimatum_team.multimatum.R
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.github.multimatum_team.multimatum.service.ClockService
import com.github.multimatum_team.multimatum.util.hideKeyboard
import com.github.multimatum_team.multimatum.util.hideKeyboardWhenClickingInTheVoid
import com.github.multimatum_team.multimatum.util.setOnIMEActionDone
import com.github.multimatum_team.multimatum.viewmodel.DeadlineListViewModel
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class CalendarActivity : AppCompatActivity(), EventsCalendar.Callback {

    override fun onDayLongPressed(selectedDate: Calendar?) {
    }
    override fun onMonthChanged(monthStartDate: Calendar?) {
    }
    override fun onDaySelected(selectedDate: Calendar?) {
        dateSelected = transformCalendarToLocalDateTime(selectedDate!!)
    }

    @Inject
    lateinit var clockService: ClockService

    private val viewModel: DeadlineListViewModel by viewModels()
    private lateinit var dateSelected: LocalDateTime


    private lateinit var calendarView: EventsCalendar
    private lateinit var deadlineTitleInputView: TextInputEditText
    private lateinit var addDeadlineButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)
        dateSelected = clockService.now()

        calendarView = findViewById(R.id.calendar_view)
        deadlineTitleInputView = findViewById(R.id.textInputEditCalendar)
        addDeadlineButton = findViewById(R.id.calendar_add_deadline_button)

        val start = Calendar.getInstance()
        start.add(Calendar.MONTH, -1)
        val end = Calendar.getInstance()
        end.add(Calendar.MONTH, EventsCalendarUtil.DEFAULT_NO_OF_MONTHS / 2)

        calendarView.setSelectionMode(calendarView.SINGLE_SELECTION) //set mode of Calendar
            .setToday(transformLocalDateTimeToCalendar(dateSelected))
            .setMonthRange(
                start,
                end
            ) //set starting month [start: Calendar] and ending month [end: Calendar]
            .setWeekStartDay(
                Calendar.SUNDAY,
                false
            ) //set start day of the week as you wish [startday: Int, doReset: Boolean]
            .setCurrentSelectedDate(transformLocalDateTimeToCalendar(dateSelected)) //set current date and scrolls the calendar to the corresponding month of the selected date [today: Calendar]
            .setEventDotColor(Color.RED)
            .setCallback(this)

        setDotForEveryDeadline()

        initTextInput()
        initAddButton()
    }

    private fun transformCalendarToLocalDateTime(calendar: Calendar): LocalDateTime {
        return LocalDateTime.of(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH), 0, 0
        )
    }

    private fun transformLocalDateTimeToCalendar(localDateTime: LocalDateTime): Calendar {
        val calendar = Calendar.getInstance()
        calendar.set(localDateTime.year, localDateTime.month.ordinal, localDateTime.dayOfMonth)
        return calendar
    }

    private fun setDotForEveryDeadline() {
        viewModel.getDeadlines().observe(this) { deadlines ->
            for (deadline in deadlines.values) {
                calendarView.addEvent(transformLocalDateTimeToCalendar(deadline.dateTime))
            }
            calendarView.build()
        }
    }


    /**
     * This function allows the user to add a new deadline directly, using the "ENTER" key (more intuitive).
     */
    private fun initTextInput() {
        deadlineTitleInputView.setOnIMEActionDone(this) { deadlineTitle ->
            val deadline = Deadline(deadlineTitle, DeadlineState.TODO, dateSelected)
            viewModel.addDeadline(deadline)
            // Reset the text input for future use
            deadlineTitleInputView.setText("")
        }
    }

    private fun initAddButton() {
        addDeadlineButton.setOnClickListener {
            deadlineTitleInputView.hideKeyboard(this)
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        hideKeyboardWhenClickingInTheVoid(event)
        return super.dispatchTouchEvent(event)
    }
}
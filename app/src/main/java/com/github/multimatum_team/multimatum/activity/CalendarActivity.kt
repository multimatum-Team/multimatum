package com.github.multimatum_team.multimatum.activity

import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.ListView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.events.calendar.utils.EventsCalendarUtil
import com.events.calendar.views.DateText
import com.events.calendar.views.EventsCalendar
import com.github.multimatum_team.multimatum.R
import com.github.multimatum_team.multimatum.adaptater.DeadlineAdapter
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.github.multimatum_team.multimatum.service.ClockService
import com.github.multimatum_team.multimatum.util.hideKeyboard
import com.github.multimatum_team.multimatum.util.hideKeyboardWhenClickingInTheVoid
import com.github.multimatum_team.multimatum.util.setOnIMEActionDone
import com.github.multimatum_team.multimatum.viewmodel.DeadlineListViewModel
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class CalendarActivity : AppCompatActivity(), EventsCalendar.Callback {

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

        // Allow the calendar to go only a month before the actual date and
        // with no end after it
        val start = transformLocalDateTimeToCalendar(clockService.now())
        start.add(Calendar.MONTH, -1)
        val end = transformLocalDateTimeToCalendar(clockService.now())
        end.add(Calendar.MONTH, EventsCalendarUtil.DEFAULT_NO_OF_MONTHS / 2)

        // Setup of the calendar
        calendarView.setSelectionMode(calendarView.SINGLE_SELECTION)
            .setToday(transformLocalDateTimeToCalendar(dateSelected))
            .setMonthRange(start, end) //set starting month and ending month
            .setWeekStartDay(Calendar.MONDAY, false)
            .setCurrentSelectedDate(transformLocalDateTimeToCalendar(dateSelected))
            .setEventDotColor(Color.RED)
            // allow to choose what happen when a date is selected and more
            // with the 3 override functions
            .setCallback(this)

        val listView = findViewById<ListView>(R.id.calendar_view_listView)
        val listViewAdapter = DeadlineAdapter(this, viewModel)

        // Set up observer on deadline list to show only deadline with the selected date
        listView.adapter = listViewAdapter
        viewModel.getDeadlines().observe(this) { deadlines ->
            val deadlineOfTheSelectedDay = deadlines.filter { deadline ->
                deadline.value.dateTime.withHour(0).withMinute(0)
                    .equals(dateSelected.withHour(0).withMinute(0))
            }
            listViewAdapter.setDeadlines(deadlineOfTheSelectedDay)
        }

        // Set when you maintain your finger on an item of the list, launch the detail activity
        listView.setOnItemLongClickListener { _, _, position, _ ->
            val (id, _) = listViewAdapter.getItem(position)
            val detailIntent = DeadlineDetailsActivity.newIntent(this, id)
            startActivity(detailIntent)
            // Last line necessary to use this function
            true
        }

        updateColorText()
        setDotForEveryDeadline()
        initTextInput()
        initAddButton()
    }

    private fun updateColorText() {
        calendarView.setMonthTitleColor(getColor(R.color.deadline_details_title))
            .setWeekHeaderColor(getColor(R.color.deadline_details_title))
            .setSecondaryTextColor(getColor(R.color.gray_variation_text))
            .setPrimaryTextColor(getColor(R.color.deadline_details_title))
            .setSelectionColor(getColor(R.color.deadline_details_title))
            .setSelectedTextColor(getColor(R.color.deadline_item_background))
        // Update the color shown
        DateText.invalidateColors()
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

    /**
     * This function set the calendar view to show a dot on a date
     * if there is a deadline who will be due at this date
     */
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
            val deadline =
                Deadline(deadlineTitleInputView.text.toString(), DeadlineState.TODO, dateSelected)
            viewModel.addDeadline(deadline)
            deadlineTitleInputView.setText("")
            deadlineTitleInputView.hideKeyboard(this)
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        hideKeyboardWhenClickingInTheVoid(event)
        return super.dispatchTouchEvent(event)
    }

    override fun onDayLongPressed(selectedDate: Calendar?) {}
    override fun onMonthChanged(monthStartDate: Calendar?) {}
    override fun onDaySelected(selectedDate: Calendar?) {
        dateSelected = transformCalendarToLocalDateTime(selectedDate!!)
        // if the deadline are fetched, show the deadlines of the selected day
        val deadlines = viewModel.getDeadlines().value
        if (!deadlines.isNullOrEmpty()) {
            val deadlineOfTheSelectedDay = deadlines.filter { deadline ->
                deadline.value.dateTime.toLocalDate().equals(dateSelected.toLocalDate())
            }
            val adapter = findViewById<ListView>(R.id.calendar_view_listView).adapter
            (adapter as DeadlineAdapter).setDeadlines(deadlineOfTheSelectedDay)
        }
    }
}
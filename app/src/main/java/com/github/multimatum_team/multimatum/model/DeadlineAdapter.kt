package com.github.multimatum_team.multimatum.model

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.github.multimatum_team.multimatum.R
import com.github.multimatum_team.multimatum.repository.DeadlineID
import com.github.multimatum_team.multimatum.service.ClockService
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.temporal.ChronoUnit
import java.util.*

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ClockServiceEntryPoint {
    fun provideClockService(): ClockService
}

/**
Class which is used to show the deadline in a clear list.
Based on the tutorial:
https://www.raywenderlich.com/155-android-listview-tutorial-with-kotlin
 */
class DeadlineAdapter(private val context: Context) : BaseAdapter() {
    companion object {
        // value who define when there is not much time left for a deadline
        const val URGENT_THRESHOLD_DAYS = 5
        const val PRESSING_THRESHOLD_DAYS = 10
    }

    var clockService: ClockService =
        EntryPointAccessors
            .fromApplication(context, ClockServiceEntryPoint::class.java)
            .provideClockService()

    private var dataSource: List<Pair<DeadlineID, Deadline>> = listOf()

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    fun setDeadlines(deadlines: Map<DeadlineID, Deadline>) {
        dataSource = sortDeadline(deadlines)
                // deadlines.toSortedMap { id1, id2 -> compareValues(deadlines[id1]!!.dateTime, deadlines[id2]!!.dateTime) }
        notifyDataSetChanged()
    }

    private fun sortDeadline(deadlines: Map<DeadlineID, Deadline>): List<Pair<DeadlineID, Deadline>> {
        val doneDeadlines: List<Pair<DeadlineID, Deadline>> = deadlines
            .filter{(_, deadline) -> deadline.state == DeadlineState.DONE}
            .map{(id,deadline) -> Pair(id, deadline)}.sortedBy { (_, deadline) -> deadline.dateTime}
        val undoneDeadlines =  deadlines
            .filter{(_, deadline) -> deadline.state == DeadlineState.TODO}
            .map {(id,deadline) -> Pair(id, deadline)}.sortedBy {(_, deadline) -> deadline.dateTime}
        return undoneDeadlines + doneDeadlines
    }

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): Pair<DeadlineID, Deadline> {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    // Customise the units of the list
    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Get view for row item
        val rowView = inflater.inflate(R.layout.list_item_deadline, parent, false)
        // Get title element
        val titleTextView = rowView.findViewById<TextView>(R.id.deadline_list_title)
        // Get subtitle element
        val subtitleTextView = rowView.findViewById<TextView>(R.id.deadline_list_subtitle)
        // Get detail element
        val detailTextView = rowView.findViewById<TextView>(R.id.deadline_list_detail)

        val (_, deadline) = getItem(position)

        // Show the title
        titleTextView.text = deadline.title
        titleTextView.setTypeface(null, Typeface.BOLD)

        // Show the date
        subtitleTextView.text = context.getString(
            R.string.DueTheXatX,
            deadline.dateTime.toLocalDate(),
            deadline.dateTime.toLocalTime()
        )
        subtitleTextView.setTypeface(null, Typeface.ITALIC)

        // Show how much time left or if it is due or done.
        val detail: String
        when {
            deadline.state == DeadlineState.DONE -> {
                detail = context.getString(R.string.done)
                detailTextView.setTextColor(Color.GREEN)
                detailTextView.setTypeface(null, Typeface.BOLD)
            }
            clockService.now() > deadline.dateTime -> {
                detail = context.getString(R.string.isAlreadyDue)
            }
            else -> {
                val timeRemaining = clockService.now().until(deadline.dateTime, ChronoUnit.DAYS)

                // If the timeRemaining is less than a day, show the hours left
                detail = if (timeRemaining <= 0) {
                    val hoursRemaining =
                        clockService.now().until(deadline.dateTime, ChronoUnit.HOURS)
                    context.getString(R.string.DueInXHours, hoursRemaining.toString())
                } else {
                    context.getString(R.string.DueInXDays, timeRemaining.toString())
                }

                // If the remaining days is too small, put the text in red or orange
                if (timeRemaining < URGENT_THRESHOLD_DAYS) {
                    detailTextView.setTextColor(Color.RED)
                    detailTextView.setTypeface(null, Typeface.BOLD)
                } else if (timeRemaining < PRESSING_THRESHOLD_DAYS) {
                    detailTextView.setTextColor(Color.rgb(255, 165, 0))
                    detailTextView.setTypeface(null, Typeface.BOLD)
                }
            }
        }
        detailTextView.text = detail
        return rowView
    }

}
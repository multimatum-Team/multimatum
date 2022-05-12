package com.github.multimatum_team.multimatum.adaptater

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.ToggleButton
import androidx.core.content.ContextCompat
import com.github.multimatum_team.multimatum.R
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.github.multimatum_team.multimatum.model.GroupOwned
import com.github.multimatum_team.multimatum.model.UserOwned
import com.github.multimatum_team.multimatum.repository.DeadlineID
import com.github.multimatum_team.multimatum.service.ClockService
import com.github.multimatum_team.multimatum.viewmodel.DeadlineListViewModel
import com.github.multimatum_team.multimatum.viewmodel.GroupViewModel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.lang.IllegalArgumentException
import java.time.temporal.ChronoUnit

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
enum class FilterState{
    /**
     * state to filter deadlines in groups
     */
    GROUPS,

    /**
     * state to filter deadlines not in groups
     */
    MINE,

    /**
     * don't filter deadlines
     */

    ALL
}

class DeadlineAdapter(
    private val context: Context,
    private val deadlineListViewModel: DeadlineListViewModel, var state: FilterState = FilterState.ALL,
    private val userGroupViewModel: GroupViewModel
) : BaseAdapter() {
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

    fun setDeadlines(deadlines: Map<DeadlineID, Deadline>, filter: String = "") {
        dataSource = sortDeadline(deadlines, filter)
        notifyDataSetChanged()
    }

    private fun filterByGroup(deadlines: MutableList<Pair<DeadlineID, Deadline>>, filter: String):
            MutableList<Pair<DeadlineID, Deadline>>{
        if (filter.isBlank() or filter.isEmpty()) throw IllegalArgumentException()
        val result = deadlines.filter { (_, deadline) -> deadline.owner is GroupOwned && userGroupViewModel.getGroup(deadline.owner.groupID).name == filter}.toMutableList()
        Log.d("filter : ", filter)
        return result
    }

    private fun filterByMine(deadlines: MutableList<Pair<DeadlineID, Deadline>>):
            MutableList<Pair<DeadlineID, Deadline>>{
        return deadlines.filter { (_, deadline) -> deadline.owner is UserOwned}.toMutableList()
    }



    private fun sortDeadline(deadlines: Map<DeadlineID, Deadline>, filter: String = ""): List<Pair<DeadlineID, Deadline>> {
        val partition = DeadlineState.values().associate { state ->
            Pair<DeadlineState, MutableList<Pair<DeadlineID, Deadline>>>(state, mutableListOf())
        }.toMap()
        for ((id, deadline) in deadlines.entries) {
            partition[deadline.state]!!.add(Pair(id, deadline))
        }

        var result: MutableList<Pair<DeadlineID, Deadline>> = mutableListOf()
        for (state in DeadlineState.values()) {
            result.addAll(partition[state]!!.sortedBy { it.second.dateTime })
        }

        if(state == FilterState.GROUPS){
            result = filterByGroup(result, filter)
        }else if(state == FilterState.MINE){
            result = filterByMine(result)
        }

        return result
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
        // Get Checkbox
        val doneCheckbox = rowView.findViewById<ToggleButton>(R.id.deadline_list_check_done)

        val (id, deadline) = getItem(position)

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
        updateDetails(detailTextView, deadline)

        // Set the checkbox
        doneCheckbox.isChecked = (deadline.state == DeadlineState.DONE)
        doneCheckbox.setOnCheckedChangeListener { _, isChecked ->
            val newDeadline =
                deadline.copy(state = if (isChecked) DeadlineState.DONE else DeadlineState.TODO)
            updateDetails(detailTextView, newDeadline)
            deadlineListViewModel.modifyDeadline(id, newDeadline)
        }
        return rowView
    }

    private fun updateDetails(detailTextView: TextView, deadline: Deadline) {
        val detail: String
        when {
            deadline.state == DeadlineState.DONE -> {
                detail = context.getString(R.string.done)
                detailTextView.setTextColor(Color.GREEN)
                detailTextView.setTypeface(detailTextView.typeface, Typeface.BOLD)
            }
            clockService.now() > deadline.dateTime -> {
                detail = context.getString(R.string.isAlreadyDue)
                detailTextView.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.deadline_details_title
                    )
                )
                detailTextView.setTypeface(detailTextView.typeface, Typeface.NORMAL)
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
                    detailTextView.setTypeface(detailTextView.typeface, Typeface.BOLD)
                } else if (timeRemaining < PRESSING_THRESHOLD_DAYS) {
                    detailTextView.setTextColor(Color.rgb(255, 165, 0))
                    detailTextView.setTypeface(detailTextView.typeface, Typeface.BOLD)
                }
            }
        }
        detailTextView.text = detail
    }

}
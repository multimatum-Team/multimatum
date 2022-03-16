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

/**
Class who is used to show the deadline in a clear list.
Based on the tutorial:
https://www.raywenderlich.com/155-android-listview-tutorial-with-kotlin
 */
class DeadlineAdapter(private val context: Context, private val dataSource: List<Deadline>) : BaseAdapter() {
    companion object {
        // value who define when there is not much time left for a deadline
        const val urgent = 5
        const val pressing = 10
    }

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    // Customise the units of the list
    @SuppressLint( "ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Get view for row item
        val rowView = inflater.inflate(R.layout.list_item_deadline, parent, false)
        // Get title element
        val titleTextView = rowView.findViewById(R.id.deadline_list_title) as TextView
        // Get subtitle element
        val subtitleTextView = rowView.findViewById(R.id.deadline_list_subtitle) as TextView
        // Get detail element
        val detailTextView = rowView.findViewById(R.id.deadline_list_detail) as TextView

        val deadline = getItem(position) as Deadline

        // Show the title
        titleTextView.text = deadline.title
        titleTextView.setTypeface(null, Typeface.BOLD)

        // Show the date
        subtitleTextView.text = context.getString(R.string.DueTheX,deadline.date)
        subtitleTextView.setTypeface(null, Typeface.ITALIC)

        // Show how much time left or if it is due or done.
        val detail: String
        when {
            deadline.state == DeadlineState.DONE -> {
                detail = context.getString(R.string.done)
            }
            deadline.isDue -> {
                detail = context.getString(R.string.isAlreadyDue)
            }
            else -> {
                detail = context.getString(R.string.DueInXDays,deadline.timeRemaining?.days.toString())
                // If the remaining days is too small, put them in red or orange
                if ((deadline.timeRemaining!!.days) < urgent) {
                    detailTextView.setTextColor(Color.RED)
                    detailTextView.setTypeface(null, Typeface.BOLD)
                } else if ((deadline.timeRemaining!!.days) < pressing) {
                    detailTextView.setTextColor(Color.rgb(255, 165, 0))
                    detailTextView.setTypeface(null, Typeface.BOLD)
                }
            }
        }
        detailTextView.text = detail
        return rowView
    }

}
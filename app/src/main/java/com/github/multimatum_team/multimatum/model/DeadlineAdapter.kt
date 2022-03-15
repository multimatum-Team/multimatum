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
/*
Class who is used to show the deadline in a clear list.
Based on the tutorial:
https://www.raywenderlich.com/155-android-listview-tutorial-with-kotlin
 */
class DeadlineAdapter(context: Context, private val dataSource: List<Deadline>) : BaseAdapter() {
    private val inflater: LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("ViewHolder", "SetTextI18n")
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

        titleTextView.text = deadline.title
        titleTextView.setTypeface(null, Typeface.BOLD)

        subtitleTextView.text = "Due the ${deadline.date}"
        subtitleTextView.setTypeface(null, Typeface.ITALIC)

        val detail: String
        when {
            deadline.state == DeadlineState.DONE -> {
                detail = "Done"
            }
            deadline.isDue -> {
                detail = "Is already Due"
            }
            else -> {
                detail = "Due in " + deadline.timeRemaining?.days + " Days"
                if ((deadline.timeRemaining!!.days) < 5) {
                    detailTextView.setTextColor(Color.RED)
                    detailTextView.setTypeface(null, Typeface.BOLD)
                } else if ((deadline.timeRemaining!!.days) < 10) {
                    detailTextView.setTextColor(Color.rgb(255, 165, 0))
                    detailTextView.setTypeface(null, Typeface.BOLD)
                }

            }

        }
        detailTextView.text = detail
        return rowView
    }

}
package com.github.multimatum_team.multimatum.adaptater

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckedTextView
import com.github.multimatum_team.multimatum.R
import com.github.multimatum_team.multimatum.model.*

/**
 * A filter allowing the user to view only a subset of their deadlines.
 */
sealed interface DeadlineFilter {
    /**
     * Returns whether the filter lets the deadline go through, and be displayed on the deadline
     * list.
     */
    fun matches(deadline: Deadline): Boolean
}

/**
 * No filter is applied (default)
 */
object NoFilter : DeadlineFilter {
    override fun matches(deadline: Deadline): Boolean = true
}

/**
 * Only show personal deadlines (owned by the user).
 */
object UserFilter : DeadlineFilter {
    override fun matches(deadline: Deadline): Boolean =
        deadline.owner == UserOwned
}

/**
 * Match deadlines belonging to a given group.
 * @param groupID the ID of the group of which we want to show the deadlines
 * @param groupName the name of the group, to be shown in the filter selection
 */
data class GroupFilter(val groupID: GroupID, val groupName: String) : DeadlineFilter {
    override fun matches(deadline: Deadline): Boolean =
        deadline.owner == GroupOwned(groupID)
}

/**
 * An adapter to show the various filters that the user can apply on their deadlines.
 */
class DeadlineFilterAdapter(context: Context) : BaseAdapter() {
    private val filters: MutableList<DeadlineFilter> = mutableListOf(NoFilter)

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    /**
     * Update the filter with a new list of groups.
     */
    fun setGroups(groups: List<UserGroup>) {
        filters.clear()
        filters.add(NoFilter)
        filters.add(UserFilter)
        for (group in groups.sortedBy(UserGroup::name)) {
            filters.add(GroupFilter(group.id, group.name))
        }
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return filters.size
    }

    override fun getItem(position: Int): DeadlineFilter {
        return filters[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = inflater.inflate(
            R.layout.spinner_item_deadline_filter,
            parent,
            false
        ) as CheckedTextView
        view.text = when (val filter = filters[position]) {
            NoFilter -> "All"
            UserFilter -> "Mine"
            is GroupFilter -> filter.groupName
        }
        return view
    }
}
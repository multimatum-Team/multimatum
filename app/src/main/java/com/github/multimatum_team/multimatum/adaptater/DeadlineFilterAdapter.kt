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

sealed interface DeadlineFilter {
    fun matches(deadline: Deadline): Boolean
}

object NoFilter : DeadlineFilter {
    override fun matches(deadline: Deadline): Boolean = true
}

object UserFilter : DeadlineFilter {
    override fun matches(deadline: Deadline): Boolean =
        deadline.owner == UserOwned
}

data class GroupFilter(val groupID: GroupID, val groupName: String) : DeadlineFilter {
    override fun matches(deadline: Deadline): Boolean =
        deadline.owner == GroupOwned(groupID)
}

class DeadlineFilterAdapter(context: Context) : BaseAdapter() {
    private val filters: MutableList<DeadlineFilter> = mutableListOf(NoFilter)

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

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
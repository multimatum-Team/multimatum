package com.github.multimatum_team.multimatum.adaptater

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.multimatum_team.multimatum.R
import com.github.multimatum_team.multimatum.model.UserGroup
import com.github.multimatum_team.multimatum.model.UserInfo
import com.github.multimatum_team.multimatum.repository.UserRepository
import kotlinx.coroutines.runBlocking

class GroupMemberAdapter(
    context: Context,
    private val userRepository: UserRepository
) : RecyclerView.Adapter<GroupMemberAdapter.GroupMemberViewHolder>() {
    private var dataSource: List<UserInfo> = listOf()

    fun setGroup(group: UserGroup) {
        dataSource = runBlocking { userRepository.fetch(group.members.toList()) }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupMemberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_group_member, parent, false)

        return GroupMemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupMemberViewHolder, position: Int) {
        val memberInfo = dataSource[position]
        holder.groupMemberName.text = memberInfo.name
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    class GroupMemberViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val groupMemberName: TextView = itemView.findViewById(R.id.group_member_name)
    }
}
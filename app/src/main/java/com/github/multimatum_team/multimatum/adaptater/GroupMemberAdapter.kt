package com.github.multimatum_team.multimatum.adaptater

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.multimatum_team.multimatum.R
import com.github.multimatum_team.multimatum.model.GroupID
import com.github.multimatum_team.multimatum.model.UserGroup
import com.github.multimatum_team.multimatum.model.UserInfo
import com.github.multimatum_team.multimatum.repository.UserRepository
import com.github.multimatum_team.multimatum.viewmodel.AuthViewModel
import com.github.multimatum_team.multimatum.viewmodel.GroupViewModel
import kotlinx.coroutines.runBlocking

class GroupMemberAdapter(
    private val context: Context,
    private val userRepository: UserRepository,
    private val authViewModel: AuthViewModel,
    private val groupViewModel: GroupViewModel
) : RecyclerView.Adapter<GroupMemberAdapter.MemberItemViewHolder>() {
    private var dataSource: MutableList<UserInfo> = mutableListOf()
    private lateinit var group: UserGroup

    fun setGroup(group: UserGroup) {
        this.group = group
        dataSource =
            runBlocking { userRepository.fetch(group.members.toList()) }
                .toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberItemViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.list_item_group_member, parent, false)
        return MemberItemViewHolder(view)
    }

    private fun showConfirmMemberRemovalDialog(memberInfo: UserInfo, position: Int) {
        AlertDialog.Builder(context)
            .setMessage(
                context.getString(
                    R.string.group_member_removal_confirmation_dialog,
                    memberInfo.name
                )
            )
            .setPositiveButton(context.getString(R.string.group_member_confirm_removal),
                DialogInterface.OnClickListener { dialog, which ->
                    groupViewModel.removeMember(group.id, memberInfo.id)
                    dataSource.removeAt(position)
                    notifyItemRemoved(position)
                    return@OnClickListener
                })
            .setNegativeButton(context.getString(R.string.group_member_cancel_removal),
                DialogInterface.OnClickListener { dialog, which ->
                    notifyItemRemoved(position + 1)
                    notifyItemRangeChanged(
                        position,
                        itemCount
                    )
                    return@OnClickListener
                })
            .show()
    }

    private fun showAttemptToRemoveOwnerErrorDialog() {
        AlertDialog.Builder(context)
            .setMessage(
                context.getString(R.string.group_member_attemt_removal_of_owner_error_dialog)
            )
            .show()
    }

    override fun onBindViewHolder(holder: MemberItemViewHolder, position: Int) {
        val memberInfo = dataSource[position]
        holder.groupMemberName.text = memberInfo.name
        val currentUserID = authViewModel.getUser().value!!.id
        if (group.owner == currentUserID) {
            holder.itemView.setOnLongClickListener {
                if (memberInfo.id == group.owner) {
                    showAttemptToRemoveOwnerErrorDialog()
                } else {
                    showConfirmMemberRemovalDialog(memberInfo, position)
                }
                true
            }
        }
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    class MemberItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val groupMemberName: TextView = itemView.findViewById(R.id.group_member_name)
    }
}
package com.github.multimatum_team.multimatum.activity

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.multimatum_team.multimatum.R
import com.github.multimatum_team.multimatum.adaptater.GroupMemberAdapter
import com.github.multimatum_team.multimatum.model.GroupID
import com.github.multimatum_team.multimatum.model.UserGroup
import com.github.multimatum_team.multimatum.repository.UserRepository
import com.github.multimatum_team.multimatum.util.hideKeyboardWhenClickingInTheVoid
import com.github.multimatum_team.multimatum.util.setOnIMEActionDone
import com.github.multimatum_team.multimatum.viewmodel.AuthViewModel
import com.github.multimatum_team.multimatum.viewmodel.GroupViewModel
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


/**
 * Classes used when you select a group in `GroupsActivity`, displaying its details, namely
 * group name, owner and members.
 * From this activity, you can invite people, delete the group if you own it, or leave it otherwise.
 * Owners can also edit the group name.
 */
@AndroidEntryPoint
class GroupDetailsActivity : AppCompatActivity() {
    lateinit var groupID: GroupID

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var userRepository: UserRepository

    private val authViewModel: AuthViewModel by viewModels()
    private val groupViewModel: GroupViewModel by viewModels()

    private lateinit var group: UserGroup

    private lateinit var groupNameView: TextInputEditText
    private lateinit var groupOwnerView: TextView
    private lateinit var groupMembersView: RecyclerView

    private lateinit var groupInviteButton: Button
    private lateinit var groupDeleteOrLeaveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_details)

        // Get the ID of the group from intent extras
        groupID = intent.getStringExtra(EXTRA_ID) as GroupID

        // Get the necessary widgets
        groupNameView = findViewById(R.id.group_details_name)
        groupOwnerView = findViewById(R.id.group_details_owner)
        groupMembersView = findViewById(R.id.group_details_members)

        groupInviteButton = findViewById(R.id.group_details_invite_button)
        groupDeleteOrLeaveButton = findViewById(R.id.group_details_delete_or_leave_button)

        // Initialize UI widgets
        initTextInput()
        initGroupMemberView()
        initLeaveButton()
    }

    /**
     * Update view when remove data changed.
     */
    private fun updateView() {
        groupNameView.focusable = if (currentUserIsGroupOwner()) {
            View.FOCUSABLE_AUTO
        } else {
            View.NOT_FOCUSABLE
        }

        groupNameView.text = SpannableStringBuilder.valueOf(group.name)
        val ownerName = runBlocking { userRepository.fetch(group.owner).name }
        groupOwnerView.text = getString(R.string.group_owner, ownerName)

        if (currentUserIsGroupOwner()) {
            initDeleteButton()
        } else {
            initLeaveButton()
        }
    }

    /**
     * Returns whether the group we are seeing is owned by the authenticated user.
     */
    private fun currentUserIsGroupOwner(): Boolean =
        group.owner == authViewModel.getUser().value!!.id

    /**
     * Setup the text input
     */
    private fun initTextInput() {
        // Adding a listener to handle the "DONE" key pressed.
        groupNameView.setOnIMEActionDone(this) { newName ->
            groupViewModel.renameGroup(groupID, newName)
        }
    }

    /**
     * Initialize group member view, setting its adapter and observing the group repository when
     * remote data changes.
     */
    private fun initGroupMemberView() {
        val adapter =
            GroupMemberAdapter(this, userRepository, authViewModel, groupViewModel)

        groupMembersView.adapter = adapter
        groupMembersView.layoutManager = LinearLayoutManager(this)

        groupViewModel.getGroups().observe(this) { groups ->
            when (groups[groupID]) {
                null -> finish()
                else -> {
                    group = groups[groupID]!!
                    adapter.setGroup(group)
                    updateView()
                }
            }
        }
    }

    /**
     * Initialize groupDeleteOrLeaveButton with the right text and leave the group when clicked.
     */
    private fun initLeaveButton() {
        groupDeleteOrLeaveButton.text = getString(R.string.group_leave)
        groupDeleteOrLeaveButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setMessage(getString(R.string.group_leave_confirmation_dialog))
                .setPositiveButton(getString(R.string.group_leave_confirm),
                    DialogInterface.OnClickListener { dialog, which ->
                        dialog.dismiss()
                        groupViewModel.removeMember(
                            groupID,
                            authViewModel.getUser().value!!.id
                        )
                        finish()
                        return@OnClickListener
                    })
                .setNegativeButton(getString(R.string.group_leave_cancel),
                    DialogInterface.OnClickListener { dialog, which ->
                        dialog.dismiss()
                        return@OnClickListener
                    })
                .show()
        }
    }

    /**
     * Initialize groupDeleteOrLeaveButton with the right text and make it delete the group when
     * clicked.
     */
    private fun initDeleteButton() {
        groupDeleteOrLeaveButton.text = getString(R.string.group_delete)
        groupDeleteOrLeaveButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setMessage(getString(R.string.group_delete_confirmation_dialog))
                .setPositiveButton(getString(R.string.group_delete_dialog_confirm),
                    DialogInterface.OnClickListener { dialog, which ->
                        dialog.dismiss()
                        groupViewModel.deleteGroup(groupID)
                        finish()
                        return@OnClickListener
                    })
                .setNegativeButton(getString(R.string.group_delete_dialog_cancel),
                    DialogInterface.OnClickListener { dialog, which ->
                        dialog.dismiss()
                        return@OnClickListener
                    })
                .show()
        }
    }

    /**
     * Intercept touch event to hide keyboard when clicking in the void.
     */
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        hideKeyboardWhenClickingInTheVoid(event)
        return super.dispatchTouchEvent(event)
    }

    companion object {
        private const val EXTRA_ID =
            "com.github.multimatum_team.group.details.id"

        // Launch an Intent to access this activity with a Deadline data
        fun newIntent(context: Context, id: GroupID): Intent {
            val detailIntent = Intent(context, GroupDetailsActivity::class.java)
            detailIntent.putExtra(EXTRA_ID, id)
            return detailIntent
        }
    }
}

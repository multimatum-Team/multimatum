package com.github.multimatum_team.multimatum.activity

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.multimatum_team.multimatum.R
import com.github.multimatum_team.multimatum.model.GroupID
import com.github.multimatum_team.multimatum.model.SignedInUser
import com.github.multimatum_team.multimatum.viewmodel.AuthViewModel
import com.github.multimatum_team.multimatum.viewmodel.GroupViewModel
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

/**
 *  Activity to accept or deny group invites.
 */
@AndroidEntryPoint
class GroupInviteActivity : AppCompatActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val groupViewModel: GroupViewModel by viewModels()

    private lateinit var inviteMessageView: TextView
    private lateinit var groupNameView: TextView
    private lateinit var acceptButton: Button
    private lateinit var denyButton: Button

    private lateinit var currentUser: SignedInUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_invite)

        inviteMessageView = findViewById(R.id.group_invite_message)
        groupNameView = findViewById(R.id.group_invite_name)
        acceptButton = findViewById(R.id.group_invite_accept)
        denyButton = findViewById(R.id.group_invite_deny)

        groupNameView.visibility = View.INVISIBLE
        acceptButton.visibility = View.INVISIBLE
        denyButton.visibility = View.INVISIBLE

        when (val user = authViewModel.getUser().value) {
            is SignedInUser -> {
                currentUser = user
                Firebase.dynamicLinks
                    .getDynamicLink(intent)
                    .addOnSuccessListener(this) { pendingDynamicLinkData ->
                        when (val link = pendingDynamicLinkData?.link) {
                            null -> initInvalidLink()
                            else -> {
                                when (val groupID = link.getQueryParameter("id")) {
                                    null -> initInvalidLink()
                                    else -> initValidLink(groupID)
                                }
                            }
                        }
                    }
                    .addOnFailureListener(this) { e ->
                        initInvalidLink()
                    }
            }
            else -> initSignInRequirementMessage()
        }
    }

    private fun initSignInRequirementMessage() {
        inviteMessageView.text = getString(R.string.group_invite_message_must_be_signed_in)
    }

    private fun initInvalidLink() {
        inviteMessageView.text = getString(R.string.group_invite_message_invalid)
    }

    private fun initValidLink(groupID: GroupID) {
        groupViewModel.fetchNewGroup(groupID) { group ->
            if (group == null) {
                inviteMessageView.text = getString(R.string.group_invite_message_invalid)
            } else {
                if (group.members.contains(currentUser.id)) {
                    inviteMessageView.text =
                        getString(R.string.group_invite_message_already_member_of_this_group)
                } else {
                    inviteMessageView.text =
                        getString(R.string.group_invite_message_you_have_been_invited_to_join)
                    groupNameView.text = group.name
                    groupNameView.visibility = View.VISIBLE
                    acceptButton.visibility = View.VISIBLE
                    denyButton.visibility = View.VISIBLE
                    acceptButton.setOnClickListener {
                        groupViewModel.addMember(groupID, currentUser.id)
                        val intent = GroupDetailsActivity.newIntent(this, groupID)
                        startActivity(intent)
                        finish()
                    }
                    denyButton.setOnClickListener { finish() }
                }
            }
        }
    }
}
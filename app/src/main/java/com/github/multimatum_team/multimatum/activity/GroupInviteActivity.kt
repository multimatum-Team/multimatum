package com.github.multimatum_team.multimatum.activity

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.multimatum_team.multimatum.LogUtil
import com.github.multimatum_team.multimatum.R
import com.github.multimatum_team.multimatum.model.GroupID
import com.github.multimatum_team.multimatum.model.SignedInUser
import com.github.multimatum_team.multimatum.model.UserGroup
import com.github.multimatum_team.multimatum.viewmodel.AuthViewModel
import com.github.multimatum_team.multimatum.viewmodel.GroupViewModel
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 *  Activity to accept or deny group invites.
 */
@AndroidEntryPoint
class GroupInviteActivity : AppCompatActivity() {
    @Inject
    lateinit var dynamicLinks: FirebaseDynamicLinks

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

        LogUtil.debugLog(intent.toString())

        inviteMessageView = findViewById(R.id.group_invite_message)
        groupNameView = findViewById(R.id.group_invite_name)
        acceptButton = findViewById(R.id.group_invite_accept)
        denyButton = findViewById(R.id.group_invite_deny)

        groupNameView.visibility = View.INVISIBLE
        acceptButton.visibility = View.INVISIBLE
        denyButton.visibility = View.INVISIBLE

        initView()
    }

    /**
     * Initialize the activity view, depending on whether the user is signed-in with an actual
     * account or anonymously (in which case the group feature is unavailable).
     */
    private fun initView() {
        when (val user = authViewModel.getUser().value) {
            is SignedInUser -> initSignedInUser(user)
            else -> initSignInRequirementMessage()
        }
    }

    /**
     * The user is signed-in, which is the first requirement to join a group.
     * We further check that the clicked link is valid.
     */
    private fun initSignedInUser(user: SignedInUser) {
        currentUser = user
        dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                when (val link = pendingDynamicLinkData?.link) {
                    null -> initInvalidLinkOrMissingGroupID()
                    else -> {
                        when (val groupID = link.getQueryParameter("id")) {
                            null -> initInvalidLinkOrMissingGroupID()
                            else -> initValidLink(groupID)
                        }
                    }
                }
            }
    }

    /**
     * The user clicked on the invite link while being signed-in anonymously which is disallowed,
     * group members must be signed-in.
     * We therefore show an error message in this case.
     */
    private fun initSignInRequirementMessage() {
        inviteMessageView.text = getString(R.string.group_invite_message_must_be_signed_in)
    }

    /**
     * The clicked link has the right host and path, but invalid or missing group ID, we proceed to
     * show an error message.
     */
    private fun initInvalidLinkOrMissingGroupID() {
        inviteMessageView.text = getString(R.string.group_invite_message_invalid)
    }

    /**
     * The link was valid but the user is already a member of the group, so it does not make sense
     * to accept the invitation.
     */
    private fun initUserIsAlreadyMember() {
        inviteMessageView.text =
            getString(R.string.group_invite_message_already_member_of_this_group)
    }

    /**
     * Happy path: all requirements were fulfilled: the user is signed-in, the link is valid and
     * they're not in the group.
     */
    private fun initInvitationPrompt(group: UserGroup) {
        inviteMessageView.text =
            getString(R.string.group_invite_message_you_have_been_invited_to_join)
        groupNameView.text = group.name
        groupNameView.visibility = View.VISIBLE
        acceptButton.visibility = View.VISIBLE
        denyButton.visibility = View.VISIBLE
        acceptButton.setOnClickListener {
            groupViewModel.addMember(group.id, currentUser.id)
            val intent = GroupDetailsActivity.newIntent(this, group.id)
            startActivity(intent)
            finish()
        }
        denyButton.setOnClickListener { finish() }
    }

    /**
     * The user is signed-in and the link is valid, we further check that the ID in the link query
     * corresponds to an existing group ID.
     */
    private fun initValidLink(groupID: GroupID) {
        groupViewModel.fetchNewGroup(groupID) { group ->
            if (group == null) {
                initInvalidLinkOrMissingGroupID()
            } else {
                if (group.members.contains(currentUser.id)) {
                    initUserIsAlreadyMember()
                } else {
                    initInvitationPrompt(group)
                }
            }
        }
    }
}
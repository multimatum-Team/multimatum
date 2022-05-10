package com.github.multimatum_team.multimatum.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Rect
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.multimatum_team.multimatum.R
import com.github.multimatum_team.multimatum.adaptater.GroupMemberAdapter
import com.github.multimatum_team.multimatum.model.GroupID
import com.github.multimatum_team.multimatum.model.UserGroup
import com.github.multimatum_team.multimatum.repository.UserRepository
import com.github.multimatum_team.multimatum.viewmodel.GroupViewModel
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Classes used when you select a group in `GroupsActivity`, displaying its details, namely
 * group name, owner and members.
 */
@AndroidEntryPoint
class GroupDetailsActivity : AppCompatActivity() {
    lateinit var id: GroupID

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var userRepository: UserRepository

    private val groupViewModel: GroupViewModel by viewModels()

    private lateinit var group: UserGroup

    private lateinit var groupNameView: TextInputEditText
    private lateinit var groupOwnerView: TextView
    private lateinit var groupMembersView: RecyclerView

    private lateinit var adapter: GroupMemberAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_details)

        adapter = GroupMemberAdapter(this, userRepository)

        // Get the necessary widgets
        groupNameView = findViewById(R.id.group_details_name)
        groupOwnerView = findViewById(R.id.group_details_owner)
        groupMembersView = findViewById(R.id.group_details_members)

        groupMembersView.adapter = adapter
        groupMembersView.layoutManager = LinearLayoutManager(this)


        initTextInput()

        // Get the ID of the group from intent extras
        id = intent.getStringExtra(EXTRA_ID) as GroupID

        groupViewModel.getGroups().observe(this){groups ->
            group = groups[id]!!
            adapter.setGroup(group)
            updateView()
        }
    }

    private fun updateView() {
        groupNameView.text = SpannableStringBuilder.valueOf(group.name)
        val ownerName = runBlocking { userRepository.fetch(group.owner).name }
        groupOwnerView.text = getString(R.string.group_owner, ownerName)
    }

    /**
     * This function allows the user to rename the group directly, using the "ENTER" key (more intuitive).
     */
    private fun initTextInput() {
        // Adding a listener to handle the "ENTER" key pressed.
        groupNameView.setOnKeyListener { v, keycode, event ->
            if ((keycode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                val newName = groupNameView.text.toString()
                groupViewModel.renameGroup(id, newName)
                // The listener has consumed the event
                return@setOnKeyListener true
            }
            false
        }

        // Adding a listener to handle the "DONE" key pressed.
        groupNameView.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val newName = groupNameView.text.toString()
                groupViewModel.renameGroup(id, newName)
                // The listener has consumed the event
                return@OnEditorActionListener true
            }
            false
        })
    }

    /*
    This function allows the user to exit the text input intuitively, just by clicking outside
     */
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            // We are in the case were the user has touched outside
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    // If the user has touched a place outside the keyboard, remove the focus and keyboard
                    v.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
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

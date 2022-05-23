package com.github.multimatum_team.multimatum.activity

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Toast
import androidx.activity.viewModels
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.github.multimatum_team.multimatum.AlertDialogBuilderProducer
import com.github.multimatum_team.multimatum.GroupViewModelProducer
import com.github.multimatum_team.multimatum.R
import com.github.multimatum_team.multimatum.adaptater.UserGroupAdapter
import com.github.multimatum_team.multimatum.repository.UserRepository
import com.github.multimatum_team.multimatum.viewmodel.GroupViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GroupsActivity : AppCompatActivity() {
    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var alertDialogBuilderProducer: AlertDialogBuilderProducer

    @Inject
    lateinit var groupViewModelProducer: GroupViewModelProducer

    private val defaultGroupViewModel: GroupViewModel by viewModels()
    private lateinit var groupViewModel: GroupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groups)

        groupViewModel = groupViewModelProducer.produce(defaultGroupViewModel)

        val listView = findViewById<ListView>(R.id.listViewGroups)

        val adapter = UserGroupAdapter(this, groupViewModel, userRepository)

        // Set when you maintain your finger on an item of the list, launch the detail activity
        listView.setOnItemClickListener { _, _, position, _ ->
            val group = adapter.getItem(position)
            val detailIntent = GroupDetailsActivity.newIntent(this, group.id)
            startActivity(detailIntent)
            // Last line necessary to use this function
            true
        }

        listView.adapter = adapter
        groupViewModel.getGroups().observe(this) { groups ->
            adapter.setGroups(groups)
        }
    }

    fun addGroup(view: View) {
        val alertDialogBuilder = alertDialogBuilderProducer.produce(this)
        alertDialogBuilder.setTitle("Create group")
        alertDialogBuilder.setMessage("Group name :")

        //add editable text box for group name
        val input = EditText(this@GroupsActivity)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        input.layoutParams = lp
        alertDialogBuilder.setView(input)

        //add cancel and create button
        alertDialogBuilder.setPositiveButton("Create") { dialog, _ ->
            createGroup(dialog, input)
        }
        alertDialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        alertDialogBuilder.create()
        alertDialogBuilder.show()
    }

    private fun createGroup(dialog: DialogInterface, editText: EditText) {
        groupViewModel.createGroup(editText.text.toString()) { groupID ->
            Toast.makeText(this, "Group created", Toast.LENGTH_SHORT).show()
            Log.d("passBy", "Toast created")
            intent = GroupDetailsActivity.newIntent(this, groupID)
            startActivity(intent)
        }

    }
}

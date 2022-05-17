package com.github.multimatum_team.multimatum.activity

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.github.multimatum_team.multimatum.R
import com.github.multimatum_team.multimatum.adaptater.UserGroupAdapter
import com.github.multimatum_team.multimatum.repository.UserRepository
import com.github.multimatum_team.multimatum.viewmodel.GroupViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GroupsActivity : AppCompatActivity() {
    @Inject
    lateinit var userRepository: UserRepository

    private val groupViewModel: GroupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groups)

        val listView = findViewById<ListView>(R.id.listViewGroups)

        val adapter = UserGroupAdapter(this, groupViewModel, userRepository)

        val button = findViewById<FloatingActionButton>(R.id.addGroupButton)
        button.setOnClickListener{ view ->
            addGroup(button)
        }

        // Set when you maintain your finger on an item of the list, launch the detail activity
        listView.setOnItemClickListener { _, _, position, _ ->
            val group = adapter.getItem(position)
            val detailIntent = GroupDetailsActivity.newIntent(this, group.id)
            startActivity(detailIntent)
            // Last line necessary to use this function
            true
        }

        listView.adapter = adapter
        groupViewModel.getGroups().observe(this){groups ->
            adapter.setGroups(groups)
        }
    }

    private fun addGroup(view: View){
        val alertDialogBuilder = AlertDialog.Builder(this)
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
        alertDialogBuilder.setCancelable(false)
        alertDialogBuilder.setPositiveButton("Create", DialogInterface.OnClickListener { dialog, _ ->
                createGroup(dialog, input)
            })
        alertDialogBuilder.setNegativeButton("Cancel", DialogInterface.OnClickListener{
            dialog, _ -> dialog.cancel()
        })

        alertDialogBuilder.create()
        alertDialogBuilder.show()
    }

    private fun createGroup(dialog: DialogInterface, editText: EditText) {
        groupViewModel.createGroup(editText.text.toString()) { groupID ->
            GroupDetailsActivity.newIntent(this, groupID)
            startActivity(intent)
        }
    }
}

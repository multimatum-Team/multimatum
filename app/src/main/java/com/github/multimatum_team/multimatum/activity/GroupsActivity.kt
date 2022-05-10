package com.github.multimatum_team.multimatum.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import androidx.activity.viewModels
import com.github.multimatum_team.multimatum.LogUtil
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

    private val groupViewModel: GroupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groups)

        val listView = findViewById<ListView>(R.id.listViewGroups)

        val adapter = UserGroupAdapter(this, groupViewModel, userRepository)

        // Set when you maintain your finger on an item of the list, launch the detail activity
        listView.setOnItemLongClickListener { _, _, position, _ ->
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
}
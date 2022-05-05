package com.github.multimatum_team.multimatum.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import androidx.activity.viewModels
import com.github.multimatum_team.multimatum.LogUtil
import com.github.multimatum_team.multimatum.R
import com.github.multimatum_team.multimatum.adaptater.UserGroupAdapter
import com.github.multimatum_team.multimatum.viewmodel.GroupViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupsActivity : AppCompatActivity() {
    private val groupViewModel: GroupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groups)

        val listView = findViewById<ListView>(R.id.listViewGroups)

        val adapter = UserGroupAdapter(this, groupViewModel)


        listView.adapter = adapter
        groupViewModel.getGroups().observe(this){groups ->
            adapter.setGroups(groups)
        }

        //LogUtil.debugLog()
    }
}
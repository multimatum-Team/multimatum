package com.github.multimatum_team.multimatum.repository

import com.github.multimatum_team.multimatum.model.Deadline
import com.google.android.gms.tasks.Task
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

/**
 * Remote Firebase repository for storing deadlines.
 */
class FirebaseDeadlineRepository : DeadlineRepository {
    private var database: FirebaseDatabase = FirebaseDatabase.getInstance()

    private val deadlinesRef
        get() = database.getReference("deadlines")

    override fun fetchAll(): Task<List<Deadline>> =
        deadlinesRef.orderByChild("date").get().continueWith { task ->
            if (task.isSuccessful) {
                task.result.value as List<Deadline>
            } else {
                listOf()
            }
        }

    override fun put(deadline: Deadline): Task<Unit> =
        deadlinesRef.push().setValue(deadline).continueWith { }
}
package com.github.multimatum_team.multimatum.repository

import com.github.multimatum_team.multimatum.model.Deadline
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import java.time.format.DateTimeFormatter

/**
 * Remote Firebase repository for storing deadlines.
 */
class FirebaseDeadlineRepository : DeadlineRepository {
    private var database: FirebaseDatabase = FirebaseDatabase.getInstance()

    private val deadlinesRef
        get() = database.getReference("deadlines")

    /**
     * Fetch all deadlines from the database.
     */
    override fun fetchAll(): List<Deadline> =
        Tasks.await(deadlinesRef.orderByChild("date").get().onSuccessTask { task ->
            Tasks.forResult(task.children.map { deadlineSnapshot ->
                deadlineSnapshot.getValue(object : GenericTypeIndicator<Deadline>() {})!!
            })
        })

    /**
     * Insert new deadline in the database.
     */
    override fun put(deadline: Deadline) {
        val deadlineMap = HashMap<String, Any>()
        deadlineMap["title"] = deadline.title
        deadlineMap["state"] = deadline.state
        deadlineMap["date"] = deadline.date.format(DateTimeFormatter.ISO_DATE)
        deadlinesRef.push().setValue(deadline)
    }
}
package com.github.multimatum_team.multimatum.repository

import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

/**
 * Remote Firebase repository for storing deadlines.
 */
class FirebaseDeadlineRepository @Inject constructor() : DeadlineRepository {
    private var database: FirebaseFirestore = Firebase.firestore

    private val deadlinesRef = database.collection("deadlines")

    /**
     * Fetch all deadlines from the database.
     */
    override suspend fun fetchAll(): List<Deadline> =
        deadlinesRef
            .get()
            .await()
            .documents
            .map { deadlineSnapshot ->
                val title = deadlineSnapshot["title"] as String
                val state = DeadlineState.values()[(deadlineSnapshot["state"] as Long).toInt()]
                val timestamp = deadlineSnapshot["date"] as Timestamp
                val milliseconds = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
                val date = Instant
                    .ofEpochMilli(milliseconds)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                Deadline(title, state, date)
            }

    /**
     * Insert new deadline in the database.
     */
    override suspend fun put(deadline: Deadline) {
        deadlinesRef.add(
            hashMapOf(
                "title" to deadline.title,
                "state" to deadline.state.ordinal,
                "date" to deadline.date.toEpochDay()
            )
        ).await()
    }
}
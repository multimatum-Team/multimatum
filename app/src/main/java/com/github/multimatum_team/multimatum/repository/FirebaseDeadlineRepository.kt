package com.github.multimatum_team.multimatum.repository

import android.util.Log
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoField
import javax.inject.Inject

/**
 * Remote Firebase repository for storing deadlines.
 */
class FirebaseDeadlineRepository @Inject constructor() : DeadlineRepository {
    private var database: FirebaseFirestore = Firebase.firestore

    private val deadlinesRef = database.collection("deadlines")

    private fun serializeDeadline(deadline: Deadline): HashMap<String, *> =
        hashMapOf(
            "title" to deadline.title,
            "state" to deadline.state.ordinal,
            "date" to Timestamp(
                deadline.date
                    .atStartOfDay(ZoneId.systemDefault())
                    .getLong(ChronoField.INSTANT_SECONDS),
                0
            )
        )

    private fun deserializeDeadline(deadlineSnapshot: DocumentSnapshot): Deadline {
        val title = deadlineSnapshot["title"] as String
        val state = DeadlineState.values()[(deadlineSnapshot["state"] as Long).toInt()]
        val timestamp = deadlineSnapshot["date"] as Timestamp
        val milliseconds = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
        val date = Instant
            .ofEpochMilli(milliseconds)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return Deadline(title, state, date)
    }

    /**
     * Fetch all deadlines from the database.
     */
    override suspend fun fetchAll(): List<Deadline> =
        deadlinesRef
            .get()
            .await()
            .documents
            .map { deserializeDeadline(it) }

    /**
     * Insert new deadline in the database.
     */
    override suspend fun put(deadline: Deadline) {
        deadlinesRef
            .add(serializeDeadline(deadline))
            .await()
    }

    /**
     * Add listener for firebase updates.
     */
    override fun onUpdate(callback: (List<Deadline>) -> Unit) {
        deadlinesRef
            .addSnapshotListener(EventListener<QuerySnapshot> { deadlineSnapshots, error ->
                if (error != null) {
                    Log.w("FirebaseDeadlineRepository", "Failed to retrieve data from database")
                    return@EventListener
                }

                val deadlineList: MutableList<Deadline> = mutableListOf()
                deadlineSnapshots!!.forEach { deadlineSnapshot ->
                    deadlineList.add(deserializeDeadline(deadlineSnapshot))
                }
                callback(deadlineList)
            })
    }
}
package com.github.multimatum_team.multimatum.repository

import android.util.Log
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
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
class FirebaseDeadlineRepository @Inject constructor() : DeadlineRepository() {
    private var database: FirebaseFirestore = Firebase.firestore

    private val deadlinesRef = database
        .collection("deadlines")

    /**
     * Convert a deadline into a hashmap, so that we can send the deadline data to Firebase.
     */
    private fun serializeDeadline(deadline: Deadline): HashMap<String, *> =
        hashMapOf(
            "title" to deadline.title,
            "state" to deadline.state.ordinal,
            "date" to Timestamp(
                deadline.dateTime
                    .atZone(ZoneId.systemDefault())
                    .toEpochSecond(),
                0
            ),
            "owner" to _user.id,
            "description" to deadline.description,
            "notificationsTimes" to deadline.notificationsTimes.toList()
        )

    /**
     * Convert a document snapshot from Firebase to a Deadline instance.
     */
    private fun deserializeDeadline(deadlineSnapshot: DocumentSnapshot): Deadline {
        val title = deadlineSnapshot["title"] as String
        val state = DeadlineState.values()[(deadlineSnapshot["state"] as Long).toInt()]
        val timestamp = deadlineSnapshot["date"] as Timestamp
        val milliseconds = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
        val date = Instant
            .ofEpochMilli(milliseconds)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        var description = ""
        if (deadlineSnapshot["description"] != null) {
            description = deadlineSnapshot["description"] as String
        }
        var notificationsTimes = ArrayList<Long>()
        if (deadlineSnapshot["notificationsTimes"] != null) {
            notificationsTimes = deadlineSnapshot["notificationsTimes"] as ArrayList<Long>
        }

        return Deadline(
            title,
            state,
            date,
            description = description,
            notificationsTimes = notificationsTimes
        )
    }

    /**
     * Fetch a single deadline given its unique ID.
     */
    override suspend fun fetch(id: DeadlineID): Deadline =
        deserializeDeadline(deadlinesRef.document(id).get().await())

    /**
     * Fetch all deadlines from the database.
     */
    override suspend fun fetchAll(): Map<DeadlineID, Deadline> =
        deadlinesRef
            .whereEqualTo("owner", _user.id)
            .get()
            .await()
            .documents
            .associate { it.id to deserializeDeadline(it) }

    /**
     * Insert new deadline in the database.
     */
    override suspend fun put(deadline: Deadline): DeadlineID =
        deadlinesRef
            .add(serializeDeadline(deadline))
            .await()
            .id

    /**
     * Change existing deadline value in the database.
     */
    override suspend fun modify(id: DeadlineID, newDeadline: Deadline) {
        deadlinesRef
            .document(id)
            .set(serializeDeadline(newDeadline))
    }

    /**
     * Remove a deadline from the Firebase database.
     */
    override suspend fun delete(id: DeadlineID) {
        deadlinesRef
            .document(id)
            .delete()
            .await()
    }

    /**
     * Add listener for firebase updates.
     */
    override fun onUpdate(callback: (Map<DeadlineID, Deadline>) -> Unit) {
        deadlinesRef
            .whereEqualTo("owner", _user.id)
            .addSnapshotListener { deadlineSnapshots, error ->
                if (error != null) {
                    Log.w("FirebaseDeadlineRepository", "Failed to retrieve data from database")
                    return@addSnapshotListener
                }
                val deadlineMap: MutableMap<DeadlineID, Deadline> = mutableMapOf()
                deadlineSnapshots!!.forEach { deadlineSnapshot ->
                    deadlineMap[deadlineSnapshot.id] = deserializeDeadline(deadlineSnapshot)
                }
                callback(deadlineMap)
            }
    }
}
package com.github.multimatum_team.multimatum.repository

import com.github.multimatum_team.multimatum.model.Deadline
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.time.format.DateTimeFormatter
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
            .toObjects(Deadline::class.java)

    /**
     * Insert new deadline in the database.
     */
    override suspend fun put(deadline: Deadline) {
        deadlinesRef.add(hashMapOf(
            "title" to deadline.title,
            "state" to deadline.state.ordinal,
            "date" to deadline.date.toEpochDay()
        )).await()
    }
}
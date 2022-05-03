package com.github.multimatum_team.multimatum.repository

import android.util.Log
import com.github.multimatum_team.multimatum.model.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
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

    private val updateCallbacks: MutableList<(Map<DeadlineID, Deadline>) -> Unit> = mutableListOf()

    private lateinit var registeredListener: ListenerRegistration

    private val userOwnedOwnerList: List<Any>
        get() = listOf(_user.id, mapOf("type" to "user", "id" to _user.id))

    private val ownerList: List<Any>
        get() {
            val owners: MutableList<Any> = userOwnedOwnerList.toMutableList()
            for (groupID in _groups.keys) {
                owners.add(mapOf("type" to "group", "id" to groupID))
            }
            return owners
        }

    private val allDeadlinesQuery: Query
        get() = deadlinesRef.whereIn("owner", ownerList)


    private fun addListenerForQuery(query: Query): ListenerRegistration =
        query.addSnapshotListener { deadlineSnapshots, error ->
            if (error != null) {
                Log.w("FirebaseDeadlineRepository", "Failed to retrieve data from database")
                return@addSnapshotListener
            }
            val deadlineMap: MutableMap<DeadlineID, Deadline> = mutableMapOf()
            deadlineSnapshots!!.forEach { deadlineSnapshot ->
                deadlineMap[deadlineSnapshot.id] = deserializeDeadline(deadlineSnapshot)
            }
            for (callback in updateCallbacks) {
                callback(deadlineMap)
            }
        }

    override fun setUser(newUser: User) {
        super.setUser(newUser)
        if (this::registeredListener.isInitialized) {
            registeredListener.remove()
        }
        registeredListener = addListenerForQuery(allDeadlinesQuery)
    }

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
            "owner" to when (deadline.owner) {
                is UserOwned -> hashMapOf("type" to "user", "id" to _user.id)
                is GroupOwned -> hashMapOf("type" to "group", "id" to deadline.owner.groupID)
            },
            "description" to deadline.description
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
        val description = deadlineSnapshot["description"] as String
        val owner = when (deadlineSnapshot["owner"]) {
            is String -> UserOwned
            is Map<*, *> -> {
                val ownerMap = deadlineSnapshot["owner"] as Map<String, String>
                when (ownerMap["type"]) {
                    "user" -> UserOwned
                    "group" -> GroupOwned(ownerMap["id"] as String)
                    else -> throw IllegalArgumentException("provided serialized deadline has ill-formed owner type, expected \"user\" or \"group\"")
                }
            }
            else -> throw IllegalArgumentException("provided serialized deadline has ill-formed owner type, expected String or Map")
        }

        return Deadline(
            title,
            state,
            date,
            description = description,
            owner
        )
    }

    /**
     * Fetch a single deadline given its unique ID.
     */
    override suspend fun fetch(id: DeadlineID): Deadline =
        deserializeDeadline(deadlinesRef.document(id).get().await())

    private suspend fun fetchQuery(query: Query): Map<DeadlineID, Deadline> =
        query
            .get()
            .await()
            .documents
            .associate { it.id to deserializeDeadline(it) }

    /**
     * Fetch all personal deadlines from the database.
     */
    private suspend fun fetchPersonal(): Map<DeadlineID, Deadline> = fetchQuery(
        deadlinesRef.whereIn("owner", userOwnedOwnerList)
    )

    /**
     * Fetch all personal deadlines from the database.
     */
    private suspend fun fetchFromGroup(groupID: GroupID): Map<DeadlineID, Deadline> = fetchQuery(
        deadlinesRef
            .whereEqualTo(FieldPath.of("owner", "type"), "group")
            .whereEqualTo(FieldPath.of("owner", "id"), groupID)
    )

    /**
     * Fetch all deadlines associated to the given owner.
     */
    override suspend fun fetchFromOwner(owner: DeadlineOwner): Map<DeadlineID, Deadline> =
        when (owner) {
            is UserOwned -> fetchPersonal()
            is GroupOwned -> fetchFromGroup(owner.groupID)
        }

    /**
     * Fetch all deadlines from the database.
     */
    override suspend fun fetchAll(): Map<DeadlineID, Deadline> =
        fetchQuery (allDeadlinesQuery)

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
        updateCallbacks.add(callback)
    }
}
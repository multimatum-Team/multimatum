package com.github.multimatum_team.multimatum.repository

import android.util.Log
import com.github.multimatum_team.multimatum.LogUtil
import com.github.multimatum_team.multimatum.model.*
import com.github.multimatum_team.multimatum.util.associateNotNull
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

/**
 * Remote Firebase repository for storing deadlines.
 */
class FirebaseDeadlineRepository @Inject constructor(database: FirebaseFirestore) :
    DeadlineRepository() {
    private val deadlinesRef = database
        .collection("deadlines")

    private val updateCallbacks: MutableList<(Map<DeadlineID, Deadline>) -> Unit> = mutableListOf()

    private lateinit var registeredListener: ListenerRegistration

    private val userOwnerField: Map<String, String>
        get() = mapOf("type" to "user", "id" to _user.id)

    private val ownerFieldList: List<Map<String, String>>
        get() {
            val owners: MutableList<Map<String, String>> = mutableListOf(userOwnerField)
            for (groupID in _groups.keys) {
                owners.add(mapOf("type" to "group", "id" to groupID))
            }
            return owners
        }

    private val allDeadlinesQuery: Query
        get() = deadlinesRef.whereIn("owner", ownerFieldList)


    private fun addListenerForQuery(query: Query): ListenerRegistration =
        query.addSnapshotListener { deadlineSnapshots, error ->
            if (error != null) {
                Log.w("FirebaseDeadlineRepository", "Failed to retrieve data from database")
                return@addSnapshotListener
            }
            val deadlineMap = deadlineSnapshots!!
                .toList()
                .associateNotNull { it.id to deserializeDeadline(it) }
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
            "description" to deadline.description,
            "owner" to when (deadline.owner) {
                is UserOwned -> hashMapOf("type" to "user", "id" to _user.id)
                is GroupOwned -> hashMapOf("type" to "group", "id" to deadline.owner.groupID)
            },
            "pdfPath" to deadline.pdfPath,
            "locationName" to deadline.locationName,
            "location" to deadline.location
        )

    /**
     * Convert a document snapshot from Firebase to a Deadline instance.
     */
    private fun deserializeDeadline(deadlineSnapshot: DocumentSnapshot): Deadline? {
        val title = when (val title = deadlineSnapshot["title"]) {
            is String -> title
            else -> {
                LogUtil.warningLog("Missing or ill-formed field \"title\"")
                return null
            }
        }
        val stateIndex = when (val stateIndex = deadlineSnapshot["state"]) {
            is Long ->
                if (0 <= stateIndex && stateIndex < DeadlineState.values().size) {
                    stateIndex.toInt()
                } else {
                    LogUtil.warningLog("Value of the \"state\" field is out of range")
                    return null
                }
            else -> {
                LogUtil.warningLog("Missing or ill-formed field \"state\"")
                return null
            }
        }
        val state = DeadlineState.values()[stateIndex]
        val timestamp = when (val timestamp = deadlineSnapshot["date"]) {
            is Timestamp -> timestamp
            else -> {
                LogUtil.warningLog("Missing or ill-formed field \"date\"")
                return null
            }
        }
        val milliseconds = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
        val date =
            Instant.ofEpochMilli(milliseconds).atZone(ZoneId.systemDefault()).toLocalDateTime()
        val description = when (val description = deadlineSnapshot["description"]) {
            is String -> description
            else -> {
                LogUtil.warningLog("Missing or ill-formed field \"description\"")
                return null
            }
        }
        val ownerMap = when (val ownerMap = deadlineSnapshot["owner"]) {
            is Map<*, *> -> ownerMap
            else -> {
                LogUtil.warningLog("Missing or ill-formed field \"owner\"")
                return null
            }
        }
        val owner = when (ownerMap["type"] ?: return null) {
            "user" -> UserOwned
            "group" -> when (val groupID = ownerMap["id"]) {
                is String -> GroupOwned(groupID)
                else -> {
                    LogUtil.warningLog("Missing or ill-formed field \"id\" of owner map")
                    return null
                }
            }
            else -> {
                LogUtil.warningLog("Missing or ill-formed field \"type\" of owner map")
                return null
            }
        }
        val pdfPath = when (val pdfPath = deadlineSnapshot["pdfPath"]) {
            is String -> pdfPath
            else -> {
                LogUtil.warningLog("Missing or ill-formed field \"pdfPath\"")
                return null
            }
        }
        val locationName = when (val locationName = deadlineSnapshot["locationName"]) {
            is String? -> locationName
            else -> {
                LogUtil.warningLog("Missing or ill-formed field \"locationName\"")
                return null
            }
        }
        val location = when (val location = deadlineSnapshot["location"]) {
            is GeoPoint? -> location
            else -> {
                LogUtil.warningLog("Missing or ill-formed field \"location\"")
                return null
            }
        }
        return Deadline(
            title, state, date, description, owner,
            pdfPath = pdfPath,
            locationName = locationName,
            location = location
        )
    }

    /**
     * Fetch a single deadline given its unique ID.
     */
    override suspend fun fetch(id: DeadlineID): Deadline? =
        deserializeDeadline(deadlinesRef.document(id).get().await())

    private suspend fun fetchQuery(query: Query): Map<DeadlineID, Deadline> =
        query
            .get()
            .await()
            .documents
            .associateNotNull { it.id to deserializeDeadline(it) }

    /**
     * Fetch all personal deadlines from the database.
     */
    private suspend fun fetchPersonal(): Map<DeadlineID, Deadline> = fetchQuery(
        deadlinesRef.whereEqualTo("owner", userOwnerField)
    )

    /**
     * Fetch all personal deadlines from the database.
     */
    private suspend fun fetchFromGroup(groupID: GroupID): Map<DeadlineID, Deadline> = fetchQuery(
        deadlinesRef.whereEqualTo("owner", mapOf("type" to "group", "id" to groupID))
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
        fetchQuery(allDeadlinesQuery)

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
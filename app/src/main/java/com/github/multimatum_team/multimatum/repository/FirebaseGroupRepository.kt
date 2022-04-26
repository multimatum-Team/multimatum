package com.github.multimatum_team.multimatum.repository

import android.util.Log
import com.github.multimatum_team.multimatum.model.GroupID
import com.github.multimatum_team.multimatum.model.UserGroup
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

/**
 * Remote Firebase repository for storing user groups.
 */
class FirebaseGroupRepository : GroupRepository() {
    private var database: FirebaseFirestore = Firebase.firestore

    private val groupsRef = database
        .collection("groups")

    /**
     * Convert a group into a hashmap, so that we can send the group data to Firebase.
     */
    private fun serializeGroup(group: UserGroup): HashMap<String, *> =
        hashMapOf(
            "name" to group.name,
            "owner" to group.owner,
            "members" to group.members.toList()
        )

    /**
     * Convert a document snapshot from Firebase to a UserGroup instance.
     */
    private fun deserializeGroup(groupSnapshot: DocumentSnapshot): UserGroup =
        UserGroup(
            id = groupSnapshot.id,
            name = groupSnapshot["name"] as String,
            owner = groupSnapshot["owner"] as String,
            members = (groupSnapshot["members"] as List<String>).toSet()
        )

    /**
     * Fetch a single deadline given its unique ID.
     * @param id the ID of the group to fetch
     */
    override suspend fun fetch(id: GroupID): UserGroup =
        deserializeGroup(groupsRef.document(id).get().await())

    /**
     * Fetch all groups from the database.
     */
    override suspend fun fetchAll(): Map<GroupID, UserGroup> =
        groupsRef
            .whereArrayContains("members", _user.id)
            .get()
            .await()
            .documents
            .associate { it.id to deserializeGroup(it) }

    /**
     * Create a new group in the database.
     * @param name the name of the new group to be created
     */
    override suspend fun create(name: String): GroupID =
        groupsRef
            .add(
                serializeGroup(
                    UserGroup(
                        id = "",
                        name = name,
                        owner = _user.id
                    )
                )
            )
            .await()
            .id

    /**
     * Remove a group from the Firebase database.
     * @param id the ID of the group to delete
     */
    override suspend fun delete(id: GroupID) {
        groupsRef
            .document(id)
            .delete()
            .await()
    }

    /**
     * Add listener for database updates.
     * @param callback the callback to run when the groups of the current user changes.
     */
    override fun onUpdate(callback: (Map<GroupID, UserGroup>) -> Unit) {
        groupsRef
            .whereArrayContains("members", _user.id)
            .addSnapshotListener { groupSnapshots, error ->
                if (error != null) {
                    Log.w("FirebaseDeadlineRepository", "Failed to retrieve data from database")
                    return@addSnapshotListener
                }
                val groupMap: MutableMap<GroupID, UserGroup> = mutableMapOf()
                groupSnapshots!!.forEach { groupSnapshot ->
                    groupMap[groupSnapshot.id] = deserializeGroup(groupSnapshot)
                }
                callback(groupMap)
            }
    }
}
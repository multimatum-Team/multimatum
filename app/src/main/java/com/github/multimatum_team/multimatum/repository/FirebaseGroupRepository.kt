package com.github.multimatum_team.multimatum.repository

import android.util.Log
import com.github.multimatum_team.multimatum.model.GroupID
import com.github.multimatum_team.multimatum.model.UserGroup
import com.github.multimatum_team.multimatum.model.UserID
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Remote Firebase repository for storing user groups.
 */
class FirebaseGroupRepository @Inject constructor(database: FirebaseFirestore) : GroupRepository() {
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
            .whereArrayContains("members", _userID)
            .get()
            .await()
            .documents
            .associate { it.id to deserializeGroup(it) }

    /**
     * Fetch all groups from the database that are owned by the current user.
     */
    override suspend fun fetchOwned(): Map<GroupID, UserGroup> =
        groupsRef
            .whereEqualTo("owner", _userID)
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
                        owner = _userID
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
     * Rename a group.
     * @param id the ID of the group to rename
     * @param newName the new name of the group
     */
    override suspend fun rename(id: GroupID, newName: String) {
        groupsRef
            .document(id)
            .update("name", newName)
            .await()
    }

    /**
     * Invite an user to join a group.
     * @param id the ID of the grop to which we want to invite the user
     * @param email the email of the user to invite
     */
    override suspend fun invite(id: GroupID, email: String) {
        throw NotImplementedError("FirebaseGroupRepository.invite is not implemented")
    }

    /**
     * Kick a user from a group.
     * @param groupID the group from which to kick the user
     * @param memberID the ID of the group member to kick
     */
    override suspend fun removeMember(groupID: GroupID, memberID: UserID) {
        groupsRef
            .document(groupID)
            .update("members", FieldValue.arrayRemove(memberID))
            .await()
    }

    /**
     * Add listener for database updates.
     * @param callback the callback to run when the groups of the current user changes.
     */
    override fun onUpdate(callback: (Map<GroupID, UserGroup>) -> Unit) {
        groupsRef
            .whereArrayContains("members", _userID)
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
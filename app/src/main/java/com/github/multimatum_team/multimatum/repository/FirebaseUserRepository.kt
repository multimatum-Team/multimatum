package com.github.multimatum_team.multimatum.repository

import com.github.multimatum_team.multimatum.model.UserID
import com.github.multimatum_team.multimatum.model.UserInfo
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * An interface to the user repository.
 * This lets us get publicly available information about signed-in users.
 */
class FirebaseUserRepository @Inject constructor(database: FirebaseFirestore) : UserRepository() {
    private val usersRef = database
        .collection("users")

    /**
     * Convert user information into a hashmap, so that we can send the user data to Firebase.
     */
    private fun serializeUserInfo(userInfo: UserInfo): HashMap<String, *> =
        hashMapOf(
            "name" to userInfo.name,
        )

    /**
     * Convert a document snapshot from Firebase to a UserInfo instance.
     */
    private fun deserializeUserInfo(userInfoSnapshot: DocumentSnapshot): UserInfo {
        val id = userInfoSnapshot.id
        val name = userInfoSnapshot["name"] as String
        return UserInfo(id, name)
    }

    /**
     * Get public information of a user identified by a user ID.
     */
    override suspend fun fetch(id: UserID): UserInfo =
        deserializeUserInfo(
            usersRef
                .document(id)
                .get()
                .await()
        )

    /**
     * Get public information of multiple users at once.
     */
    override suspend fun fetch(ids: List<UserID>): List<UserInfo> =
        usersRef
            .whereIn(FieldPath.documentId(), ids)
            .get()
            .await()
            .map { deserializeUserInfo(it) }

    /**
     * Add information about an signed-in user.
     */
    override suspend fun setInfo(userInfo: UserInfo) {
        usersRef
            .document(userInfo.id)
            .set(serializeUserInfo(userInfo))
    }
}
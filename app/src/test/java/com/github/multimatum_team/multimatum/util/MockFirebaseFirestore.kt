package com.github.multimatum_team.multimatum.util

import com.github.multimatum_team.multimatum.model.*
import com.github.multimatum_team.multimatum.repository.DeadlineID
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.mockito.Mockito.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

sealed interface DeadlineOwnerData
data class UserOwnedData(val userID: UserID) : DeadlineOwnerData
data class GroupOwnedData(val groupID: GroupID) : DeadlineOwnerData

data class DeadlineData(
    val title: String,
    val state: DeadlineState,
    val dateTime: LocalDateTime,
    val description: String,
    val ownerData: DeadlineOwnerData,
    val pdfPath: String
)

class MockFirebaseFirestore(
    deadlines: List<DeadlineData>,
    groups: List<UserGroup>,
    users: List<UserInfo>
) {
    private var deadlineCounter = 0
    private var groupCounter = 0

    private val groups: MutableMap<GroupID, UserGroup> =
        groups.associate { group ->
            val newID = groupCounter.toString()
            groupCounter++
            newID to group.copy(id = newID)
        }.toMutableMap()

    private val deadlines: MutableMap<DeadlineID, DeadlineData> =
        deadlines.associateBy {
            val id = deadlineCounter.toString()
            deadlineCounter++
            id
        }.toMutableMap()

    private val users: MutableMap<UserID, UserInfo> =
        users.associateBy { userInfo -> userInfo.id }.toMutableMap()

    val database: FirebaseFirestore = mock(FirebaseFirestore::class.java)

    private val groupSnapshotListeners: MutableMap<UserID, MutableList<EventListener<QuerySnapshot>>> =
        mutableMapOf()

    private val deadlineSnapshotListeners: MutableMap<List<DeadlineOwnerData>, MutableList<EventListener<QuerySnapshot>>> =
        mutableMapOf()

    init {
        generateDatabase()
    }

    private fun notifyGroupListeners(userID: UserID) {
        for (listener in groupSnapshotListeners.getOrElse(userID) { listOf() }) {
            listener.onEvent(
                runBlocking { generateGroupOwnerQuery(userID).get().await() },
                null
            )
        }
    }

    private fun notifyGroupListeners(members: Iterable<UserID>) {
        for (memberID in members) {
            notifyGroupListeners(memberID)
        }
    }

    private fun generateGroupDocumentSnapshot(group: UserGroup): DocumentSnapshot {
        val snapshot = mock(DocumentSnapshot::class.java)
        `when`(snapshot.id).thenReturn(group.id)
        `when`(snapshot.get("name")).thenReturn(group.name)
        `when`(snapshot.get("owner")).thenReturn(group.owner)
        `when`(snapshot.get("members")).thenReturn(group.members.toList())
        return snapshot
    }

    private fun generateGroupQueryDocumentSnapshot(group: UserGroup): QueryDocumentSnapshot {
        val snapshot = mock(QueryDocumentSnapshot::class.java)
        `when`(snapshot.id).thenReturn(group.id)
        `when`(snapshot.get("name")).thenReturn(group.name)
        `when`(snapshot.get("owner")).thenReturn(group.owner)
        `when`(snapshot.get("members")).thenReturn(group.members.toList())
        return snapshot
    }

    private fun generateGroupDocument(group: UserGroup): DocumentReference {
        val snapshot = generateGroupDocumentSnapshot(group)
        val document = mock(DocumentReference::class.java)
        `when`(document.id).thenReturn(group.id)
        `when`(document.get()).thenReturn(Tasks.forResult(snapshot))
        `when`(document.delete()).then {
            groups.remove(group.id)
            generateDatabase()
            notifyGroupListeners(group.members)
            Tasks.forResult(Unit)
        }
        return document
    }

    private fun generateGroupQuerySnapshot(queryResult: List<UserGroup>): QuerySnapshot {
        val querySnapshot = mock(QuerySnapshot::class.java)
        val groupSnapshots = queryResult.map { generateGroupDocumentSnapshot(it) }
        val groupQuerySnapshots = queryResult.map { generateGroupQueryDocumentSnapshot(it) }
        `when`(querySnapshot.documents).thenReturn(groupSnapshots)
        `when`(querySnapshot.iterator()).then {
            groupQuerySnapshots.iterator()
        }
        return querySnapshot
    }

    private fun generateGroupMemberQuery(userID: UserID): Query {
        val query = mock(Query::class.java)
        val filteredGroups = groups.filterValues { group ->
            group.members.any { it == userID }
        }
        val querySnapshot = generateGroupQuerySnapshot(filteredGroups.values.toList())
        `when`(query.get()).thenReturn(Tasks.forResult(querySnapshot))
        `when`(query.addSnapshotListener(any())).then {
            val listener = it.getArgument<EventListener<QuerySnapshot>>(0)
            groupSnapshotListeners.getOrPut(userID) { mutableListOf() }.add(listener)
            ListenerRegistration { }
        }
        return query
    }

    private fun generateGroupOwnerQuery(userID: UserID): Query {
        val query = mock(Query::class.java)
        val querySnapshot = generateGroupQuerySnapshot(groups.filterValues { group ->
            group.owner == userID
        }.values.toList())
        `when`(query.get()).thenReturn(Tasks.forResult(querySnapshot))
        return query
    }

    private fun generateNewGroupDocument(serializedGroup: HashMap<String, *>): DocumentReference {
        val newID = groupCounter.toString()
        groupCounter++
        val group = UserGroup(
            newID,
            (serializedGroup["name"]!! as UserID),
            (serializedGroup["owner"]!! as UserID),
            (serializedGroup["members"]!! as List<UserID>).toSet()
        )
        groups[newID] = group
        return generateGroupDocument(group)
    }

    private fun generateGroupCollection(groups: List<UserGroup>): CollectionReference {
        val collection = mock(CollectionReference::class.java)
        for (group in groups) {
            val document = generateGroupDocument(group)
            `when`(collection.document(group.id)).thenReturn(document)
        }
        `when`(collection.whereArrayContains(eq("members"), anyString()))
            .then {
                val userID = it.getArgument<String>(1)
                generateGroupMemberQuery(userID)
            }
        `when`(collection.whereEqualTo(eq("owner"), anyString())).then {
            val userID = it.getArgument<String>(1)
            generateGroupOwnerQuery(userID)
        }
        `when`(collection.add(any())).then {
            val serializedGroup = it.getArgument<HashMap<String, *>>(0)
            val document = generateNewGroupDocument(serializedGroup)
            generateDatabase()
            notifyGroupListeners(serializedGroup["members"] as List<UserID>)
            Tasks.forResult(document)
        }
        return collection
    }

    // Deadlines

    private fun notifyDeadlineListeners(ownerData: DeadlineOwnerData) {
        for ((ownerList, listeners) in deadlineSnapshotListeners.entries) {
            if (ownerList.contains(ownerData)) {
                for (listener in listeners) {
                    listener.onEvent(
                        runBlocking { generateDeadlineOwnerQuery(ownerList).get().await() },
                        null
                    )
                }
            }
        }
    }

    private fun generateDeadlineDocumentSnapshot(
        id: DeadlineID,
        deadlineData: DeadlineData
    ): DocumentSnapshot {
        val snapshot = mock(DocumentSnapshot::class.java)
        `when`(snapshot.id).thenReturn(id)
        `when`(snapshot.get("title")).thenReturn(deadlineData.title)
        `when`(snapshot.get("state")).thenReturn(deadlineData.state.ordinal.toLong())
        `when`(snapshot.get("date")).thenReturn(
            Timestamp(
                deadlineData.dateTime
                    .atZone(ZoneId.systemDefault())
                    .toEpochSecond(),
                0
            )
        )
        `when`(snapshot.get("description")).thenReturn(deadlineData.description)
        `when`(snapshot.get("owner")).thenReturn(
            when (deadlineData.ownerData) {
                is UserOwnedData -> mapOf(
                    "type" to "user",
                    "id" to deadlineData.ownerData.userID
                )
                is GroupOwnedData -> mapOf(
                    "type" to "group",
                    "id" to deadlineData.ownerData.groupID
                )
            }
        )
        `when`(snapshot.get("pdfPath")).thenReturn(deadlineData.pdfPath)
        return snapshot
    }

    private fun generateDeadlineDocument(
        id: DeadlineID,
        deadlineData: DeadlineData
    ): DocumentReference {
        val snapshot = generateDeadlineDocumentSnapshot(id, deadlineData)
        val document = mock(DocumentReference::class.java)
        `when`(document.id).thenReturn(id)
        `when`(document.get()).thenReturn(Tasks.forResult(snapshot))
        `when`(document.delete()).then {
            deadlines.remove(id)
            generateDatabase()
            notifyDeadlineListeners(deadlineData.ownerData)
            Tasks.forResult(Unit)
        }
        return document
    }

    private fun generateDeadlineQueryDocumentSnapshot(
        id: DeadlineID,
        deadlineData: DeadlineData
    ): QueryDocumentSnapshot {
        val snapshot = mock(QueryDocumentSnapshot::class.java)
        `when`(snapshot.id).thenReturn(id)
        `when`(snapshot.get("title")).thenReturn(deadlineData.title)
        `when`(snapshot.get("state")).thenReturn(deadlineData.state.ordinal.toLong())
        `when`(snapshot.get("date")).thenReturn(
            Timestamp(
                deadlineData.dateTime
                    .atZone(ZoneId.systemDefault())
                    .toEpochSecond(),
                0
            )
        )
        `when`(snapshot.get("description")).thenReturn(deadlineData.description)
        `when`(snapshot.get("owner")).thenReturn(
            when (deadlineData.ownerData) {
                is UserOwnedData -> mapOf(
                    "type" to "user",
                    "id" to deadlineData.ownerData.userID
                )
                is GroupOwnedData -> mapOf(
                    "type" to "group",
                    "id" to deadlineData.ownerData.groupID
                )
            }
        )
        `when`(snapshot.get("pdfPath")).thenReturn(deadlineData.pdfPath)
        return snapshot
    }

    private fun generateNewDeadlineDocument(serializedDeadline: HashMap<String, *>): DocumentReference {
        val newID = deadlineCounter.toString()
        deadlineCounter++
        val title = serializedDeadline["title"] as String
        val state = DeadlineState.values()[serializedDeadline["state"] as Int]
        val timestamp = serializedDeadline["date"] as Timestamp
        val milliseconds = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
        val date = Instant
            .ofEpochMilli(milliseconds)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        val description = serializedDeadline["description"] as String
        val ownerMap = serializedDeadline["owner"] as Map<String, String>
        val ownerData = when (ownerMap["type"]) {
            "user" -> UserOwnedData(ownerMap["id"] as UserID)
            "group" -> GroupOwnedData(ownerMap["id"] as GroupID)
            else -> throw IllegalArgumentException("provided serialized deadline has ill-formed owner type, expected \"user\" or \"group\"")
        }
        val pdfPath = serializedDeadline["pdfPath"] as String
        val deadlineData = DeadlineData(title, state, date, description, ownerData, pdfPath)
        deadlines[newID] = deadlineData
        return generateDeadlineDocument(newID, deadlineData)
    }

    private fun generateDeadlineQuerySnapshot(queryResult: Map<DeadlineID, DeadlineData>): QuerySnapshot {
        val querySnapshot = mock(QuerySnapshot::class.java)
        val deadlineSnapshots =
            queryResult.map { generateDeadlineDocumentSnapshot(it.key, it.value) }
        val deadlineQuerySnapshots =
            queryResult.map { generateDeadlineQueryDocumentSnapshot(it.key, it.value) }
        `when`(querySnapshot.documents).thenReturn(deadlineSnapshots)
        `when`(querySnapshot.iterator()).then {
            deadlineQuerySnapshots.iterator()
        }
        return querySnapshot
    }

    private fun generateDeadlineOwnerQuery(ownerList: List<DeadlineOwnerData>): Query {
        val query = mock(Query::class.java)
        val querySnapshot = generateDeadlineQuerySnapshot(deadlines.filterValues { deadlineData ->
            ownerList.contains(deadlineData.ownerData)
        })
        `when`(query.get()).thenReturn(Tasks.forResult(querySnapshot))
        `when`(query.addSnapshotListener(any())).then {
            val listener = it.getArgument<EventListener<QuerySnapshot>>(0)
            deadlineSnapshotListeners.getOrPut(ownerList) { mutableListOf() }.add(listener)
            ListenerRegistration { }
        }
        return query
    }

    private fun generateDeadlineCollection(deadlines: Map<DeadlineID, DeadlineData>): CollectionReference {
        val collection = mock(CollectionReference::class.java)
        for ((id, deadlineData) in deadlines.entries) {
            val document = generateDeadlineDocument(id, deadlineData)
            `when`(collection.document(id)).thenReturn(document)
        }
        `when`(collection.whereIn(eq("owner"), anyList()))
            .then {
                val ownerList = it.getArgument<List<Any>>(1).map { ownerMap ->
                    when (ownerMap) {
                        is String -> UserOwnedData(ownerMap)
                        is Map<*, *> -> when (ownerMap["type"]!! as String) {
                            "user" -> UserOwnedData(ownerMap["id"]!! as String)
                            "group" -> GroupOwnedData(ownerMap["id"]!! as String)
                            else -> throw IllegalArgumentException("invalid owner type")
                        }
                        else -> throw IllegalArgumentException("invalid owner query")
                    }
                }
                generateDeadlineOwnerQuery(ownerList)
            }
        `when`(collection.whereEqualTo(eq("owner"), anyMap<String, String>()))
            .then {
                val ownerMap = it.getArgument<Map<String, String>>(1)
                val ownerData = when (ownerMap["type"]!!) {
                    "user" -> UserOwnedData(ownerMap["id"]!!)
                    "group" -> GroupOwnedData(ownerMap["id"]!!)
                    else -> throw IllegalArgumentException("invalid owner type")
                }
                generateDeadlineOwnerQuery(listOf(ownerData))
            }
        `when`(collection.add(any())).then {
            val serializedDeadline = it.getArgument<HashMap<String, *>>(0)
            val document = generateNewDeadlineDocument(serializedDeadline)
            generateDatabase()
            val ownerMap = serializedDeadline["owner"] as Map<String, String>
            val ownerData = when (ownerMap["type"]!!) {
                "user" -> UserOwnedData(ownerMap["id"]!!)
                "group" -> GroupOwnedData(ownerMap["id"]!!)
                else -> throw IllegalArgumentException("invalid owner type")
            }
            notifyDeadlineListeners(ownerData)
            Tasks.forResult(document)
        }
        return collection
    }

    // Users

    private fun generateUserQueryDocumentSnapshot(
        userInfo: UserInfo
    ): QueryDocumentSnapshot {
        val snapshot = mock(QueryDocumentSnapshot::class.java)
        `when`(snapshot.id).thenReturn(userInfo.id)
        `when`(snapshot.get("name")).thenReturn(userInfo.name)
        return snapshot
    }

    private fun generateUserQuerySnapshot(
        userInfo: List<UserInfo>
    ): QuerySnapshot {
        val snapshot = mock(QuerySnapshot::class.java)
        `when`(snapshot.iterator()).then {
            userInfo.map {
                generateUserQueryDocumentSnapshot(it)
            }.iterator()
        }
        return snapshot
    }

    private fun generateUserQuery(ids: List<UserID>): Query {
        val query = mock(Query::class.java)
        val querySnapshot = generateUserQuerySnapshot(users.filterKeys { id ->
            ids.contains(id)
        }.values.toList())
        `when`(query.get()).thenReturn(Tasks.forResult(querySnapshot))
        return query
    }

    private fun generateUserDocumentSnapshot(userInfo: UserInfo): DocumentSnapshot {
        val snapshot = mock(DocumentSnapshot::class.java)
        `when`(snapshot.id).thenReturn(userInfo.id)
        `when`(snapshot.get("name")).thenReturn(userInfo.name)
        return snapshot
    }

    private fun generateUserDocument(userInfo: UserInfo): DocumentReference {
        val snapshot = generateUserDocumentSnapshot(userInfo)
        val document = mock(DocumentReference::class.java)
        `when`(document.id).thenReturn(userInfo.id)
        `when`(document.get()).thenReturn(Tasks.forResult(snapshot))
        `when`(document.set(any())).then {
            val serializedInfo = it.getArgument<Map<String, *>>(0)
            users[userInfo.id] = UserInfo(userInfo.id, serializedInfo["name"] as String)
            generateDatabase()
            Tasks.forResult(Unit)
        }
        return document
    }

    private fun generateNewUserDocument(id: UserID): DocumentReference {
        val document = mock(DocumentReference::class.java)
        `when`(document.set(any())).then {
            val serializedInfo = it.getArgument<Map<String, *>>(0)
            users[id] = UserInfo(id, serializedInfo["name"] as String)
            generateDatabase()
            Tasks.forResult(Unit)
        }
        return document
    }

    private fun generateUserCollection(users: Map<UserID, UserInfo>): CollectionReference {
        val collection = mock(CollectionReference::class.java)
        `when`(collection.document(anyString())).then {
            val id = it.getArgument<UserID>(0)
            when (val userInfo = users[id]) {
                is UserInfo -> generateUserDocument(userInfo)
                else -> generateNewUserDocument(id)
            }
        }
        `when`(collection.whereIn(eq(FieldPath.documentId()), anyList<UserID>()))
            .then {
                val idList = it.getArgument<List<UserID>>(1)
                generateUserQuery(idList)
            }
        return collection
    }

    private fun generateDatabase() {
        val groupCollection = generateGroupCollection(groups.values.toList())
        val deadlineCollection = generateDeadlineCollection(deadlines)
        val userCollection = generateUserCollection(users)
        reset(database)
        `when`(database.collection("groups")).thenReturn(groupCollection)
        `when`(database.collection("deadlines")).thenReturn(deadlineCollection)
        `when`(database.collection("users")).thenReturn(userCollection)
    }
}
package com.github.multimatum_team.multimatum.util

import com.github.multimatum_team.multimatum.model.UserGroup
import com.github.multimatum_team.multimatum.model.UserID
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.mockito.Mockito.*

class MockFirebaseFirestore(initialContents: List<UserGroup>) {
    private var counter: Int = 0

    private val groups: MutableList<UserGroup> =
        initialContents.map { group ->
            val newID = counter.toString()
            counter++
            group.copy(id = newID)
        }.toMutableList()

    val database: FirebaseFirestore = mock(FirebaseFirestore::class.java)

    private val snapshotListeners: MutableMap<UserID, MutableList<EventListener<QuerySnapshot>>> =
        mutableMapOf()

    init {
        generateDatabase()
    }

    private fun notifyListeners(userID: UserID) {
        println("notifying $userID, ${snapshotListeners[userID]}")
        for (listener in snapshotListeners.getOrElse(userID) { listOf() }) {
            listener.onEvent(
                runBlocking { generateOwnerQuery(userID).get().await() },
                null
            )
        }
    }

    private fun notifyListeners(members: Iterable<UserID>) {
        for (memberID in members) {
            notifyListeners(memberID)
        }
    }

    private fun generateDocumentSnapshot(group: UserGroup): DocumentSnapshot {
        val snapshot = mock(DocumentSnapshot::class.java)
        `when`(snapshot.id).thenReturn(group.id)
        `when`(snapshot.get("name")).thenReturn(group.name)
        `when`(snapshot.get("owner")).thenReturn(group.owner)
        `when`(snapshot.get("members")).thenReturn(group.members.toList())
        return snapshot
    }

    private fun generateQueryDocumentSnapshot(group: UserGroup): QueryDocumentSnapshot {
        val snapshot = mock(QueryDocumentSnapshot::class.java)
        `when`(snapshot.id).thenReturn(group.id)
        `when`(snapshot.get("name")).thenReturn(group.name)
        `when`(snapshot.get("owner")).thenReturn(group.owner)
        `when`(snapshot.get("members")).thenReturn(group.members.toList())
        return snapshot
    }

    private fun generateDocument(group: UserGroup): DocumentReference {
        val snapshot = generateDocumentSnapshot(group)
        val document = mock(DocumentReference::class.java)
        `when`(document.id).thenReturn(group.id)
        `when`(document.get()).thenReturn(Tasks.forResult(snapshot))
        `when`(document.delete()).then {
            groups.removeIf { it.id == group.id }
            generateDatabase()
            notifyListeners(group.members)
            Tasks.forResult(Unit)
        }
        return document
    }

    private fun generateQuerySnapshot(queryResult: List<UserGroup>): QuerySnapshot {
        val querySnapshot = mock(QuerySnapshot::class.java)
        val groupSnapshots = queryResult.map { generateDocumentSnapshot(it) }
        val groupQuerySnapshots = queryResult.map { generateQueryDocumentSnapshot(it) }
        `when`(querySnapshot.documents).thenReturn(groupSnapshots)
        `when`(querySnapshot.iterator()).then {
            groupQuerySnapshots.iterator()
        }
        return querySnapshot
    }

    private fun generateMemberQuery(userID: UserID): Query {
        val query = mock(Query::class.java)
        val filteredGroups = groups.filter { group ->
            group.members.any { it == userID }
        }
        val querySnapshot = generateQuerySnapshot(filteredGroups)
        `when`(query.get()).thenReturn(Tasks.forResult(querySnapshot))
        `when`(query.addSnapshotListener(any())).then {
            val listener = it.getArgument<EventListener<QuerySnapshot>>(0)
            snapshotListeners.getOrPut(userID) { mutableListOf() }.add(listener)
            ListenerRegistration { }
        }
        return query
    }

    private fun generateOwnerQuery(userID: UserID): Query {
        val query = mock(Query::class.java)
        val querySnapshot = generateQuerySnapshot(groups.filter { group ->
            group.owner == userID
        })
        `when`(query.get()).thenReturn(Tasks.forResult(querySnapshot))
        return query
    }

    private fun generateNewDocument(serializedGroup: HashMap<String, *>): DocumentReference {
        val newID = counter.toString()
        counter++
        val group = UserGroup(
            newID,
            (serializedGroup["name"]!! as UserID),
            (serializedGroup["owner"]!! as UserID),
            (serializedGroup["members"]!! as List<UserID>).toSet()
        )
        groups.add(group)
        return generateDocument(group)
    }

    private fun generateCollection(groups: List<UserGroup>): CollectionReference {
        val collection = mock(CollectionReference::class.java)
        for (group in groups) {
            val document = generateDocument(group)
            `when`(collection.document(group.id)).thenReturn(document)
        }
        `when`(collection.whereArrayContains(eq("members"), anyString()))
            .then {
                val userID = it.getArgument<String>(1)
                generateMemberQuery(userID)
            }
        `when`(collection.whereEqualTo(eq("owner"), anyString())).then {
            val userID = it.getArgument<String>(1)
            generateOwnerQuery(userID)
        }
        `when`(collection.add(any())).then {
            val serializedGroup = it.getArgument<HashMap<String, *>>(0)
            val document = generateNewDocument(serializedGroup)
            generateDatabase()
            notifyListeners(serializedGroup["members"] as List<UserID>)
            Tasks.forResult(document)
        }
        return collection
    }

    fun generateDatabase() {
        val collection = generateCollection(groups)
        reset(database)
        `when`(database.collection("groups")).thenReturn(collection)
    }
}
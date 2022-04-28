package com.github.multimatum_team.multimatum.util

import com.github.multimatum_team.multimatum.model.GroupID
import com.github.multimatum_team.multimatum.model.UserGroup
import com.github.multimatum_team.multimatum.model.UserID
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import org.mockito.Mockito

class MockFirestore(initialContents: List<UserGroup>) {
    private var counter: Int = 0

    private val groups: MutableList<UserGroup> =
        initialContents.map { group ->
            val newID = counter.toString()
            counter++
            group.copy(id = newID)
        }.toMutableList()

    val database = Mockito.mock(FirebaseFirestore::class.java)

    init {
        generateDatabase()
    }

    private fun serializeGroup(group: UserGroup): HashMap<String, *> =
        hashMapOf(
            "name" to group.name,
            "owner" to group.owner,
            "members" to group.members.toList()
        )

    private fun deserializeGroup(groupSnapshot: DocumentSnapshot): UserGroup =
        UserGroup(
            id = groupSnapshot.id,
            name = groupSnapshot["name"] as String,
            owner = groupSnapshot["owner"] as String,
            members = (groupSnapshot["members"] as List<String>).toSet()
        )

    private fun generateDocumentSnapshot(
        id: GroupID,
        serializedGroup: HashMap<String, *>
    ): DocumentSnapshot {
        val snapshot = Mockito.mock(DocumentSnapshot::class.java)
        Mockito.`when`(snapshot.id).thenReturn(id)
        Mockito.`when`(snapshot.get("name")).thenReturn(serializedGroup["name"])
        Mockito.`when`(snapshot.get("owner")).thenReturn(serializedGroup["owner"])
        Mockito.`when`(snapshot.get("members")).thenReturn(serializedGroup["members"])
        return snapshot
    }

    private fun generateDocumentSnapshot(group: UserGroup): DocumentSnapshot =
        generateDocumentSnapshot(group.id, serializeGroup(group))

    private fun generateDocument(group: UserGroup): DocumentReference {
        val snapshot = generateDocumentSnapshot(group)
        val document = Mockito.mock(DocumentReference::class.java)
        Mockito.`when`(document.id).thenReturn(group.id)
        Mockito.`when`(document.get()).thenReturn(Tasks.forResult(snapshot))
        Mockito.`when`(document.delete()).then {
            groups.removeIf { it.id == group.id }
            Tasks.forResult(Unit)
        }.then {
            generateDatabase()
        }
        return document
    }

    private fun generateQuerySnapshot(queryResult: List<UserGroup>): QuerySnapshot {
        val querySnapshot = Mockito.mock(QuerySnapshot::class.java)
        val groupSnapshots = queryResult.map { generateDocumentSnapshot(it) }
        Mockito.`when`(querySnapshot.documents).thenReturn(groupSnapshots)
        return querySnapshot
    }

    private fun generateMemberQuery(userID: UserID): Query {
        val query = Mockito.mock(Query::class.java)
        val querySnapshot = generateQuerySnapshot(groups.filter { group ->
            group.members.any { it == userID }
        })
        Mockito.`when`(query.get()).thenReturn(Tasks.forResult(querySnapshot))
        return query
    }

    private fun generateOwnerQuery(userID: UserID): Query {
        val query = Mockito.mock(Query::class.java)
        val querySnapshot = generateQuerySnapshot(groups.filter { group ->
            group.owner == userID
        })
        Mockito.`when`(query.get()).thenReturn(Tasks.forResult(querySnapshot))
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
        val collection = Mockito.mock(CollectionReference::class.java)
        for (group in groups) {
            val document = generateDocument(group)
            Mockito.`when`(collection.document(group.id)).thenReturn(document)
        }
        Mockito.`when`(collection.whereArrayContains(Mockito.eq("members"), Mockito.anyString()))
            .then {
                val userID = it.getArgument<String>(1)
                generateMemberQuery(userID)
            }
        Mockito.`when`(collection.whereEqualTo(Mockito.eq("owner"), Mockito.anyString())).then {
            val userID = it.getArgument<String>(1)
            generateOwnerQuery(userID)
        }
        Mockito.`when`(collection.add(Mockito.any())).then {
            val serializedGroup = it.getArgument<HashMap<String, *>>(0)
            val document = generateNewDocument(serializedGroup)
            Tasks.forResult(document)
        }.then {
            generateDatabase()
        }
        return collection
    }

    fun generateDatabase() {
        val collection = generateCollection(groups)
        Mockito.`when`(database.collection("groups")).thenReturn(collection)
    }
}
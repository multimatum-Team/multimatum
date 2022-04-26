package com.github.multimatum_team.multimatum.model

typealias GroupID = String

/**
 * Model for user groups.
 * A group enables users to share deadlines.
 * Each group is owned by a single user which must be signed-in, and contains a collection of user
 * IDs which identify group members.
 * @param id an unique identifier provided by the group repository
 * @param name the human name of the group
 * @param owner the ID of the user that created the group, must correspond to a signed-in user
 * @param members the collection of group members (including the owner) stored as user IDs
 */
data class UserGroup(
    val id: GroupID,
    val name: String,
    val owner: UserID,
    val members: Set<UserID>
) {
    init {
        if (!members.contains(owner)) {
            throw IllegalArgumentException()
        }
    }

    constructor(id: GroupID, name: String, owner: UserID) :
            this(id, name, owner, setOf(owner))
}
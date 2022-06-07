package com.github.multimatum_team.multimatum.model

typealias UserID = String

/**
 * A class to gather public information about an user.
 */
data class UserInfo(
    val id: UserID,
    val name: String,
)

/**
 * Model for users.
 */
sealed interface User {
    val id: UserID
}

/**
 * A model for users that have not yet signed-in using an e-mail.
 * @param id an unique identifier provided by the user repository
 */
data class AnonymousUser(override val id: UserID) : User

/**
 * A model for users that have signed-in with an e-mail.
 * @param id an unique identifier provided by the user repository
 * @param email the e-mail associated with the signed-in account
 */
data class SignedInUser(
    override val id: UserID,
    val name: String,
    val email: String,
) : User {
    val info: UserInfo
        get() = UserInfo(id, name)
}
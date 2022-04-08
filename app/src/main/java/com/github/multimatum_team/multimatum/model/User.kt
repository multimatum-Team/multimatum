package com.github.multimatum_team.multimatum.model

typealias UserID = String

sealed class User(open val id: UserID)

data class AnonymousUser(override val id: UserID) : User(id)

data class SignedInUser(
    override val id: UserID,
    val email: String
) : User(id)
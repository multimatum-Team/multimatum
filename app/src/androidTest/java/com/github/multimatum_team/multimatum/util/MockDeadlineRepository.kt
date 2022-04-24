package com.github.multimatum_team.multimatum.util

import com.github.multimatum_team.multimatum.model.AnonymousUser
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.UserID
import com.github.multimatum_team.multimatum.repository.DeadlineID
import com.github.multimatum_team.multimatum.repository.DeadlineRepository

/**
 * Defines a dummy deadline repository that works locally on a plain list.
 * This way the tests are completely independent from Firebase or network availability.
 */
class MockDeadlineRepository(initialContents: Map<UserID, List<Deadline>>) : DeadlineRepository() {
    private var counter: Int = 0

    private var deadlinesPerUser: MutableMap<UserID, MutableMap<DeadlineID, Deadline>> =
        mutableMapOf()

    private val deadlines: MutableMap<DeadlineID, Deadline>
        get() {
            if (!deadlinesPerUser.containsKey(_user.id)) {
                deadlinesPerUser[_user.id] = mutableMapOf()
            }
            return deadlinesPerUser[_user.id]!!
        }

    private val updateListeners: MutableMap<UserID, MutableList<(Map<DeadlineID, Deadline>) -> Unit>> =
        mutableMapOf()

    constructor(initialContents: List<Deadline>) :
            this(mutableMapOf("0" to initialContents)) {
        setUser(AnonymousUser("0"))
    }

    init {
        for ((userId, deadlinesForUser) in initialContents) {
            val identifiedDeadlines: MutableMap<DeadlineID, Deadline> = mutableMapOf()
            for (deadline in deadlinesForUser) {
                identifiedDeadlines[(counter++).toString()] = deadline
            }
            deadlinesPerUser[userId] = identifiedDeadlines
        }
    }

    private fun notifyUpdateListeners() =
        updateListeners[_user.id]?.forEach { it(deadlines) }

    override suspend fun fetchAll(): Map<DeadlineID, Deadline> = deadlines

    override suspend fun put(deadline: Deadline): DeadlineID {
        val id = (counter++).toString()
        deadlines[id] = deadline
        notifyUpdateListeners()
        return id
    }

    override suspend fun modify(id: DeadlineID, newDeadline: Deadline) {
        deadlines[id] = newDeadline
    }

    override suspend fun delete(id: DeadlineID) {
        deadlines.remove(id)
    }

    override fun onUpdate(callback: (Map<DeadlineID, Deadline>) -> Unit) {
        if (!updateListeners.containsKey(_user.id)) {
            updateListeners[_user.id] = mutableListOf()
        }
        updateListeners[_user.id]!!.add(callback)
    }
}
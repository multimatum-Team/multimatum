package com.github.multimatum_team.multimatum.util

import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.repository.DeadlineID
import com.github.multimatum_team.multimatum.repository.DeadlineRepository

/**
 * Defines a dummy deadline repository that works locally on a plain list.
 * This way the tests are completely independent from Firebase or network availability.
 */
class MockDeadlineRepository(deadlines: List<Deadline>) : DeadlineRepository {
    private val deadlines: MutableMap<DeadlineID, Deadline> =
        deadlines
            .mapIndexed { i, deadline -> i.toString() to deadline }
            .toMap()
            .toMutableMap()

    private var counter: Int = deadlines.size + 1

    private val updateListeners: MutableList<(Map<DeadlineID, Deadline>) -> Unit> =
        mutableListOf()

    private fun notifyUpdateListeners() =
        updateListeners.forEach { it(deadlines) }

    override suspend fun fetch(id: String): Deadline? =
        deadlines[id]

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
        updateListeners.add(callback)
    }
}
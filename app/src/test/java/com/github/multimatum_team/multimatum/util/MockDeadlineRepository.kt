package com.github.multimatum_team.multimatum.util

import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.repository.DeadlineRepository

/**
 * Defines a dummy deadline repository that works locally on a plain list.
 * This way the tests are completely independent from Firebase or network availability.
 */
class MockDeadlineRepository(deadlines: List<Deadline>) : DeadlineRepository {
    private val deadlines: MutableList<Deadline> = deadlines.toMutableList()

    private val updateListeners: MutableList<(List<Deadline>) -> Unit> = mutableListOf()

    private fun notifyUpdateListeners() =
        updateListeners.forEach { it(deadlines) }

    override suspend fun fetchAll(): List<Deadline> = deadlines.sortedBy { it.date }

    override suspend fun put(deadline: Deadline) {
        deadlines.add(deadline)
        notifyUpdateListeners()
    }

    override fun onUpdate(callback: (List<Deadline>) -> Unit) {
        updateListeners.add(callback)
    }
}
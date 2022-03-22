package com.github.multimatum_team.multimatum.model

import java.time.LocalDate
import java.time.Period

/**
 * The state of a deadline.
 *
 * Represents the level of progress of the task.
 */
enum class DeadlineState {
    /**
     * State for tasks that are yet to be completed.
     */
    TODO,

    /**
     * State for deadlines whose task is fully completed.
     */
    DONE
}

/**
 * A deadline.
 *
 * The Deadline class represents a deadline at a given date.
 * The deadline is also annotated by a state representing the advancement of the task.
 * If the given title is empty, IllegalArgumentException is thrown.
 *
 * @property title the title of the deadline (must be non empty)
 * @property state the advancement state of the deadline
 * @property date the time at which the work is due
 * @constructor Creates a deadline from specified parameters
 * @throws IllegalArgumentException when title is empty or startDate > end
 */
data class Deadline(
    val title: String,
    val state: DeadlineState,
    val date: LocalDate,
    val id: Int = 0
) {
    init {
        if (title.isEmpty()) {
            throw IllegalArgumentException()
        }
    }

    /**
     * Returns how much time is left to complete the task before the deadline.
     * If the deadline is due, return null.
     */
    val timeRemaining: Period?
        get() =
            if (isDue)
                null
            else {
                Period.between(LocalDate.now(), date)
            }

    /**
     * Tells whether the deadline has passed.
     */
    val isDue: Boolean
        get() = LocalDate.now() > date
}
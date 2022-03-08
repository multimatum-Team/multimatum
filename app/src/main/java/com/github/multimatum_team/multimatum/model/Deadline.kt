package com.github.multimatum_team.multimatum.model

import java.lang.IllegalArgumentException
import java.time.*

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
 * The Deadline class represents a deadline, marked with a start date and an end date.
 * The deadline is also annotated by a state representing the advancement of the task.
 * The start date must occur before the end date, otherwise IllegalArgumentException is thrown.
 *
 * @property title the title of the deadline
 * @property state the advancement state of the deadline
 * @property startDate the time at which the deadline was given
 *                     (not necessarily the date of creation)
 * @property endDate the time at which the work is due
 * @constructor Creates a deadline from specified parameters
 * @throws IllegalArgumentException when startDate > end
 */
data class Deadline(
    var title: String,
    var state: DeadlineState,
    var startDate: LocalDate,
    var endDate: LocalDate
) {
    init {
        if (startDate > endDate) {
            throw IllegalArgumentException()
        }
    }

    /**
     * Returns how much time was given for the task.
     */
    val duration: Period
        get() = Period.between(startDate, endDate)

    /**
     * Returns how much time is left to complete the task before the deadline.
     * If the deadline is due, return null.
     */
    val timeRemaining: Period?
        get() =
            if (isDue)
                null
            else {
                Period.between(LocalDate.now(), endDate)
            }

    /**
     * Tells whether the deadline has passed.
     */
    val isDue: Boolean
        get() = LocalDate.now() > endDate
}
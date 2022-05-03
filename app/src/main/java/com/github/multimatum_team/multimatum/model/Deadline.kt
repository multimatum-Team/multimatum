package com.github.multimatum_team.multimatum.model

import java.time.LocalDateTime
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
 * @property dateTime the time at which the work is due
 * @property description some description of the deadline, by default empty
 * @property notificationsTimes ArrayList of the notification's trigger time before deadline
 * @constructor Creates a deadline from specified parameters
 * @throws IllegalArgumentException when title is empty or startDate > end
 */
data class Deadline(
    val title: String,
    val state: DeadlineState,
    val dateTime: LocalDateTime,
    val description: String = "",
    val notificationsTimes: List<Long> = ArrayList(),
    val pdfURI: String = ""
) {
    init {
        if (title.isEmpty()) {
            throw IllegalArgumentException()
        }
    }
}
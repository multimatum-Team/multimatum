package com.github.multimatum_team.multimatum.service

import java.time.Clock
import java.time.LocalDateTime

/**
 * An interface for providing date and time.
 */
interface ClockService {
    /**
     * Return the clock used by the service.
     */
    fun getClock(): Clock

    /**
     * Get the local date provided by the clock.
     */
    fun now(): LocalDateTime =
        LocalDateTime.now(getClock())
}
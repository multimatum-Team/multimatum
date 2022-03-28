package com.github.multimatum_team.multimatum.service

import java.time.Clock
import java.time.LocalDate

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
    fun now(): LocalDate =
        LocalDate.now(getClock())
}
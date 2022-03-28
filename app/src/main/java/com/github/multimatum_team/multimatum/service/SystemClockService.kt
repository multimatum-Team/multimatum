package com.github.multimatum_team.multimatum.service

import java.time.Clock

/**
 * A real implementation of the clock service.
 * Uses the system clock to return the real time at which the code is executed.
 */
class SystemClockService : ClockService {
    override fun getClock(): Clock =
        Clock.systemDefaultZone()
}
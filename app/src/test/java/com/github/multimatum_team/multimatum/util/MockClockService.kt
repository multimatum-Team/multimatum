package com.github.multimatum_team.multimatum.util

import com.github.multimatum_team.multimatum.service.ClockService
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoField

/**
 * A mock class to simulate a fixed clock.
 * This lets us write more reproducible tests.
 */
class MockClockService(private var date: LocalDate) : ClockService {
    override fun getClock(): Clock =
        Clock.fixed(
            Instant.ofEpochSecond(date.atStartOfDay(ZoneId.of("UTC")).toEpochSecond()),
            ZoneId.of("UTC")
        )
}
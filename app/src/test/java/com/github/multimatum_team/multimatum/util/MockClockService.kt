package com.github.multimatum_team.multimatum.util

import com.github.multimatum_team.multimatum.service.ClockService
import java.time.*

/**
 * A mock class to simulate a fixed clock.
 * This lets us write more reproducible tests.
 */
class MockClockService(private var dateTime: LocalDateTime) : ClockService {
    override fun getClock(): Clock =
        Clock.fixed(
            Instant.ofEpochSecond(dateTime.toLocalDate().atStartOfDay(ZoneId.of("UTC")).toEpochSecond()),
            ZoneId.of("UTC")
        )
}
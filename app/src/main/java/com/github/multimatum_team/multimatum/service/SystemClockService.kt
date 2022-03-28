package com.github.multimatum_team.multimatum.service

import java.time.Clock

class SystemClockService() : ClockService {
    override fun getClock(): Clock =
        Clock.systemDefaultZone()
}
package com.github.multimatum_team.multimatum

import com.github.multimatum_team.multimatum.model.datetime_parser.DateTimeExtractor
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalTime

class DateTimeExtractorTest {

    @Test
    fun time_is_extracted_correctly(){
        val str = "Aqua-pony at 10:00"
        val expText = "Aqua-pony"
        val actualRes = DateTimeExtractor.parse(str)
        assertEquals(expText, actualRes.text)
        assertTrue(actualRes.timeFound)
        assertEquals(LocalTime.of(10, 0), actualRes.time)
        assertFalse(actualRes.dateFound)
    }

    @Test
    fun am_time_is_parsed_correctly(){
        val str = "Chemistry 2am (report)"
        val expText = "Chemistry (report)"
        val actualRes = DateTimeExtractor.parse(str)
        assertEquals(expText, actualRes.text)
        assertTrue(actualRes.timeFound)
        assertEquals(LocalTime.of(2, 0), actualRes.time)
        assertFalse(actualRes.dateFound)
    }

    @Test
    fun pm_time_is_parsed_correctly(){
        val str = "History dissertation 4pm"
        val expText = "History dissertation"
        val actualRes = DateTimeExtractor.parse(str)
        assertEquals(expText, actualRes.text)
        assertTrue(actualRes.timeFound)
        assertEquals(LocalTime.of(16, 0), actualRes.time)
        assertFalse(actualRes.dateFound)
    }

    @Test
    fun second_time_is_ignored(){
        val str = "History dissertation 4pm 5pm"
        val expText = "History dissertation 5pm"
        val actualRes = DateTimeExtractor.parse(str)
        assertEquals(expText, actualRes.text)
        assertTrue(actualRes.timeFound)
        assertEquals(LocalTime.of(16, 0), actualRes.time)
        assertFalse(actualRes.dateFound)
    }

}
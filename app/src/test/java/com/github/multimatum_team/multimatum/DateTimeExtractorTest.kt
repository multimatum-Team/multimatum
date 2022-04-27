package com.github.multimatum_team.multimatum

import com.github.multimatum_team.multimatum.model.datetime_parser.DateTimeExtractor
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalTime

class DateTimeExtractorTest {

    @Test
    fun date_is_extracted_correctly(){
        val str = "Aqua-pony at 10:00"
        val expText = "Aqua-pony at"
        val actualRes = DateTimeExtractor.parse(str)
        assertEquals(expText, actualRes.text)
        assertTrue(actualRes.timeFound)
        assertEquals(LocalTime.of(10, 0), actualRes.time)
        assertFalse(actualRes.dateFound)
    }

}
package com.github.multimatum_team.multimatum

import com.github.multimatum_team.multimatum.model.datetime_parser.DateTimeExtractor
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month

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

    @Test
    fun complete_date_is_parsed_correctly_in_d_m_y_format(){
        val str = "Entree en bourse de Multimatum&co 1.08.2022"
        val expText = "Entree en bourse de Multimatum&co"
        val actualRes = DateTimeExtractor.parse(str)
        assertEquals(expText, actualRes.text)
        assertTrue(actualRes.dateFound)
        assertEquals(LocalDate.of(2022, Month.AUGUST, 1), actualRes.date)
        assertFalse(actualRes.timeFound)
    }

    @Test
    fun complete_date_is_parsed_correctly_in_y_m_d_format(){
        val str = "Entree en bourse de Multimatum&co 2022.8.1"
        val expText = "Entree en bourse de Multimatum&co"
        val actualRes = DateTimeExtractor.parse(str)
        assertEquals(expText, actualRes.text)
        assertTrue(actualRes.dateFound)
        assertEquals(LocalDate.of(2022, Month.AUGUST, 1), actualRes.date)
        assertFalse(actualRes.timeFound)
    }

    @Test
    fun invalid_date_is_ignored(){
        // should reject 29.02.2021 (does not exist), but take 5.12.2021 (valid)
        val str = "New version release 29.2.2021.12.5"
        val expText = "New version release 29.2."
        val actualRes = DateTimeExtractor.parse(str)
        assertEquals(expText, actualRes.text)
        assertTrue(actualRes.dateFound)
        assertEquals(LocalDate.of(2021, Month.DECEMBER, 5), actualRes.date)
        assertFalse(actualRes.timeFound)
    }

    @Test
    fun valid_february29_should_be_parsed_correctly(){
        val str = "Declaration d'impots 29.2.2024"
        val expText = "Declaration d'impots"
        val actualRes = DateTimeExtractor.parse(str)
        assertEquals(expText, actualRes.text)
        assertTrue(actualRes.dateFound)
        assertEquals(LocalDate.of(2024, Month.FEBRUARY, 29), actualRes.date)
        assertFalse(actualRes.timeFound)
    }

    @Test
    fun date_and_time_in_same_string_should_be_parsed_correctly(){
        val str = "Rendu devoir 23.8.2020 14:30"
        val expStr = "Rendu devoir"
        val actualRes = DateTimeExtractor.parse(str)
        assertEquals(expStr, actualRes.text)
        assertTrue(actualRes.dateFound)
        assertTrue(actualRes.timeFound)
        assertEquals(LocalDate.of(2020, 8, 23), actualRes.date)
        assertEquals(LocalTime.of(14, 30), actualRes.time)
    }

}
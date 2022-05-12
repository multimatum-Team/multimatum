package com.github.multimatum_team.multimatum

import com.github.multimatum_team.multimatum.model.datetime_parser.DateTimeExtractionResult
import com.github.multimatum_team.multimatum.model.datetime_parser.DateTimeExtractor
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month

class DateTimeExtractorTest {

    companion object {
        private val DEFAULT_DATE = LocalDate.of(2022, 4, 1)
        private val DEFAULT_DATE_TIME_PROVIDER = { DEFAULT_DATE }
        private val DEFAULT_DATE_TIME_EXTRACTOR = DateTimeExtractor(DEFAULT_DATE_TIME_PROVIDER)
    }

    private fun assertFound(
        expText: String,
        expTime: LocalTime? = null,
        expDate: LocalDate? = null
    ): (DateTimeExtractionResult) -> Unit = { dateTimeExtractionRes ->
        assertEquals(expText, dateTimeExtractionRes.text)
        if (expTime == null) {
            assertFalse("found time, unexpected", dateTimeExtractionRes.timeFound)
        } else {
            assertTrue("expecting time but not found", dateTimeExtractionRes.timeFound)
            assertEquals(expTime, dateTimeExtractionRes.time)
        }
        if (expDate == null) {
            assertFalse("found date, unexpected", dateTimeExtractionRes.dateFound)
        } else {
            assertTrue("expecting date but not found", dateTimeExtractionRes.dateFound)
            assertEquals(expDate, dateTimeExtractionRes.date)
        }
    }

    @Test
    fun `18h_is_parsed_correctly`() {
        val str = "Geography 18h"
        val expText = "Geography"
        val actualRes = DEFAULT_DATE_TIME_EXTRACTOR.parse(str)
        assertFound(expText, expTime = LocalTime.of(18, 0))(actualRes)
    }

    @Test
    fun `10h00_is_parsed_correctly`() {
        val str = "Aqua-pony at 10:00"
        val expText = "Aqua-pony"
        val actualRes = DEFAULT_DATE_TIME_EXTRACTOR.parse(str)
        assertFound(expText, expTime = LocalTime.of(10, 0))(actualRes)
    }

    @Test
    fun am_time_is_parsed_correctly() {
        val str = "Chemistry 2am (report)"
        val expText = "Chemistry (report)"
        val actualRes = DEFAULT_DATE_TIME_EXTRACTOR.parse(str)
        assertFound(expText, expTime = LocalTime.of(2, 0))(actualRes)
    }

    @Test
    fun pm_time_is_parsed_correctly() {
        val str = "History dissertation 4pm"
        val expText = "History dissertation"
        val actualRes = DEFAULT_DATE_TIME_EXTRACTOR.parse(str)
        assertFound(expText, expTime = LocalTime.of(16, 0))(actualRes)
    }

    @Test
    fun second_time_is_ignored() {
        val str = "History dissertation 4pm 5pm"
        val expText = "History dissertation 5pm"
        val actualRes = DEFAULT_DATE_TIME_EXTRACTOR.parse(str)
        assertFound(expText, expTime = LocalTime.of(16, 0))(actualRes)
    }

    @Test
    fun complete_date_is_parsed_correctly_in_d_m_y_format() {
        val str = "Entree en bourse de Multimatum&co 1.08.2022"
        val expText = "Entree en bourse de Multimatum&co"
        val actualRes = DEFAULT_DATE_TIME_EXTRACTOR.parse(str)
        assertFound(expText, expDate = LocalDate.of(2022, Month.AUGUST, 1))(actualRes)
    }

    @Test
    fun complete_date_is_parsed_correctly_in_y_m_d_format() {
        val str = "Entree en bourse de Multimatum&co 2022.8.1"
        val expText = "Entree en bourse de Multimatum&co"
        val actualRes = DEFAULT_DATE_TIME_EXTRACTOR.parse(str)
        assertFound(expText, expDate = LocalDate.of(2022, Month.AUGUST, 1))(actualRes)
    }

    @Test
    fun invalid_date_is_ignored() {
        // should reject 29.02.2021 (does not exist), but take 5.12.2021 (valid)
        val str = "New version release 29.2.2021.12.5"
        val expText = "New version release 29.2."
        val actualRes = DEFAULT_DATE_TIME_EXTRACTOR.parse(str)
        assertFound(expText, expDate = LocalDate.of(2021, Month.DECEMBER, 5))(actualRes)
    }

    @Test
    fun valid_february29_should_be_parsed_correctly() {
        val str = "Declaration d'impots 29.2.2024"
        val expText = "Declaration d'impots"
        val actualRes = DEFAULT_DATE_TIME_EXTRACTOR.parse(str)
        assertFound(expText, expDate = LocalDate.of(2024, Month.FEBRUARY, 29))(actualRes)
    }

    @Test
    fun date_and_time_in_same_string_should_be_parsed_correctly() {
        val str = "Rendu devoir 23.8.2020 14:30"
        val expText = "Rendu devoir"
        val actualRes = DEFAULT_DATE_TIME_EXTRACTOR.parse(str)
        assertFound(
            expText,
            expDate = LocalDate.of(2020, 8, 23),
            expTime = LocalTime.of(14, 30)
        )(
            actualRes
        )
    }

    @Test
    fun day_of_week_should_be_parsed_correctly() {
        val str = "SDP meeting friday"
        val expText = "SDP meeting"
        val currentDate = LocalDate.of(2022, Month.APRIL, 30)
        val nextFriday = LocalDate.of(2022, Month.MAY, 6)
        val dateTimeExtractor = DateTimeExtractor { currentDate }
        val actualRes = dateTimeExtractor.parse(str)
        assertFound(expText, expDate = nextFriday)(actualRes)
    }

    @Test
    fun day_of_week_with_on_should_be_parsed_correctly() {
        val str = "SDP meeting on friday"
        val expText = "SDP meeting"
        val currentDate = LocalDate.of(2022, Month.APRIL, 30)
        val nextFriday = LocalDate.of(2022, Month.MAY, 6)
        val dateTimeExtractor = DateTimeExtractor { currentDate }
        val actualRes = dateTimeExtractor.parse(str)
        assertFound(expText, expDate = nextFriday)(actualRes)
    }

    @Test
    fun month_given_by_3_letters_name_is_parsed_correctly() {
        val str = "Devoir a rendre 15 oct 2019"
        val expText = "Devoir a rendre"
        val actualRes = DEFAULT_DATE_TIME_EXTRACTOR.parse(str)
        assertFound(expText, expDate = LocalDate.of(2019, Month.OCTOBER, 15))(
            actualRes
        )
    }

    @Test
    fun month_given_by_full_name_is_parsed_correctly() {
        val str = "Something to hand-in 23 jan 2018 in math"
        val expText = "Something to hand-in in math"
        val actualRes = DEFAULT_DATE_TIME_EXTRACTOR.parse(str)
        assertFound(expText, expDate = LocalDate.of(2018, Month.JANUARY, 23))(
            actualRes
        )
    }

    @Test
    fun thursday_20_is_parsed_correctly() {
        val str = "Physics experiment report thursday 20"
        val expText = "Physics experiment report"
        val currentDate = LocalDate.of(2022, Month.JANUARY, 12)
        val actualRes = DateTimeExtractor { currentDate }.parse(str)
        assertFound(expText, expDate = LocalDate.of(2022, Month.JANUARY, 20))(actualRes)
    }

    @Test
    fun friday_20_is_rejected_when_the_20th_is_not_a_friday() {
        /* what happens here is that the extractor tries
         * to match "friday 20", notices that the 20th is not
         * a friday and falls back to parsing only "friday" */
        val str = "Physics experiment report friday 20"
        val expText = "Physics experiment report 20"
        val currentDate = LocalDate.of(2022, Month.JANUARY, 12)
        val actualRes = DateTimeExtractor { currentDate }.parse(str)
        assertFound(expText, expDate = LocalDate.of(2022, Month.JANUARY, 14))(actualRes)
    }

    @Test
    fun midday_is_parsed_correctly() {
        val str = "Lunch at noon"
        val expText = "Lunch"
        val actualRes = DEFAULT_DATE_TIME_EXTRACTOR.parse(str)
        assertFound(expText, expTime = LocalTime.NOON)(actualRes)
    }

    @Test
    fun midnight_is_parsed_correctly() {
        val str = "Due work at midnight"
        val expText = "Due work"
        val actualRes = DEFAULT_DATE_TIME_EXTRACTOR.parse(str)
        assertFound(expText, expTime = LocalTime.MIDNIGHT)(actualRes)
    }

}
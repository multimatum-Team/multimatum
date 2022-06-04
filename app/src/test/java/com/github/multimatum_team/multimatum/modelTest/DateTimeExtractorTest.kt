package com.github.multimatum_team.multimatum.modelTest

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
        expDate: LocalDate? = null,
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
    fun `18h is parsed correctly`() {
        val str = "Geography 18h"
        val expText = "Geography"
        val actualRes = DEFAULT_DATE_TIME_EXTRACTOR.parse(str)
        assertFound(expText, expTime = LocalTime.of(18, 0))(actualRes)
    }

    @Test
    fun `10h00 is parsed correctly`() {
        val str = "Aqua-pony at 10:00"
        val expText = "Aqua-pony"
        val actualRes = DEFAULT_DATE_TIME_EXTRACTOR.parse(str)
        assertFound(expText, expTime = LocalTime.of(10, 0))(actualRes)
    }

    @Test
    fun `am time is parsed correctly`() {
        val str = "Chemistry 2am (report)"
        val expText = "Chemistry (report)"
        val actualRes = DEFAULT_DATE_TIME_EXTRACTOR.parse(str)
        assertFound(expText, expTime = LocalTime.of(2, 0))(actualRes)
    }

    @Test
    fun `pm time is parsed correctly`() {
        val str = "History dissertation 4pm"
        val expText = "History dissertation"
        val actualRes = DEFAULT_DATE_TIME_EXTRACTOR.parse(str)
        assertFound(expText, expTime = LocalTime.of(16, 0))(actualRes)
    }

    @Test
    fun `second time is ignored`() {
        val str = "History dissertation 4pm 5pm"
        val expText = "History dissertation 5pm"
        val actualRes = DEFAULT_DATE_TIME_EXTRACTOR.parse(str)
        assertFound(expText, expTime = LocalTime.of(16, 0))(actualRes)
    }

    @Test
    fun `complete date is parsed correctly in d-m-y format`() {
        val str = "Entree en bourse de Multimatum&co 1.08.2022"
        val expText = "Entree en bourse de Multimatum&co"
        val actualRes = DEFAULT_DATE_TIME_EXTRACTOR.parse(str)
        assertFound(expText, expDate = LocalDate.of(2022, Month.AUGUST, 1))(actualRes)
    }

    @Test
    fun `complete date is parsed correctly in y-m-d format`() {
        val str = "Entree en bourse de Multimatum&co 2022.8.1"
        val expText = "Entree en bourse de Multimatum&co"
        val actualRes = DEFAULT_DATE_TIME_EXTRACTOR.parse(str)
        assertFound(expText, expDate = LocalDate.of(2022, Month.AUGUST, 1))(actualRes)
    }

    @Test
    fun `invalid date is ignored`() {
        // should reject 29.02.2021 (does not exist), but take 5.12.2021 (valid)
        val str = "New version release 29.2.2021.12.5"
        val expText = "New version release 29.2."
        val actualRes = DEFAULT_DATE_TIME_EXTRACTOR.parse(str)
        assertFound(expText, expDate = LocalDate.of(2021, Month.DECEMBER, 5))(actualRes)
    }

    @Test
    fun `valid february 29 should be parsed correctly`() {
        val str = "Declaration d'impots 29.2.2024"
        val expText = "Declaration d'impots"
        val actualRes = DEFAULT_DATE_TIME_EXTRACTOR.parse(str)
        assertFound(expText, expDate = LocalDate.of(2024, Month.FEBRUARY, 29))(actualRes)
    }

    @Test
    fun `date and time in same string should be parsed correctly`() {
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
    fun `day of week should be parsed correctly`() {
        val str = "SDP meeting friday"
        val expText = "SDP meeting"
        val currentDate = LocalDate.of(2022, Month.APRIL, 30)
        val nextFriday = LocalDate.of(2022, Month.MAY, 6)
        val dateTimeExtractor = DateTimeExtractor { currentDate }
        val actualRes = dateTimeExtractor.parse(str)
        assertFound(expText, expDate = nextFriday)(actualRes)
    }

    @Test
    fun `day of week with on should be parsed correctly`() {
        val str = "SDP meeting on friday"
        val expText = "SDP meeting"
        val currentDate = LocalDate.of(2022, Month.APRIL, 30)
        val nextFriday = LocalDate.of(2022, Month.MAY, 6)
        val dateTimeExtractor = DateTimeExtractor { currentDate }
        val actualRes = dateTimeExtractor.parse(str)
        assertFound(expText, expDate = nextFriday)(actualRes)
    }

    @Test
    fun `month given by 3 letters name is parsed correctly`() {
        val str = "Devoir a rendre 15 oct 2019"
        val expText = "Devoir a rendre"
        val actualRes = DEFAULT_DATE_TIME_EXTRACTOR.parse(str)
        assertFound(expText, expDate = LocalDate.of(2019, Month.OCTOBER, 15))(
            actualRes
        )
    }

    @Test
    fun `month given by full name is parsed correctly`() {
        val str = "Something to hand-in 23 jan 2018 in math"
        val expText = "Something to hand-in in math"
        val actualRes = DEFAULT_DATE_TIME_EXTRACTOR.parse(str)
        assertFound(expText, expDate = LocalDate.of(2018, Month.JANUARY, 23))(
            actualRes
        )
    }

    @Test
    fun `thursday 20 is parsed correctly`() {
        val str = "Physics experiment report thursday 20"
        val expText = "Physics experiment report"
        val currentDate = LocalDate.of(2022, Month.JANUARY, 12)
        val actualRes = DateTimeExtractor { currentDate }.parse(str)
        assertFound(expText, expDate = LocalDate.of(2022, Month.JANUARY, 20))(actualRes)
    }

    @Test
    fun `friday 20 is rejected when the 20th is not a friday`() {
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
    fun `midday is parsed correctly`() {
        val str = "Lunch at noon"
        val expText = "Lunch"
        val actualRes = DEFAULT_DATE_TIME_EXTRACTOR.parse(str)
        assertFound(expText, expTime = LocalTime.NOON)(actualRes)
    }

    @Test
    fun `midnight is parsed correctly`() {
        val str = "Due work at midnight"
        val expText = "Due work"
        val actualRes = DEFAULT_DATE_TIME_EXTRACTOR.parse(str)
        assertFound(expText, expTime = LocalTime.MIDNIGHT)(actualRes)
    }

    @Test
    fun `tomorrow is parsed correctly`() {
        val str = "Due homework tomorrow"
        val expText = "Due homework"
        val currentDate = LocalDate.of(2020, 4, 4)
        val expectedDate = LocalDate.of(2020, 4, 5)
        val extractor = DateTimeExtractor { currentDate }
        assertFound(expText, expDate = expectedDate)(extractor.parse(str))
    }

    @Test
    fun `may 3rd is parsed correctly`() {
        val str = "Report May 3rd"
        val expText = "Report"
        val currDate = LocalDate.of(2021, Month.MARCH, 17)
        val expectedDate = LocalDate.of(2021, Month.MAY, 3)
        val extractor = DateTimeExtractor { currDate }
        assertFound(expText, expDate = expectedDate)(extractor.parse(str))
    }

    @Test
    fun `may-3 is parsed correctly`() {
        val str = "Report May-3"
        val expText = "Report"
        val currDate = LocalDate.of(2021, Month.MARCH, 17)
        val expectedDate = LocalDate.of(2021, Month.MAY, 3)
        val extractor = DateTimeExtractor { currDate }
        assertFound(expText, expDate = expectedDate)(extractor.parse(str))
    }

    @Test
    fun `3dot12 is parsed correctly`() {
        val str = "Something to submit 3.14"
        val expText = "Something to submit"
        val currDate = LocalDate.of(2021, Month.JANUARY, 10)
        val expectedDate = LocalDate.of(2021, Month.MARCH, 14)
        val extractor = DateTimeExtractor { currDate }
        assertFound(expText, expDate = expectedDate)(extractor.parse(str))
    }

    @Test
    fun `12dot3 is parsed correctly`() {
        val str = "Something to submit 14.3"
        val expText = "Something to submit"
        val currDate = LocalDate.of(2021, Month.JANUARY, 10)
        val expectedDate = LocalDate.of(2021, Month.MARCH, 14)
        val extractor = DateTimeExtractor { currDate }
        assertFound(expText, expDate = expectedDate)(extractor.parse(str))
    }

    @Test
    fun `11h45am is parsed correctly`() {
        val str = "Apero at 11h45am"
        val expText = "Apero"
        val expectedTime = LocalTime.of(11, 45)
        assertFound(expText, expTime = expectedTime)(DEFAULT_DATE_TIME_EXTRACTOR.parse(str))
    }

    @Test
    fun `7h27pm is parsed correctly`() {
        val str = "Apero at 7h27pm"
        val expText = "Apero"
        val expectedTime = LocalTime.of(19, 27)
        assertFound(expText, expTime = expectedTime)(DEFAULT_DATE_TIME_EXTRACTOR.parse(str))
    }

    @Test
    fun `2022 2 27 is parsed correctly`() {
        val str = "ToDo 2022 2 27"
        val expText = "ToDo"
        val expectedDate = LocalDate.of(2022, Month.FEBRUARY, 27)
        assertFound(expText, expDate = expectedDate)(DEFAULT_DATE_TIME_EXTRACTOR.parse(str))
    }

}
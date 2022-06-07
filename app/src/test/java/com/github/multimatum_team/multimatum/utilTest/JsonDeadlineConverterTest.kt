package com.github.multimatum_team.multimatum.utilTest

import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.github.multimatum_team.multimatum.util.JsonDeadlineConverter
import com.google.firebase.firestore.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.Month

class JsonDeadlineConverterTest {
    private val jsonDeadlineConverter = JsonDeadlineConverter()

    @Test
    fun `check right result when converting from deadline into json`() {
        val deadline = Deadline(
            "Bloup",
            DeadlineState.TODO,
            LocalDateTime.of(2022, Month.APRIL, 29, 14, 32)
        )
        assertEquals(
            """{"title":"Bloup","state":"TODO","dateTime":"29::04::2022 14::32::00","description":"","pdfPath":""}""",
            jsonDeadlineConverter.toJson(deadline)
        )
    }

    @Test
    fun `check right result when converting from json to deadline`() {
        val deadline = Deadline(
            "Bloup",
            DeadlineState.TODO,
            LocalDateTime.of(2022, Month.APRIL, 29, 14, 32)
        )
        val json = jsonDeadlineConverter.toJson(deadline)

        assertEquals(deadline, jsonDeadlineConverter.fromJson(json))
    }

    @Test
    fun `deadline containing location can be converted correctly`() {
        val deadline = Deadline(
            "Bloup",
            DeadlineState.TODO,
            LocalDateTime.of(2022, Month.APRIL, 29, 14, 32),
            locationName = "EPFL",
            location = GeoPoint(46.5191, 6.5668)
        )
        val json = jsonDeadlineConverter.toJson(deadline)
        assertEquals(deadline, jsonDeadlineConverter.fromJson(json))
    }
}
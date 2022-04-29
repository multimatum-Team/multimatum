package com.github.multimatum_team.multimatum.model.datetime_parser

import java.time.LocalDate
import java.time.LocalTime

/**
 * Time information extracted from a string
 * Can be a date, a time or NoInfo if no info was found
 */
sealed interface ExtractedInfo
data class ExtractedDate(val date: LocalDate) : ExtractedInfo
data class ExtractedTime(val time: LocalTime) : ExtractedInfo
object NoInfo : ExtractedInfo


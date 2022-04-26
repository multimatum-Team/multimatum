package com.github.multimatum_team.multimatum.model.datetime_parser

import java.time.LocalTime
import java.util.*

object DateTimeExtractor {

    /*
    18h00
    18h
    6am
    6pm

    Monday
    next Monday
    on Monday
     */

    private val PATTERNS = listOf<List<(Token) -> Boolean>>(
        listOf(Token::isHour, { it.isEqualTo(":") }, Token::isMinute),
        TODO()
    )

    fun parse(str: String): DateTimeExtractionResult {

        val remainingTokens = Tokenizer.tokenize(str).toMutableList()
        val alreadyProcessedTokensWithoutTimeInfo = mutableListOf<Token>()

        tailrec fun extractDateTimeInfo(date: Date?, time: LocalTime?): Pair<Date?, LocalTime?> {
            TODO()
        }

        val (date, time) = extractDateTimeInfo(null, null)
        val text = alreadyProcessedTokensWithoutTimeInfo.joinToString { it.str }
        return DateTimeExtractionResult(text, date, time)
    }

}

data class DateTimeExtractionResult(
    val text: String,
    val date: Date? = null,
    val time: LocalTime? = null
) {
    val dateFound get() = (date != null)
    val timeFound get() = (time != null)
}

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

    private fun List<Any?>.getInt(idx: Int): Int = get(idx) as Int

    private sealed interface ExtractedInfo
    private data class ExtractedDate(val date: Date) : ExtractedInfo
    private data class ExtractedTime(val time: LocalTime) : ExtractedInfo
    private object NoInfo : ExtractedInfo

    private val PATTERNS: List<Pair<List<(Token) -> Any?>, (List<Any?>) -> ExtractedInfo>> = listOf(
        listOf(Token::asHour, { it.filterEqualTo(":") }, Token::asMinute) to
                { args: List<Any?> -> ExtractedTime(LocalTime.of(args.getInt(0), args.getInt(2))) }
    )

    fun parse(str: String): DateTimeExtractionResult {

        val alreadyProcessedTokensWithoutTimeInfo = mutableListOf<Token>()

        data class ExtractionResult(
            val extractedInfo: ExtractedInfo,
            val remaining: List<Token>,
            val consumed: List<Token>
        )

        fun extractDateTimeInfo(tokens: List<Token>): ExtractionResult {
            require(tokens.isNotEmpty())
            return PATTERNS.asSequence()
                .filter { (extractors, _) -> extractors.size <= tokens.size }
                .map { (extractors, createExtractedInfo) ->
                    val curr = tokens.take(extractors.size)
                    val next = tokens.drop(extractors.size)
                    Triple(
                        extractors.zip(curr).map { (extr, tok) -> extr(tok) },
                        createExtractedInfo,
                        Pair(next, curr)
                    )
                }
                .find { (args, _, _) -> args.all { it != null } }
                ?.let { (args, createExtracted, nextAndCurr) ->
                    ExtractionResult(createExtracted(args), nextAndCurr.first, nextAndCurr.second)
                } ?: ExtractionResult(NoInfo, tokens.drop(1), tokens.subList(0, 1))
        }

        tailrec fun recurse(
            remTokens: List<Token>,
            date: Date?,
            time: LocalTime?
        ): Pair<Date?, LocalTime?> =
            if (remTokens.isEmpty()) {
                Pair(date, time)
            } else {
                val (extractedInfo, newRemTokens, consumed) = extractDateTimeInfo(remTokens)
                when (extractedInfo) {
                    is ExtractedDate -> {
                        val newDate = date ?: extractedInfo.date
                        if (date != null) {
                            alreadyProcessedTokensWithoutTimeInfo.addAll(consumed)
                        }
                        recurse(newRemTokens, newDate, time)
                    }
                    is ExtractedTime -> {
                        val newTime = time ?: extractedInfo.time
                        if (time != null) {
                            alreadyProcessedTokensWithoutTimeInfo.addAll(consumed)
                        }
                        recurse(newRemTokens, date, newTime)
                    }
                    is NoInfo -> {
                        alreadyProcessedTokensWithoutTimeInfo.addAll(consumed)
                        recurse(newRemTokens, date, time)
                    }
                }
            }

        val initTokens = Tokenizer.tokenize(str)
        val (date, time) = recurse(initTokens, null, null)
        val text = alreadyProcessedTokensWithoutTimeInfo
            .dropWhile { it is WhitespaceToken }
            .dropLastWhile { it is WhitespaceToken }
            .joinToString(separator = "", transform = Token::str)
        return DateTimeExtractionResult(text, date, time)
    }

}

enum class DayOfWeek {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;

    companion object {
        fun parse(str: String): DayOfWeek? =
            values().find { it.name.lowercase() == str.lowercase() }
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

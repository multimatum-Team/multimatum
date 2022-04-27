package com.github.multimatum_team.multimatum.model.datetime_parser

import java.time.LocalTime
import java.util.*

object DateTimeExtractor {

    /*
        TODO implement date recognition + more patterns for time recognition

        When finished, should be able to parse:

        18h00
        18h
        6am
        6pm
        etc.

        Monday
        next Monday
        on Monday
        etc.
     */

    private fun List<Any?>.getInt(idx: Int): Int = get(idx) as Int

    /**
     * Time information extracted from a string
     * Can be a date, a time or NoInfo if no info was found
     */
    private sealed interface ExtractedInfo
    private data class ExtractedDate(val date: Date) : ExtractedInfo
    private data class ExtractedTime(val time: LocalTime) : ExtractedInfo
    private object NoInfo : ExtractedInfo


    // WARNING: you are about to read one of the most complicated pieces of code of this project
    // DISCLAIMER: this would not be necessary if Kotlin implemented pattern matching properly...
    /**
     * All the patterns that the parser should recognize
     *
     * Implemented as a list of pairs where the first element is an "extractor", a list of functions
     * that map a token to any kind of value. This list is applied to the beginning of a list of
     * tokens by applying the function at index i to the token at index i in the list. A pattern is
     * considered to match the beginning of the list iff all of these function applications return
     * something else than null. If it matches, the (non-null) results of these function applications
     * are used to build a list that is given as an argument to the second element of the pair, that
     * should use it to build an appropriate ExtractedInfo
     *
     * e.g.: for the pair
     *
     *    listOf(
     *        Token::asHour,
     *        { it.filterEqualTo(":") },
     *        Token::asMinute
     *    ) to { args: List<Any?> ->
     *        ExtractedTime(LocalTime.of(args.getInt(0), args.getInt(2)))
     *    }
     *
     * the pattern
     *
     *    listOf(
     *       Token::asHour,
     *       { it.filterEqualTo(":") },
     *       Token::asMinute
     *    )
     *
     * matches the list
     *
     *     listOf(NumericToken("15"), SymbolToken(":"), NumericToken("00"))
     *
     * (obtained by tokenizing the string 15:00)
     *
     * because asHour returns 15, filterEqualTo returns a non-null value (the SymbolToken itself) and
     * asMinute returns 0.
     * Then the "extractor"
     *
     *     args: List<Any?> -> ExtractedTime(LocalTime.of(args.getInt(0), args.getInt(2)))
     *
     * is applied to the list [15, SymbolToken(":"), 0] and builds an ExtractedTime that contains
     * the time 15:00
     */
    private val PATTERNS: List<Pair<List<(Token) -> Any?>, (List<Any?>) -> ExtractedInfo>> = listOf(
        listOf(
            Token::asHour,
            { it.filterEqualTo(":") },
            Token::asMinute
        ) to { args: List<Any?> ->
            ExtractedTime(LocalTime.of(args.getInt(0), args.getInt(2)))
        },
        listOf(
            { it.filterEqualTo("at") },
            Token::filterWhitespace,
            Token::asHour,
            { it.filterEqualTo(":") },
            Token::asMinute
        ) to { args: List<Any?> ->
            ExtractedTime(LocalTime.of(args.getInt(2), args.getInt(4)))
        },
        listOf(
            Token::asHour,
            { it.filterEqualTo("am") }
        ) to { args: List<Any?> ->
            ExtractedTime(LocalTime.of(args.getInt(0), 0))
        },
        listOf(
            Token::asHour,
            { it.filterEqualTo("pm") }
        ) to { args: List<Any?> ->
            ExtractedTime(LocalTime.of(args.getInt(0) + 12, 0))
        }
    )

    /**
     * Analyzes the given string and uses the patterns above to extract date or time info
     * @return a DateTimeExtractionResult, containing:
     * - the title after removing the date and time info
     * - the extracted date, if any was found
     * - the extracted time, if any was found
     */
    fun parse(str: String): DateTimeExtractionResult {

        // this list will be filled with the tokens that do not provide info on date/time
        val alreadyProcessedTokensWithoutTimeInfo = mutableListOf<Token>()

        /**
         * Return value of extractDateTimeInfo
         * @param extractedInfo Time information extracted from the string
         * @param remaining tokens that remain to be processed (tail of the initial tokens list)
         * @param consumed the tokens corresponding to the extracted info (first elements of the tokens list)
         */
        data class ExtractionResult(
            val extractedInfo: ExtractedInfo,
            val remaining: List<Token>,
            val consumed: List<Token>
        )

        /**
         * Attempts to match each of the patterns with the beginning of the list
         */
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

        /**
         * Iterates on the list of tokens
         */
        tailrec fun recurse(
            remTokens: List<Token>,
            date: Date?,
            time: LocalTime?
        ): Pair<Date?, LocalTime?> =
            if (remTokens.isEmpty()) {
                Pair(date, time)  // end of list, return found info
            } else {
                val (extractedInfo, newRemTokens, consumed) = extractDateTimeInfo(remTokens)
                when (extractedInfo) {
                    is ExtractedDate -> {
                        // if a date has already been found, ignore the newly found one (treat it as normal text)
                        val newDate = date ?: extractedInfo.date
                        if (date != null) {
                            alreadyProcessedTokensWithoutTimeInfo.addAll(consumed)
                        }
                        recurse(newRemTokens, newDate, time)
                    }
                    is ExtractedTime -> {
                        // if a time has already been found, ignore the newly found one (treat it as normal text)
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

        // replaces the multiple whitespaces (e.g. "     ") by a single whitespace
        fun removeMultipleWhitespaces(str: String): String =
            if (str.contains("  ", ignoreCase = true)) {
                removeMultipleWhitespaces(str.replace("  ", " ", ignoreCase = true))
            } else str

        val initTokens = Tokenizer.tokenize(str)
        val (date, time) = recurse(initTokens, null, null)
        val text = alreadyProcessedTokensWithoutTimeInfo
            .dropWhile { it is WhitespaceToken }     // ignore leading whitespaces
            .dropLastWhile { it is WhitespaceToken } // ignore trailing whitespaces
            .joinToString(separator = "", transform = Token::str)
            .let(::removeMultipleWhitespaces)
        return DateTimeExtractionResult(text, date, time)
    }

}

/**
 * Contains the info extracted by parsing
 * @param text the text after removing the parts that contain time/date info
 * @param date the date found in the initial text, if any was found
 * @param time the time found in the initial text if any was found
 */
data class DateTimeExtractionResult(
    val text: String,
    val date: Date? = null,
    val time: LocalTime? = null
) {
    val dateFound get() = (date != null)
    val timeFound get() = (time != null)
}

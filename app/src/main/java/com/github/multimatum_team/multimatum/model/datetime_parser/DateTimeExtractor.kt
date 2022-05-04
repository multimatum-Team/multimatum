package com.github.multimatum_team.multimatum.model.datetime_parser

import java.time.LocalDate
import java.time.LocalTime

typealias Pattern = List<(Token) -> Any?>
typealias Extractor = (List<Any?>) -> ExtractedInfo?

class DateTimeExtractor(private val dateTimePatterns: DateTimePatterns) {
    constructor(currentDateProvider: () -> LocalDate) :
            this(DateTimePatterns(currentDateProvider))

    /**
     * Return value of extractDateTimeInfo
     * @param extractedInfo Time information extracted from the string
     * @param remaining tokens that remain to be processed (tail of the initial tokens list)
     * @param consumed the tokens corresponding to the extracted info (first elements of the
     * initial tokens list)
     */
    private data class ExtractionResult(
        val extractedInfo: ExtractedInfo,
        val remaining: List<Token>,
        val consumed: List<Token>
    )

    /**
     * Filters out the patterns involving more tokens than the pattern
     */
    private fun Sequence<Pair<Pattern, Extractor>>.filterForMaxLength(
        maxLength: Int
    ) = filter { (pat, _) -> pat.size <= maxLength }

    /**
     * Applies the functions of the patterns to the corresponding tokens
     * @return a sequence of triples, each containing:
     *   1. the list of results of these function applications
     *   2. the extractor corresponding to the pattern
     *   3. a pair containing:
     *      1) the tokens that remain to be consumed by the parser
     *      2) the tokens that the were consumed by this pattern
     */
    private fun Sequence<Pair<Pattern, Extractor>>.matchTokensAgainstPattern(
        tokens: List<Token>
    ) = map { (pattern, createExtractedInfoFunc) ->
        val currentTokens = tokens.take(pattern.size)
        val nextTokens = tokens.drop(pattern.size)
        Triple(
            pattern.zip(currentTokens)
                .map { (extractValueFunc, tok) -> extractValueFunc(tok) },
            createExtractedInfoFunc,
            Pair(nextTokens, currentTokens)
        )
    }

    /**
     * Stops the processing of the patterns that lead to null values when applying functions
     * to corresponding tokens (= pattern not matched)
     */
    private fun Sequence<Triple<List<Any?>, Extractor, Pair<List<Token>, List<Token>>>>.filterAllArgsNotNull() =
        filter { (args, _, _) ->
            args.all { it != null }
        }

    /**
     * Applies the extractors to the result of applying functions to corresponding tokens,
     * producing an ExtractionResult (or null if the pattern failed to match because of
     * an additional condition that is checked in the extractor, this would occur e.g.
     * when parsing "31.02.2000")
     */
    private fun Sequence<Triple<List<Any?>, Extractor, Pair<List<Token>, List<Token>>>>.mapToExtractionResults() =
        map { (args, createExtractedInfoFunc, nextAndCurrTokens) ->
            val extractedInfo = createExtractedInfoFunc(args)
            extractedInfo?.let {
                ExtractionResult(
                    it,
                    nextAndCurrTokens.first,
                    nextAndCurrTokens.second
                )
            }
        }

    /**
     * Attempts to match each of the patterns with the beginning of the list
     */
    private fun extractDateTimeInfo(tokens: List<Token>): ExtractionResult {
        require(tokens.isNotEmpty())
        return dateTimePatterns.patterns
            .sortedByDescending { it.first.size }
            .asSequence()
            .filterForMaxLength(tokens.size)
            .matchTokensAgainstPattern(tokens)
            .filterAllArgsNotNull()
            .mapToExtractionResults()
            .filterNotNull()
            .firstOrNull()
            ?: ExtractionResult(NoInfo, tokens.drop(1), tokens.subList(0, 1))
    }

    private fun firstFoundTimeIfAny(
        extractedInfo: ExtractedInfo,
        previouslySelectedTime: LocalTime?
    ) =
        if (extractedInfo is ExtractedTime && previouslySelectedTime == null)
            extractedInfo.time
        else previouslySelectedTime

    private fun firstFoundDateIfAny(
        extractedInfo: ExtractedInfo,
        previouslySelectedDate: LocalDate?
    ) =
        if (extractedInfo is ExtractedDate && previouslySelectedDate == null)
            extractedInfo.date
        else previouslySelectedDate

    private fun computeTokensToAddToAlreadyProcessedList(
        extractedInfo: ExtractedInfo,
        date: LocalDate?,
        consumed: List<Token>,
        time: LocalTime?
    ) = when (extractedInfo) {
        is ExtractedDate -> if (date == null) listOf(RemovedToken) else consumed
        is ExtractedTime -> if (time == null) listOf(RemovedToken) else consumed
        is NoInfo -> consumed
    }

    /**
     * Iterates on the list of tokens
     */
    private tailrec fun recursivelyParse(
        remTokens: List<Token>,
        date: LocalDate?,
        time: LocalTime?,
        alreadyProcessedTokensList: MutableList<Token>
    ): Pair<LocalDate?, LocalTime?> =
        if (remTokens.isEmpty()) Pair(date, time)  // end of list, return found info
        else {
            val (extractedInfo, newRemTokens, consumed) = extractDateTimeInfo(remTokens)
            /* if date or time already exists, keep already existing one and treat consumed
             * tokens as normal text */
            val newDate = firstFoundDateIfAny(extractedInfo, date)
            val newTime = firstFoundTimeIfAny(extractedInfo, time)
            val newProcessedTokens =
                computeTokensToAddToAlreadyProcessedList(extractedInfo, date, consumed, time)
            alreadyProcessedTokensList.addAll(newProcessedTokens)
            recursivelyParse(newRemTokens, newDate, newTime, alreadyProcessedTokensList)
        }

    /**
     * Set followedByWhitespace to true for each token that is followed by a WhitespaceToken
     */
    private fun markTokensFollowedByWhitespace(tokens: List<Token>) {
        tokens.fold(null as Token?) { prevTok, currTok ->
            if (currTok.isWhitespace()) {
                prevTok?.followedByWhitespace = true
            }
            currTok
        }
    }

    private fun String.removeTrailingWhitespaces(): String =
        dropLastWhile { it.isWhitespace() }

    /**
     * Removes the conjunctions like "at", or "on" that are left after removal of date or time
     * E.g. when parsing "Example at 10am", the "at" is not contained in the pattern, but should
     * still be removed
     */
    private fun danglingConjunctionsRemoved(tokens: List<Token>): List<Token> {
        val remainingTokens = mutableListOf<Token>()
        val lastToken = tokens.fold(null as Token?) { prevTok, currTok ->
            if (prevTok != null
                && (currTok !is RemovedToken
                        || !CONJUNCTIONS_TO_REMOVE_WHEN_DANGLING.contains(prevTok.str))
            ) {
                remainingTokens.add(prevTok)
            }
            currTok
        }
        lastToken?.let { remainingTokens.add(it) }
        return remainingTokens
    }

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

        val initTokens = Tokenizer.tokenize(str)
        markTokensFollowedByWhitespace(initTokens)
        val nonWhiteSpaceTokens = initTokens.filter { !it.isWhitespace() }

        val (date, time) = recursivelyParse(
            nonWhiteSpaceTokens,
            null,
            null,
            alreadyProcessedTokensWithoutTimeInfo
        )
        val text = alreadyProcessedTokensWithoutTimeInfo
            .let(::danglingConjunctionsRemoved)
            .joinToString(separator = "", transform = Token::strWithWhitespaceIfNeeded)
            .removeTrailingWhitespaces()
        return DateTimeExtractionResult(text, date, time)
    }

    companion object {
        private val CONJUNCTIONS_TO_REMOVE_WHEN_DANGLING = listOf(
            "at", "on", "next"
        )
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
    val date: LocalDate? = null,
    val time: LocalTime? = null
) {
    val dateFound get() = (date != null)
    val timeFound get() = (time != null)
}

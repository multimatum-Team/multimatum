package com.github.multimatum_team.multimatum.model.datetime_parser

import java.time.LocalDate
import java.time.LocalTime

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
     * Attempts to match each of the patterns with the beginning of the list
     */
    private fun extractDateTimeInfo(tokens: List<Token>): ExtractionResult {
        require(tokens.isNotEmpty())
        return dateTimePatterns.patterns
            .sortedByDescending { it.first.size }
            .asSequence()
            .filter { (pattern, _) -> pattern.size <= tokens.size }
            .map { (pattern, createExtractedInfoFunc) ->
                val currentTokens = tokens.take(pattern.size)
                val nextTokens = tokens.drop(pattern.size)
                Triple(
                    pattern.zip(currentTokens)
                        .map { (extractValueFunc, tok) -> extractValueFunc(tok) },
                    createExtractedInfoFunc,
                    Pair(nextTokens, currentTokens)
                )
            }
            .filter { (args, _, _) ->
                args.all { it != null }
            }
            .map { (args, createExtractedInfoFunc, nextAndCurrTokens) ->
                val extractedInfo = createExtractedInfoFunc(args)
                extractedInfo?.let {
                    ExtractionResult(
                        it,
                        nextAndCurrTokens.first,
                        nextAndCurrTokens.second
                    )
                }
            }
            .find { it != null }
            ?: ExtractionResult(NoInfo, tokens.drop(1), tokens.subList(0, 1))
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
        if (remTokens.isEmpty()) {
            Pair(date, time)  // end of list, return found info
        } else {
            val (extractedInfo, newRemTokens, consumed) = extractDateTimeInfo(remTokens)
            when (extractedInfo) {
                is ExtractedDate -> {
                    // if a date has already been found, ignore the newly found one (treat it as normal text)
                    alreadyProcessedTokensList.addAll(if (date == null) listOf(RemovedToken) else consumed)
                    val newDate = date ?: extractedInfo.date
                    recursivelyParse(newRemTokens, newDate, time, alreadyProcessedTokensList)
                }
                is ExtractedTime -> {
                    // if a time has already been found, ignore the newly found one (treat it as normal text)
                    alreadyProcessedTokensList.addAll(if (time == null) listOf(RemovedToken) else consumed)
                    val newTime = time ?: extractedInfo.time
                    recursivelyParse(newRemTokens, date, newTime, alreadyProcessedTokensList)
                }
                is NoInfo -> {
                    alreadyProcessedTokensList.addAll(consumed)
                    recursivelyParse(newRemTokens, date, time, alreadyProcessedTokensList)
                }
            }
        }

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

    private fun removeDanglingConjunctions(tokens: List<Token>): List<Token> {
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
            .let(::removeDanglingConjunctions)
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

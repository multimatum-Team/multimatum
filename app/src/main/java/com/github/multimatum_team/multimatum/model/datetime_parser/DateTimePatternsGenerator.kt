package com.github.multimatum_team.multimatum.model.datetime_parser

import java.time.*
import java.time.temporal.TemporalAdjusters

/**
 * Contains all the patterns that the parser should recognize
 *
 * Implemented as a list of pairs where the first element is an "extractor", a list of functions
 * that map a token to any kind of value. This list is applied to the beginning of a list of
 * tokens by applying the function at index i to the token at index i in the list. A pattern is
 * considered to match the beginning of the list iff all of these function applications return
 * something else than null. If it matches, the (non-null) results of these function applications
 * are used to build a list that is given as an argument to the second element of the pair, that
 * should use it to build an appropriate ExtractedInfo. If this ExtractedInfo is null, search for
 * a matching pattern will resume, until no pattern is left.
 *
 * E.g.: for the pair
 *
 *    listOf(
 *       Token::asHour,
 *       tokenMatching(":"),
 *       Token::asMinute
 *    ) to { args: List<Any?> ->
 *       ExtractedTime(LocalTime.of(args.getInt(0), args.getInt(2)))
 *    }
 *
 * the pattern
 *
 *    listOf(
 *       Token::asHour,
 *       tokenMatching(":"),
 *       Token::asMinute
 *    )
 *
 * matches the list
 *
 *     listOf(NumericToken("15"), SymbolToken(":"), NumericToken("00"))
 *
 * (obtained by tokenizing the string 15:00)
 *
 * because asHour returns 15, the function created by tokenMatching(":") returns a non-null
 * value (the SymbolToken itself) and asMinute returns 0.
 *
 * Then the "extractor"
 *
 *     args: List<Any?> -> ExtractedTime(LocalTime.of(args.getInt(0), args.getInt(2)))
 *
 * is applied to the list [15, SymbolToken(":"), 0] and builds an ExtractedTime that contains
 * the time 15:00
 */
@Suppress("PrivatePropertyName")
class DateTimePatternsGenerator(private val currentDateProvider: () -> LocalDate) {

    /**
     * @return a function that takes a token and returns it if it contains the provided string,
     * null o.w.
     */
    private fun tokenMatchingOneOf(vararg cmpStrings: String): (Token) -> Token? {
        require(cmpStrings.isNotEmpty())
        return { tok: Token ->
            if (cmpStrings.map(String::lowercase).contains(tok.str.lowercase())) tok else null
        }
    }

    private fun List<Any?>.getInt(idx: Int): Int = get(idx) as Int
    private fun List<Any?>.getDayOfWeek(idx: Int): DayOfWeek = get(idx) as DayOfWeek
    private fun List<Any?>.getMonth(idx: Int): Month = get(idx) as Month

    private fun timeFor(hour: Int, minute: Int): ExtractedTime =
        ExtractedTime(LocalTime.of(hour, minute))

    /**
     * @return an instance of LocalDate if the given date is valid, null o.w.
     */
    private fun localDateForIfExists(year: Int, month: Month, day: Int): LocalDate? {
        val yearMonth = YearMonth.of(year, month)
        return if (yearMonth.isValidDay(day))
            LocalDate.of(year, month, day)
        else null
    }

    private fun dateForIfExists(year: Int, month: Month, day: Int): ExtractedDate? =
        localDateForIfExists(year, month, day)?.let(::ExtractedDate)

    /**
     * @return the next date (after the current one) matching the given day of week
     * (e.g. for expressions like "next monday")
     */
    private fun dateForNextDayMatching(requestedDayOfWeek: DayOfWeek): ExtractedDate {
        val currDate = currentDateProvider()
        val nextMatchingDay = currDate.with(TemporalAdjusters.next(requestedDayOfWeek))
        return ExtractedDate(nextMatchingDay)
    }

    // Patterns

    private val `15h` =
        listOf(
            Token::asHour24,
            tokenMatchingOneOf("h", "hour")
        ) to { args: List<Any?> ->
            timeFor(hour = args.getInt(0), 0)
        }

    private val `15h00` =
        listOf(
            Token::asHour24,
            Token::asTimeSeparator,
            Token::asMinute
        ) to { args: List<Any?> ->
            timeFor(hour = args.getInt(0), minute = args.getInt(2))
        }

    private val `3am` =
        listOf(
            Token::asHour12,
            tokenMatchingOneOf("am")
        ) to { args: List<Any?> ->
            timeFor(hour = args.getInt(0), minute = 0)
        }

    private val `3pm` =
        listOf(
            Token::asHour12,
            tokenMatchingOneOf("pm")
        ) to { args: List<Any?> ->
            timeFor(hour = args.getInt(0) + 12, minute = 0)
        }

    private val `1-01-2000` =
        listOf(
            Token::asPossibleDayOfMonthIndex,
            Token::asDateSeparator,
            Token::asMonth,
            Token::asDateSeparator,
            Token::asPossibleYear
        ) to { args: List<Any?> ->
            dateForIfExists(year = args.getInt(4), month = args.getMonth(2), day = args.getInt(0))
        }

    private val `2000-1-1` =
        listOf(
            Token::asPossibleYear,
            Token::asDateSeparator,
            Token::asMonth,
            Token::asDateSeparator,
            Token::asPossibleDayOfMonthIndex
        ) to { args: List<Any?> ->
            dateForIfExists(day = args.getInt(4), month = args.getMonth(2), year = args.getInt(0))
        }

    private val `1_01_2000` =
        listOf(
            Token::asPossibleDayOfMonthIndex,
            Token::asMonth,
            Token::asPossibleYear
        ) to { args: List<Any?> ->
            dateForIfExists(day = args.getInt(0), month = args.getMonth(1), year = args.getInt(2))
        }

    private val monday =
        listOf(
            Token::asDayOfWeek
        ) to { args: List<Any?> ->
            val requestedDayOfWeek = args.getDayOfWeek(0)
            dateForNextDayMatching(requestedDayOfWeek)
        }

    private val monday_15 =
        listOf(
            Token::asDayOfWeek,
            Token::asPossibleDayOfMonthIndex
        ) to { args: List<Any?> ->
            val targetDayOfWeek = args.getDayOfWeek(0)
            val dayOfMonthIdx = args.getInt(1)
            val currentDate = currentDateProvider()
            val targetDate =
                localDateForIfExists(currentDate.year, currentDate.month, dayOfMonthIdx)
            targetDate?.let {
                if (targetDate.dayOfWeek == targetDayOfWeek) ExtractedDate(targetDate)
                else null
            }
        }

    private val midday =
        listOf(
            tokenMatchingOneOf("noon", "midday")
        ) to { _: List<Any?> ->
            timeFor(12, 0)
        }

    private val midnight =
        listOf(
            tokenMatchingOneOf("midnight")
        ) to { _: List<Any?> ->
            timeFor(0, 0)
        }

    val patterns: List<PatternMatchCase> =
        listOf(
            `15h`,
            `15h00`,
            `3am`,
            `3pm`,
            `1-01-2000`,
            `2000-1-1`,
            `1_01_2000`,
            monday,
            monday_15,
            midday,
            midnight
        ).map { (pattern, extractionFunc) ->
            PatternMatchCase(pattern, extractionFunc)
        }

}
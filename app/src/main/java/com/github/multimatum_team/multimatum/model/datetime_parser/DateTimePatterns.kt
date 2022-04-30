package com.github.multimatum_team.multimatum.model.datetime_parser

import java.time.*
import java.time.temporal.TemporalAdjusters

// TODO update doc

/**
 * Contains all the patterns that the parser should recognize
 *
 * Implemented as a list of pairs where the first element is an "extractor", a list of functions
 * that map a token to any kind of value. This list is applied to the beginning of a list of
 * tokens by applying the function at index i to the token at index i in the list. A pattern is
 * considered to match the beginning of the list iff all of these function applications return
 * something else than null. If it matches, the (non-null) results of these function applications
 * are used to build a list that is given as an argument to the second element of the pair, that
 * should use it to build an appropriate ExtractedInfo
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
class DateTimePatterns(private val currentDateProvider: () -> LocalDate) {

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

    private fun dateFor(year: Int, month: Month, day: Int): ExtractedDate? {
        val yearMonth = YearMonth.of(year, month)
        return if (yearMonth.isValidDay(day))
            ExtractedDate(LocalDate.of(year, month, day))
        else null
    }

    private fun dateForNextDayMatching(requestedDayOfWeek: DayOfWeek): ExtractedDate {
        val currDate = currentDateProvider()
        val nextMatchingDay = currDate.with(TemporalAdjusters.next(requestedDayOfWeek))
        return ExtractedDate(nextMatchingDay)
    }

    private val simpleTimePat =
        listOf(
            Token::asHour,
            Token::asTimeSeparator,
            Token::asMinute
        ) to { args: List<Any?> ->
            timeFor(hour = args.getInt(0), minute = args.getInt(2))
        }

    private val atTimePat =
        listOf(
            tokenMatchingOneOf("at"),
            Token::asHour,
            Token::asTimeSeparator,
            Token::asMinute
        ) to { args: List<Any?> ->
            timeFor(hour = args.getInt(1), minute = args.getInt(3))
        }

    private val amTimePat =
        listOf(
            Token::asHour,
            tokenMatchingOneOf("am")
        ) to { args: List<Any?> ->
            timeFor(hour = args.getInt(0), minute = 0)
        }

    private val pmTimePat =
        listOf(
            Token::asHour,
            tokenMatchingOneOf("pm")
        ) to { args: List<Any?> ->
            timeFor(hour = args.getInt(0) + 12, minute = 0)
        }

    private val completeDateDayMonthYearDotsWithSepPat =
        listOf(
            Token::asPossibleDayOfMonthIndex,
            Token::asDateSeparator,
            Token::asMonth,
            Token::asDateSeparator,
            Token::asPossibleYear
        ) to { args: List<Any?> ->
            dateFor(year = args.getInt(4), month = args.getMonth(2), day = args.getInt(0))
        }

    private val completeDateYearMonthDayWithSepPat =
        listOf(
            Token::asPossibleYear,
            Token::asDateSeparator,
            Token::asMonth,
            Token::asDateSeparator,
            Token::asPossibleDayOfMonthIndex
        ) to { args: List<Any?> ->
            dateFor(day = args.getInt(4), month = args.getMonth(2), year = args.getInt(0))
        }

    private val completeDateDayMonthYearNoSepPat =
        listOf(
            Token::asPossibleDayOfMonthIndex,
            Token::asMonth,
            Token::asPossibleYear
        ) to {args: List<Any?> ->
            dateFor(day = args.getInt(0), month = args.getMonth(1), year = args.getInt(2))
        }

    private val dayOfWeekDatePat =
        listOf(
            Token::asDayOfWeek
        ) to { args: List<Any?> ->
            val requestedDayOfWeek = args.getDayOfWeek(0)
            dateForNextDayMatching(requestedDayOfWeek)
        }

    private val onDayOfWeekDatePat =
        listOf(
            tokenMatchingOneOf("on", "next"),
            Token::asDayOfWeek
        ) to { args: List<Any?> ->
            val requestedDayOfWeek = args.getDayOfWeek(1)
            dateForNextDayMatching(requestedDayOfWeek)
        }

    val patterns: List<Pair<List<(Token) -> Any?>, (List<Any?>) -> ExtractedInfo?>> =
        listOf(
            simpleTimePat,
            atTimePat,
            amTimePat,
            pmTimePat,
            completeDateDayMonthYearDotsWithSepPat,
            completeDateYearMonthDayWithSepPat,
            completeDateDayMonthYearNoSepPat,
            dayOfWeekDatePat,
            onDayOfWeekDatePat
        )

}
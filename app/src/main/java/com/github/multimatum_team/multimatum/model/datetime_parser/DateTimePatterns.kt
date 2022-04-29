package com.github.multimatum_team.multimatum.model.datetime_parser

import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

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
object DateTimePatterns {

    /**
     * @return a function that takes a token and returns it if it contains the provided string,
     * null o.w.
     */
    private fun tokenMatching(cmpStr: String) =
        { tok: Token -> if (tok.str == cmpStr) tok else null }

    private fun List<Any?>.getInt(idx: Int): Int = get(idx) as Int

    private val SIMPLE_DATE_PAT =
        listOf(
            Token::asHour,
            tokenMatching(":"),
            Token::asMinute
        ) to { args: List<Any?> ->
            ExtractedTime(LocalTime.of(args.getInt(0), args.getInt(2)))
        }

    private val AT_TIME_PAT =
        listOf(
            tokenMatching("at"),
            Token::asHour,
            tokenMatching(":"),
            Token::asMinute
        ) to { args: List<Any?> ->
            ExtractedTime(LocalTime.of(args.getInt(1), args.getInt(3)))
        }

    private val AM_TIME_PAT =
        listOf(
            Token::asHour,
            tokenMatching("am")
        ) to { args: List<Any?> ->
            ExtractedTime(LocalTime.of(args.getInt(0), 0))
        }

    private val PM_TIME_PAT =
        listOf(
            Token::asHour,
            tokenMatching("pm")
        ) to { args: List<Any?> ->
            ExtractedTime(LocalTime.of(args.getInt(0) + 12, 0))
        }

    private val COMPLETE_DATE_DAY_MONTH_YEAR_PAT =
        listOf(
            Token::asPossibleDayOfMonthIndex,
            tokenMatching("."),
            Token::asMonthIndex,
            tokenMatching("."),
            Token::asPossibleYear
        ) to { args: List<Any?> ->
            val dayOfMonth = args.getInt(0)
            val monthIdx = args.getInt(2)
            val year = args.getInt(4)
            val yearMonth = YearMonth.of(year, monthIdx)
            if (yearMonth.isValidDay(dayOfMonth)) ExtractedDate(
                LocalDate.of(
                    year,
                    monthIdx,
                    dayOfMonth
                )
            ) else null
        }

    private val COMPLETE_DATE_YEAR_MONTH_DATE =
        listOf(
            Token::asPossibleYear,
            tokenMatching("."),
            Token::asMonthIndex,
            tokenMatching("."),
            Token::asPossibleDayOfMonthIndex
        ) to { args: List<Any?> ->
            val dayOfMonth = args.getInt(4)
            val monthIdx = args.getInt(2)
            val year = args.getInt(0)
            val yearMonth = YearMonth.of(year, monthIdx)
            if (yearMonth.isValidDay(dayOfMonth)) ExtractedDate(
                LocalDate.of(
                    year,
                    monthIdx,
                    dayOfMonth
                )
            ) else null
        }

    val PATTERNS: List<Pair<List<(Token) -> Any?>, (List<Any?>) -> ExtractedInfo?>> =
        listOf(
            SIMPLE_DATE_PAT,
            AT_TIME_PAT,
            AM_TIME_PAT,
            PM_TIME_PAT,
            COMPLETE_DATE_DAY_MONTH_YEAR_PAT,
            COMPLETE_DATE_YEAR_MONTH_DATE
        )

}
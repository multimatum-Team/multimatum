package com.github.multimatum_team.multimatum.model.datetime_parser

import java.time.*
import java.time.temporal.TemporalAdjusters
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

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
     * Indicates that a property is a pattern
     *
     * Property should be of type Pair<List<(Token) -> Any?>, (List<Any?>) -> ExtractedTime>
     */
    @Target(AnnotationTarget.PROPERTY)
    private annotation class PatternPair

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
     * To be used instead of LocalDate when the date may be invalid (e.g. 29.02.2021)
     */
    private data class PossiblyInvalidDate(val day: Int, val month: Month, val year: Int) :
        Comparable<LocalDate> {

        fun compareTo(other: PossiblyInvalidDate): Int {
            val (otherDay, otherMonth, otherYear) = other
            fun compDay() = day.compareTo(otherDay)
            fun compMonth() = month.compareTo(otherMonth)
            fun compYear() = year.compareTo(otherYear)
            return sequenceOf(compYear(), compMonth(), compDay())
                .firstOrNull { it != 0 } ?: 0
        }

        override fun compareTo(other: LocalDate): Int {
            val otherAsPossiblyInvalidDate =
                PossiblyInvalidDate(day = other.dayOfMonth, month = other.month, year = other.year)
            return compareTo(otherAsPossiblyInvalidDate)
        }
    }

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

    private fun dateForIfExistsInferringYear(month: Month, day: Int): ExtractedDate? {
        // dates may not be valid, so do not use LocalDate
        val currDate = currentDateProvider()
        val requiredDayThisYear =
            PossiblyInvalidDate(year = currDate.year, month = month, day = day)
        val targetDate =
            if (requiredDayThisYear >= currDate) requiredDayThisYear
            else PossiblyInvalidDate(
                day = day,
                month = month,
                year = currDate.year + 1
            )
        return dateForIfExists(
            year = targetDate.year,
            month = targetDate.month,
            day = targetDate.day
        )
    }

    /**
     * @return the next date (after the current one) matching the given day of week
     * (e.g. for expressions like "next monday")
     */
    private fun dateForNextDayMatching(requestedDayOfWeek: DayOfWeek): ExtractedDate {
        val currDate = currentDateProvider()
        val nextMatchingDay = currDate.with(TemporalAdjusters.next(requestedDayOfWeek))
        return ExtractedDate(nextMatchingDay)
    }

    private val timeSeparator = tokenMatchingOneOf("h", ":")
    private val dateSeparator = tokenMatchingOneOf(".", "/", "-")
    private val am = tokenMatchingOneOf("am")
    private val pm = tokenMatchingOneOf("pm")

    // Patterns

    @PatternPair
    private val `15h` =
        listOf(
            Token::asHour24,
            tokenMatchingOneOf("h", "hour")
        ) to { args: List<Any?> ->
            timeFor(hour = args.getInt(0), 0)
        }

    @PatternPair
    private val `15h00` =
        listOf(
            Token::asHour24,
            timeSeparator,
            Token::asMinute
        ) to { args: List<Any?> ->
            timeFor(hour = args.getInt(0), minute = args.getInt(2))
        }

    @PatternPair
    private val `3am` =
        listOf(
            Token::asHour12,
            am
        ) to { args: List<Any?> ->
            timeFor(hour = args.getInt(0), minute = 0)
        }

    @PatternPair
    private val `3pm` =
        listOf(
            Token::asHour12,
            pm
        ) to { args: List<Any?> ->
            timeFor(hour = args.getInt(0) + 12, minute = 0)
        }

    @PatternPair
    private val `3h00am` =
        listOf(
            Token::asHour12,
            timeSeparator,
            Token::asMinute,
            am
        ) to { args: List<Any?> ->
            timeFor(hour = args.getInt(0), minute = args.getInt(2))
        }

    @PatternPair
    private val `3h00pm` =
        listOf(
            Token::asHour12,
            timeSeparator,
            Token::asMinute,
            pm
        ) to { args: List<Any?> ->
            timeFor(hour = args.getInt(0) + 12, minute = args.getInt(2))
        }

    @PatternPair
    private val `1-01-2000` =
        listOf(
            Token::asPossibleDayOfMonthIndex,
            dateSeparator,
            Token::asMonth,
            dateSeparator,
            Token::asPossibleYear
        ) to { args: List<Any?> ->
            dateForIfExists(year = args.getInt(4), month = args.getMonth(2), day = args.getInt(0))
        }

    @PatternPair
    private val `2000-1-1` =
        listOf(
            Token::asPossibleYear,
            dateSeparator,
            Token::asMonth,
            dateSeparator,
            Token::asPossibleDayOfMonthIndex
        ) to { args: List<Any?> ->
            dateForIfExists(day = args.getInt(4), month = args.getMonth(2), year = args.getInt(0))
        }

    @PatternPair
    private val `1_01_2000` =
        listOf(
            Token::asPossibleDayOfMonthIndex,
            Token::asMonth,
            Token::asPossibleYear
        ) to { args: List<Any?> ->
            dateForIfExists(day = args.getInt(0), month = args.getMonth(1), year = args.getInt(2))
        }

    @PatternPair
    private val `2000_1_1` =
        listOf(
            Token::asPossibleYear,
            Token::asMonth,
            Token::asPossibleDayOfMonthIndex
        ) to { args: List<Any?> ->
            dateForIfExists(year = args.getInt(0), month = args.getMonth(1), day = args.getInt(2))
        }

    @PatternPair
    private val `1-January` =
        listOf(
            Token::asPossibleDayOfMonthIndex,
            dateSeparator,
            Token::asMonth
        ) to { args: List<Any?> ->
            dateForIfExistsInferringYear(day = args.getInt(0), month = args.getMonth(2))
        }

    @PatternPair
    private val January_1 =
        listOf(
            Token::asMonth,
            dateSeparator,
            Token::asPossibleDayOfMonthIndex
        ) to { args: List<Any?> ->
            dateForIfExistsInferringYear(month = args.getMonth(0), day = args.getInt(2))
        }

    @PatternPair
    private val January_1st =
        listOf(
            Token::asMonth,
            Token::asPossibleDayOfMonthIndex,
            tokenMatchingOneOf("st", "nd", "rd", "th")
        ) to { args: List<Any?> ->
            dateForIfExistsInferringYear(month = args.getMonth(0), day = args.getInt(1))
        }

    @PatternPair
    private val monday =
        listOf(
            Token::asDayOfWeek
        ) to { args: List<Any?> ->
            val requestedDayOfWeek = args.getDayOfWeek(0)
            dateForNextDayMatching(requestedDayOfWeek)
        }

    @PatternPair
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

    @PatternPair
    private val midday =
        listOf(
            tokenMatchingOneOf("noon", "midday")
        ) to { _: List<Any?> ->
            timeFor(12, 0)
        }

    @PatternPair
    private val midnight =
        listOf(
            tokenMatchingOneOf("midnight")
        ) to { _: List<Any?> ->
            timeFor(0, 0)
        }

    @PatternPair
    private val tomorrow =
        listOf(
            tokenMatchingOneOf("tomorrow")
        ) to { _: List<Any?> ->
            ExtractedDate(currentDateProvider().plusDays(1))
        }

    // all the patterns, i.e. all the properties marked with @PatternPair
    val patterns: List<PatternMatchCase> =
        @Suppress("UNCHECKED_CAST")
        /* Cannot check the type parameters in cast
         * This may cause a crash when this class is instantiated if @PatternPair is used
         * on something else than the type given in its documentation */
        this::class.memberProperties
            .filter { it.findAnnotation<PatternPair>() != null }
            .map {
                it.getter.isAccessible = true
                val (pattern, extractionFunc) = it.getter.call(this)
                        as Pair<List<(Token) -> Any?>, (List<Any?>) -> ExtractedInfo>
                it.getter.isAccessible = false
                PatternMatchCase(pattern, extractionFunc, it.name)
            }

}
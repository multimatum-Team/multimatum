package com.github.multimatum_team.multimatum.model.datetime_parser

import java.time.DayOfWeek
import java.time.Month

infix fun String.equalsIgnoreCase(that: String): Boolean = this.lowercase() == that.lowercase()

/**
 * Chunk of text, built of one or several similar characters (e.g. all letters, all digits)
 */
sealed class Token {

    abstract val str: String

    var followedByWhitespace: Boolean = false

    // The following methods are default implementations, they are meant to be overridden
    /**
     * @return this if it is a WhitespaceToken, null o.w.
     */
    open fun filterWhitespace(): Token? = null

    fun isWhitespace(): Boolean = (filterWhitespace() != null)

    /**
     * @return the numeric value of this token if it is numeric and if it is acceptable
     * as an hour (0..23), null o.w.
     */
    open fun asHour24(): Int? = null

    open fun asHour12(): Int? = null

    /**
     * @return the numeric value of this token if it is numeric and if it is acceptable
     * as a minute (0..59), null o.w.
     */
    open fun asMinute(): Int? = null

    open fun asPossibleDayOfMonthIndex(): Int? = null

    open fun asMonth(): Month? = null

    open fun asPossibleYear(): Int? = null

    open fun asDayOfWeek(): DayOfWeek? = null

    open fun asDateSeparator(): Token? = null
    open fun asTimeSeparator(): Token? = null

    fun strWithWhitespaceIfNeeded(): String =
        if (followedByWhitespace) "$str "
        else str

}

/**
 * Token containing only letters
 */
data class AlphabeticToken(override val str: String) : Token() {
    override fun asDayOfWeek(): DayOfWeek? =
        DayOfWeek.values().find { it.name equalsIgnoreCase str }

    override fun asMonth(): Month? =
        Month.values().find {
            it.name equalsIgnoreCase str  // match full month name (e.g. march)
                    || it.name.substring(0, 3) equalsIgnoreCase str  // or 3 letters code (e.g. mar)
        }
}

/**
 * Token containing only digits
 */
data class NumericToken(override val str: String) : Token() {
    init {
        require(str.all(Char::isDigit))
    }

    val numericValue: Int get() = str.toInt()

    private fun inRangeOrNull(range: IntRange): Int? =
        if (numericValue in range) numericValue else null

    override fun asHour24(): Int? = inRangeOrNull(0..23)
    override fun asHour12(): Int? = inRangeOrNull(1..11)
    override fun asMinute(): Int? = inRangeOrNull(0..59)
    override fun asPossibleDayOfMonthIndex(): Int? = inRangeOrNull(1..31)
    override fun asMonth(): Month? = if (numericValue in 1..12) Month.of(numericValue) else null
    override fun asPossibleYear(): Int? = inRangeOrNull(1900..2999)
}

/**
 * Token containing a (single) symbol
 */
data class SymbolToken(override val str: String) : Token() {
    init {
        require(str.length == 1)
    }

    private val dateSeparators = listOf('.', '/', '-')
    private val timeSeparators = listOf(':', 'h')

    val charValue: Char get() = str[0]

    override fun asDateSeparator(): Token? = if (dateSeparators.contains(charValue)) this else null
    override fun asTimeSeparator(): Token? = if (timeSeparators.contains(charValue)) this else null
}

/**
 * Token for whitespaces
 */
object WhitespaceToken : Token() {
    override val str = " "
    override fun toString(): String = "WhitespaceToken"
    override fun filterWhitespace(): Token = this
}

/**
 * Token indicating that there was initially another token at its place, that was removed
 */
object RemovedToken : Token(){
    override val str: String = ""
    override fun toString(): String = "RemovedToken"
}


object Tokenizer {

    private val TOKEN_CREATION_FUNCS = listOf(
        Char::isLetter to ::AlphabeticToken,
        Char::isDigit to ::NumericToken,
        Char::isWhitespace to { _: String -> WhitespaceToken }
    )

    /**
     * @return a pair (leading token, rest of the string)
     */
    private fun extractLeadingToken(rem: String): Pair<Token?, String> =
        if (rem.isEmpty()) {
            Pair(null, "")
        } else {
            val headChar = rem[0]
            val (matchChar, createToken) = TOKEN_CREATION_FUNCS.find { (matchChar, _) ->
                matchChar(headChar)
            } ?: Pair({ _: Char -> false }, ::SymbolToken)
            val tailChars = rem.substring(1)
            val tokenTextTail = tailChars.takeWhile(matchChar)
            val newRem = tailChars.dropWhile(matchChar)
            val s = headChar + tokenTextTail
            Pair(createToken(s), newRem)
        }

    /**
     * Transforms the given string into a list of tokens that correspond to the type of characters
     * that they contain
     */
    fun tokenize(str: String): List<Token> {

        val tokens = mutableListOf<Token>()

        /**
         * Iterates on the string and adds the tokens to the list
         */
        tailrec fun recurse(rem: String) {
            if (rem.isNotEmpty()) {
                val (token, newRem) = extractLeadingToken(rem)
                tokens.add(token!!)
                recurse(newRem)
            }
        }

        recurse(str)
        return tokens
    }

}

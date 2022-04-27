package com.github.multimatum_team.multimatum.model.datetime_parser

/**
 * Chunk of text, built of one or several similar characters (e.g. all letters, all digits)
 */
sealed interface Token {

    val str: String

    /**
     * @return this if its str matches the provided one, null o.w.
     */
    fun filterEqualTo(cmpStr: String): Token? = if (cmpStr == str) this else null

    // The following methods are default implementations, they are meant to be overridden
    /**
     * @return this if it is a WhitespaceToken, null o.w.
     */
    fun filterWhitespace(): Token? = null

    /**
     * @return the numeric value of this token if it is numeric and if it is acceptable
     * as an hour (0..23), null o.w.
     */
    fun asHour(): Int? = null

    /**
     * @return the numeric value of this token if it is numeric and if it is acceptable
     * as a minute (0..59), null o.w.
     */
    fun asMinute(): Int? = null

}

/**
 * Token containing only letters
 */
data class AlphabeticToken(override val str: String) : Token

/**
 * Token containing only digits
 */
data class NumericToken(override val str: String) : Token {
    init {
        require(str.all(Char::isDigit))
    }

    val numericValue: Int get() = str.toInt()
    override fun asHour(): Int? = if (numericValue in 0..23) numericValue else null
    override fun asMinute(): Int? = if (numericValue in 0..59) numericValue else null
}

/**
 * Token containing a (single) symbol
 */
data class SymbolToken(override val str: String) : Token {
    init {
        require(str.length == 1)
    }

    val charValue: Char get() = str[0]
}

/**
 * Token for whitespaces
 */
object WhitespaceToken : Token {
    override val str = " "
    override fun toString(): String = "WhitespaceToken"
    override fun filterWhitespace(): Token = this
}


object Tokenizer {

    private val TOKEN_CREATION_FUNCS = listOf(
        Char::isLetter to ::AlphabeticToken,
        Char::isDigit to ::NumericToken,
        Char::isWhitespace to { _: String -> WhitespaceToken }
    )

    /**
     * Transforms the given string into a list of tokens that correspond to the type of characters
     * that they contain
     */
    fun tokenize(str: String): List<Token> {

        val tokens = mutableListOf<Token>()

        /**
         * @return a pair (leading token, rest of the string)
         */
        fun extractLeadingToken(rem: String): Pair<Token?, String> =
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

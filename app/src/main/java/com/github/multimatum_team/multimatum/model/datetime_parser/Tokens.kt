package com.github.multimatum_team.multimatum.model.datetime_parser

sealed interface Token {

    val str: String

    fun filterEqualTo(cmpStr: String): Token? = if (cmpStr == str) this else null

    fun asHour(): Int? = null
    fun asMinute(): Int? = null

}

data class AlphabeticToken(override val str: String) : Token {

}

data class NumericToken(override val str: String) : Token {
    init {
        require(str.all(Char::isDigit))
    }
    val numericValue: Int get() = str.toInt()
    override fun asHour(): Int? = if (numericValue in 0..23) numericValue else null
    override fun asMinute(): Int? = if (numericValue in 0..59) numericValue else null
}

data class SymbolToken(override val str: String) : Token {
    init {
        require(str.length == 1)
    }
    val charValue: Char get() = str[0]
}

object WhitespaceToken : Token {
    override val str = " "
    override fun toString(): String = "WhitespaceToken"
}

object Tokenizer {

    private val TOKEN_CREATION_FUNCS = listOf(
        Char::isLetter to ::AlphabeticToken,
        Char::isDigit to ::NumericToken,
        Char::isWhitespace to { _: String -> WhitespaceToken }
    )

    fun tokenize(str: String): List<Token> {

        val tokens = mutableListOf<Token>()

        fun consume(rem: String): Pair<Token?, String> =
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

        tailrec fun tokenizeRemaining(rem: String) {
            if (rem.isNotEmpty()) {
                val (token, newRem) = consume(rem)
                tokens.add(token!!)
                tokenizeRemaining(newRem)
            }
        }

        tokenizeRemaining(str)
        return tokens
    }

}

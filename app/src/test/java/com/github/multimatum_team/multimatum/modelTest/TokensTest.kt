package com.github.multimatum_team.multimatum.modelTest

import com.github.multimatum_team.multimatum.model.datetime_parser.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class TokensTest {
    @Test
    fun `string is tokenized correctly`() {
        val str = "abcd1234 !/xyz. 987"
        val exp = listOf(
            AlphabeticToken("abcd"),
            NumericToken("1234"),
            WhitespaceToken,
            SymbolToken("!"),
            SymbolToken("/"),
            AlphabeticToken("xyz"),
            SymbolToken("."),
            WhitespaceToken,
            NumericToken("987")
        )
        assertEquals(exp, Tokenizer.tokenize(str))
    }

    @Test
    fun `deadline title including date and time is tokenized correctly`() {
        val title = "Aqua-poney monday 15 at 11am"
        val exp = listOf(
            AlphabeticToken("Aqua"),
            SymbolToken("-"),
            AlphabeticToken("poney"),
            WhitespaceToken,
            AlphabeticToken("monday"),
            WhitespaceToken,
            NumericToken("15"),
            WhitespaceToken,
            AlphabeticToken("at"),
            WhitespaceToken,
            NumericToken("11"),
            AlphabeticToken("am")
        )
        assertEquals(exp, Tokenizer.tokenize(title))
    }

    @Test
    fun `numericToken gives correct numeric value`() {
        val inputsOutputs = listOf(
            "123" to 123,
            "0" to 0,
            "47" to 47
        )
        for ((input, output) in inputsOutputs) {
            assertEquals(output, NumericToken(input).numericValue)
        }
    }

    @Test
    fun `symbolToken gives correct char value`() {
        val inputsOutputs = listOf(
            "!" to '!',
            "*" to '*',
            "@" to '@'
        )
        for ((input, output) in inputsOutputs) {
            assertEquals(output, SymbolToken(input).charValue)
        }
    }

    @Test
    fun `numericToken throws on non numeric input`() {
        assertThrows(IllegalArgumentException::class.java) {
            NumericToken("123a4")
        }
    }

    @Test
    fun `symbolToken throws on input with length different of 1`() {
        assertThrows(IllegalArgumentException::class.java) {
            SymbolToken("[!]")
        }
    }
}
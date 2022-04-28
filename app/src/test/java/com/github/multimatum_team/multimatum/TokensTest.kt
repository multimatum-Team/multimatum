package com.github.multimatum_team.multimatum

import com.github.multimatum_team.multimatum.model.datetime_parser.*
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import java.lang.IllegalArgumentException

class TokensTest {

    @Test
    fun string_is_tokenized_correctly(){
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
    fun deadline_title_including_date_and_time_is_tokenized_correctly(){
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
    fun numericToken_gives_correct_numeric_value(){
        val inputsOutputs = listOf(
            "123" to 123,
            "0" to 0,
            "47" to 47
        )
        for ((input, output) in inputsOutputs){
            assertEquals(output, NumericToken(input).numericValue)
        }
    }

    @Test fun symbolToken_gives_correct_char_value(){
        val inputsOutputs = listOf(
            "!" to '!',
            "*" to '*',
            "@" to '@'
        )
        for ((input, output) in inputsOutputs){
            assertEquals(output, SymbolToken(input).charValue)
        }
    }

    @Test
    fun numericToken_throws_on_non_numeric_input(){
        assertThrows(IllegalArgumentException::class.java){
            NumericToken("123a4")
        }
    }

    @Test
    fun symbolToken_throws_on_input_with_length_different_of_1(){
        assertThrows(IllegalArgumentException::class.java){
            SymbolToken("[!]")
        }
    }

}
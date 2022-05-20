package com.github.multimatum_team.multimatum

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.util.PDFUtil
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*


@RunWith(AndroidJUnit4::class)
class PDFUtilTest {
    @Test
    fun testSelectPDF() {
        PDFUtil.selectPdfIntent {
            assertEquals(Intent.ACTION_GET_CONTENT, it.action)
        }
    }

    @Test
    fun testStrRandom() {
        val input = "someString"
        val out1 = PDFUtil.addRdmCharToStr(input, 16)
        val out2 = PDFUtil.addRdmCharToStr(input, 16)
        assertEquals(out1.length, input.length + 16)
        assert(out1 != out2)
    }
}
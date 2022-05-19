package com.github.multimatum_team.multimatum

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.util.PDFUtil
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.kotlin.mock


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
        val out1 = PDFUtil.addRdmCharToStr(input, 10)
        val out2 = PDFUtil.addRdmCharToStr(input, 10)
        assertEquals(out1.length, input.length+10)
        assert(out1 != out2)
    }
}
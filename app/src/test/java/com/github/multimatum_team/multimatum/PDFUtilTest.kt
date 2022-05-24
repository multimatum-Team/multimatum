package com.github.multimatum_team.multimatum

import android.content.Context
import android.content.Intent
import android.database.MatrixCursor
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.util.PDFUtil
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class PDFUtilTest {
    private var context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun teardown() {
        Intents.release()
    }


    @Test
    fun `selecting a PDF should produce the correct intent`() {
        PDFUtil.selectPdfIntent {
            assertEquals(Intent.ACTION_GET_CONTENT, it.action)
        }
    }

    @Test
    fun `adding a random char to string should work correctly`() {
        val input = "someString"
        val out1 = PDFUtil.addRdmCharToStr(input, 16)
        val out2 = PDFUtil.addRdmCharToStr(input, 16)
        assertEquals(out1.length, input.length + 16)
        assert(out1 != out2)
    }

    @Test
    fun `get empty Uri file name should return empty name`(){
        val input = Uri.EMPTY
        val out = PDFUtil.getFileNameFromUri(input, context)
        assertEquals("", out)
    }

    @Test
    fun `getting name from Uri`(){
        val inputUri: Uri = Uri.parse("fooDir1/fooDir2/someFile.pdf")
        val expectedOutput = "someFile.pdf"
        val cursorColumn: Array<String> = arrayOf("Uri")
        val matrixCursor = MatrixCursor(cursorColumn)
        matrixCursor.addRow(arrayOf("fooDir1/fooDir2/someFile.pdf"))


        val actualOutput = PDFUtil.getFileNameFromUri(inputUri, context)
        assertEquals(expectedOutput, actualOutput)
    }
}
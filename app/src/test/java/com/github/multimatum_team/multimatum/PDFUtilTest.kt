package com.github.multimatum_team.multimatum

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.util.PDFUtil
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.reflect.typeOf

@RunWith(AndroidJUnit4::class)
class PDFUtilTest {
    @Test
    fun testSelectPDF() {
        PDFUtil.selectPdfIntent {
            assertEquals(Intent.ACTION_GET_CONTENT, it.action)
        }
    }

    @Test
    fun testUidGenerator(){
        val uid = PDFUtil.getUniqueInt()
        assert(uid< Int.MAX_VALUE.toString())
    }
}
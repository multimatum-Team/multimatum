package com.github.multimatum_team.multimatum

import android.content.Intent
import android.util.Log
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.activity.MainActivity
import com.github.multimatum_team.multimatum.util.PDFUtil
import kotlinx.coroutines.test.withTestContext
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class PDFUtilTest {

    @Test
    fun testSelectPDF (){
        PDFUtil.selectPdfIntent {
            assertEquals(Intent.ACTION_GET_CONTENT, it.action)
        }
    }
}
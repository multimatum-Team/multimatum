package com.github.multimatum_team.multimatum.service

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.repository.DeadlineID
import com.github.multimatum_team.multimatum.util.GenerateQRCodeUtility
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QRCodeGeneratorTest {

    @Test
    fun `Test right bitmap result for arbitrary string`() {
        val string: DeadlineID = "This is a test"
        val qrGenerator = QRCodeGenerator(string)
        val bitmap = qrGenerator.generateQRCode()
        assertThat(GenerateQRCodeUtility.extractContent(bitmap), `is`("This is a test"))

    }
}
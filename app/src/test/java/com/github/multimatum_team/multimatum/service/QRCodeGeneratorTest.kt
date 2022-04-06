package com.github.multimatum_team.multimatum.service

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.repository.DeadlineID
import com.github.multimatum_team.multimatum.util.QRGeneratorUtilityTest
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QRCodeGeneratorTest: QRGeneratorUtilityTest {

    @Test
    fun `Test right bitmap result for arbitrary string`(){
        val string: DeadlineID = "This is a test"
        val qrGenerator = QRCodeGenerator(string)
        val bitmap = qrGenerator.generateQRCode()
        assertThat(extractContent(bitmap), `is`("This is a test"))

    }
}
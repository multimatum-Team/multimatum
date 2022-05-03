package com.github.multimatum_team.multimatum.util

import android.content.Intent
import androidx.core.app.ActivityCompat.startActivityForResult

object PDFUtil {
    /**
     * Intent for navigating to the files to select PDF
     * from "https://www.section.io/engineering-education/picking-pdf-and-image-from-phone-storage/"
     */
    fun selectPdfIntent(callback: (Intent) -> Unit) {
        val pdfIntent = Intent(Intent.ACTION_GET_CONTENT)
        pdfIntent.type = "application/pdf"
        pdfIntent.addCategory(Intent.CATEGORY_OPENABLE)
        callback(pdfIntent)
    }
}
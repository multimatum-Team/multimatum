package com.github.multimatum_team.multimatum.util

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns


object PDFUtil {
    /**
     * Intent for navigating to the files to select PDF
     */
    fun selectPdfIntent(callback: (Intent) -> Unit) {
        val pdfIntent = Intent(Intent.ACTION_GET_CONTENT)
        pdfIntent.type = "application/pdf"
        pdfIntent.addCategory(Intent.CATEGORY_OPENABLE)
        callback(pdfIntent)
    }
}

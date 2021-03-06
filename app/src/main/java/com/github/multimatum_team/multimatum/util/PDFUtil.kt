package com.github.multimatum_team.multimatum.util

import android.content.Context
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

    /**
     * Retrieve name of a pdf from it's Uri
     */
    fun getFileNameFromUri(pdfData: Uri, context: Context): String {
        val cursor: Cursor? = context.contentResolver.query(pdfData, null, null, null, null)
        val fileName: String
        if (cursor != null) {
            if (cursor.count <= 0) {
                cursor.close()
                throw IllegalArgumentException("Can't obtain file name, cursor is empty")
            }
            cursor.moveToFirst()
            fileName =
                cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            cursor.close()
        } else {
            val pathStr = pdfData.toString()
            fileName = pathStr.substring(pathStr.lastIndexOf('/') + 1)
        }
        return fileName
    }

    fun addRdmCharToStr(input: String, nbChar: Int): String {
        val rdmString = List(nbChar) {
            (('a'..'z') + ('A'..'Z') + ('0'..'9')).random()
        }.joinToString("")
        return rdmString + input
    }
}

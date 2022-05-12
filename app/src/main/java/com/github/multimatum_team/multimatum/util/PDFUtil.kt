package com.github.multimatum_team.multimatum.util

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import com.github.multimatum_team.multimatum.service.ClockService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.StorageReference


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
     * generate a unique ID randomize over Int range
     */
    fun getUniqueInt(): String {
        return (Int.MIN_VALUE..Int.MAX_VALUE).random().toString()
    }

    /**
     * Retrieve name of a pdf from it's Uri
     */
    fun getFileNameFromUri(pdfData: Uri): String {
        val path: String = pdfData.toString()
        val lastSlashIndex = path.lastIndexOf("/")
        return path.substring(lastSlashIndex + 1, path.length)
    }
}

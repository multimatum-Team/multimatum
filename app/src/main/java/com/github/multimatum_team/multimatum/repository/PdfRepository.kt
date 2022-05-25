package com.github.multimatum_team.multimatum.repository

import android.content.Context
import android.net.Uri
import java.io.File

abstract class PdfRepository {
    abstract fun uploadPdf(
        data: Uri,
        context: Context,
        callback: (String, Boolean) -> Unit
    )
    abstract fun delete(path: String)

    abstract fun downloadPdf(
        path: String,
        title: String,
        callback: (File?, Boolean) -> Unit
    )
}
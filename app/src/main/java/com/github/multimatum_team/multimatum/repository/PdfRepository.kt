package com.github.multimatum_team.multimatum.repository

import android.content.Context
import android.net.Uri

abstract class PdfRepository {
    abstract fun uploadPdf(
        data: Uri,
        context: Context,
        callback: (String) -> Unit
    )
    abstract fun delete(path: String)
}
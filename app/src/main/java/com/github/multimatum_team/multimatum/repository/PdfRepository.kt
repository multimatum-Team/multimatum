package com.github.multimatum_team.multimatum.repository

import android.content.Context
import android.net.Uri
import java.io.File

/**
 * An interface for the pdf storage.
 * A minimal implementation of this interface requires defining `download`, `upload` and `delete`
 */
abstract class PdfRepository {

    /**
     * Upload a pdf to firebase storage. Then launch a callback that takes the new file Uri and a boolean whether the upload succeed or not
     */
    abstract fun uploadPdf(
        data: Uri,
        context: Context,
        callback: (String, Boolean) -> Unit,
    )

    /**
     * Delete a file located in the given path
     */
    abstract fun delete(path: String)

    /**
     * Download a pdf to firebase storage. Then launch a callback that takes the new file Uri and a boolean whether the download succeed or not
     */
    abstract fun downloadPdf(
        path: String,
        title: String,
        callback: (File?, Boolean) -> Unit,
    )
}
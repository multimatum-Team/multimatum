package com.github.multimatum_team.multimatum.util

import android.content.Context
import android.net.Uri
import com.github.multimatum_team.multimatum.repository.PdfRepository
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import java.io.File

class MockPdfRepository: PdfRepository() {
    override fun uploadPdf(data: Uri, context: Context, callback: (String, Boolean) -> Unit) {
        doNothing()
    }

    override fun delete(path: String) {
        doNothing()
    }

    override fun downloadPdf(path: String, title: String, callback: (File?, Boolean) -> Unit) {
        callback(any(), true)
    }
}
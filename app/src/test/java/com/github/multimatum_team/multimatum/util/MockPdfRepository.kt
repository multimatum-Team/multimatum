package com.github.multimatum_team.multimatum.util

import android.content.Context
import android.net.Uri
import com.github.multimatum_team.multimatum.repository.PdfRepository
import org.mockito.kotlin.doNothing

class MockPdfRepository: PdfRepository() {
    override fun uploadPdf(data: Uri, context: Context, callback: (String) -> Unit) {
        doNothing()
    }

    override fun delete(path: String) {
        doNothing()
    }
}
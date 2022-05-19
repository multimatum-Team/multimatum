package com.github.multimatum_team.multimatum.repository

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import com.github.multimatum_team.multimatum.LogUtil
import com.github.multimatum_team.multimatum.util.PDFUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import javax.inject.Inject


class FirebasePdfRepository @Inject constructor(database: FirebaseStorage) : PdfRepository() {
    private val storageReference = database.reference

    override fun uploadPdf(
        data: Uri,
        context: Context,
        callback: (String) -> Unit
    ) {
        if (data != Uri.EMPTY) {
            val ref =
                storageReference.child(
                    FirebaseAuth.getInstance().uid + "/" + PDFUtil.addRdmCharToStr(
                        PDFUtil.getFileNameFromUri(
                            data, context
                        ), 16
                    )
                )
            ref.putFile(data).addOnSuccessListener {
                callback(ref.path)
            }.addOnFailureListener {
                val failureDialog =
                    AlertDialog.Builder(context).setTitle("pdf upload failed").show()
                callback("")
            }
        } else {
            callback("")
        }
    }

    override fun delete(path: String) {
        storageReference.child(path).delete()
            .addOnFailureListener { LogUtil.debugLog("PDF has failed to be deleted") }
    }

    override fun downloadPdf(path: String, title: String, callback: (File) -> Unit) {
        val ref = storageReference.child(path)

        val rootPath = File("file_name")
        if (!rootPath.exists()) {
            rootPath.mkdirs()
        }

        val localFile = File.createTempFile(title, ".pdf")

        ref.getFile(localFile).addOnSuccessListener {
            callback(localFile)
            //  updateDb(timestamp,localFile.toString(),position);
        }.addOnFailureListener { exception ->
            LogUtil.debugLog(
                "firebase ;local tem file not created  created $exception"
            )
        }
    }
}
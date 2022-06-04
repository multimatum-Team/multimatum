package com.github.multimatum_team.multimatum.repository

import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import com.github.multimatum_team.multimatum.LogUtil
import com.github.multimatum_team.multimatum.util.PDFUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import javax.inject.Inject

/**
 * Remote Firebase repository for storing pdf files.
 */
class FirebasePdfRepository @Inject constructor(
    database: FirebaseStorage,
    private val auth: FirebaseAuth,
    private val connectivityManager: ConnectivityManager,
) : PdfRepository() {
    private val storageReference = database.reference

    override fun uploadPdf(
        data: Uri,
        context: Context,
        callback: (String, Boolean) -> Unit,
    ) {
        if (data != Uri.EMPTY) {
            val ref =
                storageReference.child(
                    auth.uid + "/" + PDFUtil.addRdmCharToStr(
                        PDFUtil.getFileNameFromUri(
                            data, context
                        ), 16
                    )
                )
            if (isOnline()) {
                ref.putFile(data).addOnSuccessListener {
                    callback(ref.path, true)
                }.addOnFailureListener {
                    AlertDialog.Builder(context).setTitle("pdf upload failed").show()
                    callback("", false)
                }
            } else {
                callback("", false)
            }
        } else {
            callback("", true)
        }
    }

    override fun delete(path: String) {
        storageReference.child(path).delete()
            .addOnFailureListener { LogUtil.debugLog("PDF has failed to be deleted") }
    }

    override fun downloadPdf(path: String, title: String, callback: (File?, Boolean) -> Unit) {
        if (isOnline()) {
            val ref = storageReference.child(path)

            val rootPath = File("file_name")
            if (!rootPath.exists()) {
                rootPath.mkdirs()
            }

            val localFile = File.createTempFile(title, ".pdf")

            ref.getFile(localFile).addOnSuccessListener {
                callback(localFile, true)
                //  updateDb(timestamp,localFile.toString(),position);
            }.addOnFailureListener { exception ->
                LogUtil.debugLog(
                    "firebase ;local tem file not created  created $exception"
                )
            }
        } else {
            callback(null, false)
        }
    }

    private fun isOnline(): Boolean {
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            return true
        }
        return false
    }
}
package com.github.multimatum_team.multimatum.repository

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import com.github.multimatum_team.multimatum.LogUtil
import com.github.multimatum_team.multimatum.util.PDFUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
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
                    FirebaseAuth.getInstance().uid + "/upload" + (Int.MIN_VALUE..Int.MAX_VALUE).random().toString() + PDFUtil.getFileNameFromUri(
                        data
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

    override fun delete(path: String){
        storageReference.child(path).delete()
            .addOnFailureListener { LogUtil.debugLog("PDF has failed to be deleted") }
    }
}
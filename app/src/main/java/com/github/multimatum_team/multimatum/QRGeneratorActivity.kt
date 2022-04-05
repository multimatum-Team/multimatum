package com.github.multimatum_team.multimatum

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.nfc.NfcAdapter.EXTRA_ID
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.github.multimatum_team.multimatum.repository.DeadlineID
import com.github.multimatum_team.multimatum.service.QRCodeGenerator
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
/**
    Activity that allow the user to generate a QR Code.
    The user write in the TextField what he want encoded in QR Code and press the button Generate
    to make the encoded data appear in a ImageView.
    If no data is in the TextField, a Toast will appear saying to enter some data.
 */
class QRGeneratorActivity : AppCompatActivity() {

    lateinit var id: DeadlineID
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrgenerator)
        id = intent.getStringExtra(EXTRA_ID).toString()
        displayQRCode()
    }

    //function to return to the main activity
    fun returnMain(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }


    fun displayQRCode() {
        val data = id

        //Make a notification if there is no text
        if (data.isEmpty()) {
            Toast.makeText(this, "Enter some data", Toast.LENGTH_SHORT).show()
        } else {
            //create a QRCodeGenerator
            val qrCodeGenerator = QRCodeGenerator(data)
            //set the image
            findViewById<ImageView>(R.id.QRGenerated).setImageBitmap(qrCodeGenerator.generateQRCode())
        }
    }
}
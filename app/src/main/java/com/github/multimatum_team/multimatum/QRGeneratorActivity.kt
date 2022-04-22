package com.github.multimatum_team.multimatum

import android.content.Intent
import android.nfc.NfcAdapter.EXTRA_ID
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.github.multimatum_team.multimatum.repository.DeadlineID
import com.github.multimatum_team.multimatum.service.QRCodeGenerator

/**
Activity that generate a QR Code from the id of the parent deadline.
It receive the id of the deadline with the intent to generate the QRCode.
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

    /**
     * display the QRCode generated by the QRGenerator object
     */
    private fun displayQRCode() {
        val data = id
        //create a QRCodeGenerator
        val qrCodeGenerator = QRCodeGenerator(data)
        //set the image
        findViewById<ImageView>(R.id.QRGenerated).setImageBitmap(qrCodeGenerator.generateQRCode())
    }
}
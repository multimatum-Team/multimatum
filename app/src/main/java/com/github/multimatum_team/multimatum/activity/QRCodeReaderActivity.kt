package com.github.multimatum_team.multimatum.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.github.multimatum_team.multimatum.CodeScannerProducer
import com.github.multimatum_team.multimatum.util.JsonDeadlineConverter
import com.github.multimatum_team.multimatum.R
import com.github.multimatum_team.multimatum.viewmodel.DeadlineListViewModel
import com.google.gson.JsonSyntaxException
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * The purpose of this activity is to provide the user an interface to scan QR-Codes.
 * The main component of this activity is the scanner that as the capability to read
 * and return the content of a QR-Code as a string.
 */

@AndroidEntryPoint
class QRCodeReaderActivity : AppCompatActivity() {

    @Inject
    lateinit var codeScannerProducer: CodeScannerProducer

    @Inject
    lateinit var jsonConverter: JsonDeadlineConverter

    private lateinit var codeScanner: CodeScanner
    private val deadlineListViewModel: DeadlineListViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        // Launching the QR Code reader with default parameters
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode_reader)
        val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)

        codeScanner = codeScannerProducer.produce(this, scannerView)

        // Parameters (default values)
        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

        // Callback for successful scanning
        codeScanner.decodeCallback = DecodeCallback {
            scanDeadline(it.text)
        }

        // Callback for the initialization error of the camera
        codeScanner.errorCallback = ErrorCallback {
            Toast.makeText(this, "Camera initialization error: ${it.message}", Toast.LENGTH_LONG).show()
        }

        // Start the scanner
        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    // Useful function to display a given string using the Toast interface
    private fun scanDeadline(scan: String) {
        runOnUiThread {
            try {
                val deadline = jsonConverter.fromJson(scan)
                deadlineListViewModel.addDeadline(deadline)
                Toast.makeText(this, "Deadline successfully added", Toast.LENGTH_LONG).show()
            } catch (e: JsonSyntaxException) {
                Toast.makeText(this, "provide a valid QRCode please", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }
}
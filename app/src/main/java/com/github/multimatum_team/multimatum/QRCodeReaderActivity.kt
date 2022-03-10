package com.github.multimatum_team.multimatum

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode

class QRCodeReaderActivity : AppCompatActivity() {
    private lateinit var codeScanner: CodeScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        // Launching the QR Code reader with default parameters
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode_reader)
        val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)

        codeScanner = CodeScanner(this, scannerView)

        // Parameters (default values)
        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

        // Callback for successful scanning
        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                generateCallbackTest("Scan result: ${it.text}")
            }
        }

        // Callback for the initialization error of the camera
        codeScanner.errorCallback = ErrorCallback {
            runOnUiThread {
                generateCallbackTest("Camera initialization error: ${it.message}")
            }
        }

        // Start the scanner
        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    fun generateCallbackTest(text: String){
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }
}
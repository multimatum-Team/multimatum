package com.github.multimatum_team.multimatum.service

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter

/**
 * class that create an object to generate QRCode
 * goal of modularisation
 */
class QRCodeGenerator(data: String) {

    private val writer = QRCodeWriter()
    private val bitmap: BitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512)

    /**
     * generate a QRCode from data saved by constructor
     */
    fun generateQRCode(): Bitmap {
        val bmp = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.RGB_565)
        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                bmp.setPixel(x, y, if (bitmap[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    }
}
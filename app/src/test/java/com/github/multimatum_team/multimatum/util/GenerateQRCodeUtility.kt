package com.github.multimatum_team.multimatum.util

import android.graphics.Bitmap
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader

class GenerateQRCodeUtility {
    companion object {
        fun extractContent(bitmap: Bitmap): String? {
            val binary = convertToBinary(bitmap)
            return try {
                // Parse the bitmap and check it against expected value
                QRCodeReader().decode(binary).text
            } catch (ex: Exception) {
                when (ex) {
                    is NotFoundException, is ChecksumException, is FormatException -> {
                        throw IllegalArgumentException(
                            "The provided image is not a valid QRCode",
                            ex
                        )
                    }
                    else -> throw ex
                }
            }
        }

        fun convertToBinary(qrCode: Bitmap): BinaryBitmap {
            // Convert the QRCode to something zxing understands
            val buffer = IntArray(qrCode.width * qrCode.height)
            qrCode.getPixels(buffer, 0, qrCode.width, 0, 0, qrCode.width, qrCode.height)
            val source: LuminanceSource =
                RGBLuminanceSource(qrCode.width, qrCode.height, buffer)
            return BinaryBitmap(HybridBinarizer(source))
        }
    }
}
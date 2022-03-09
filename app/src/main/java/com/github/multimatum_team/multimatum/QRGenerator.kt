package com.github.multimatum_team.multimatum

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

class QRGenerator : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrgenerator)
    }

    //function to return to the main activity
    fun returnMain(view: View){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }


    fun displayQRCode(view: View){
        val qrNameField = findViewById<EditText>(R.id.QRTextEdit)
        val data = qrNameField.text.toString().trim()

        //Make a notification if there is no text
        if(data.isEmpty()){
            Toast.makeText(this, "Enter some data", Toast.LENGTH_SHORT).show()
        } else {

            //encode the data to the format QRCode
            val writer = QRCodeWriter()
            val bitmap = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitmap.width
            val height = bitmap.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width){
                for(y in 0 until height){
                    bmp.setPixel(x,y, if (bitmap[x,y]) Color.BLACK else Color.WHITE)
                }
            }

            //set the image
            findViewById<ImageView>(R.id.QRGenerated).setImageBitmap(bmp)
        }

    }
}
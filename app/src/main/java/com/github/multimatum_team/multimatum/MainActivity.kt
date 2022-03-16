package com.github.multimatum_team.multimatum

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

const val EXTRA_NAME = "com.github.multimatum_team.multimatum.main.name"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun goQRGenerator(view:View){
        val intent = Intent(this, QRGenerator::class.java)
        startActivity(intent)
    }

    fun displayGreeting(view: View) {
        val mainNameField = findViewById<EditText>(R.id.mainName)
        val intent = Intent(this, GreetingActivity::class.java).apply {
            putExtra(EXTRA_NAME, mainNameField.text.toString())
        }
        startActivity(intent)
    }

    fun goToLoginScreen(view: View){
        val intent = Intent(this, AccountActivity::class.java)
        startActivity(intent)
    }


    fun launchSettingsActivity(view: View) {
        val intent = Intent(this, MainSettingsActivity::class.java)
        startActivity(intent)
    }

}
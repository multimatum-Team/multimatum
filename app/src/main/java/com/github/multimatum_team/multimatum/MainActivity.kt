package com.github.multimatum_team.multimatum

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText

const val EXTRA_NAME = "com.github.multimatum_team.multimatum.main.name"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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


}
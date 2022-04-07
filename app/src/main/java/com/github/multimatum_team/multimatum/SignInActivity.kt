package com.github.multimatum_team.multimatum

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInActivity : AppCompatActivity() {
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        onSignInResult(res)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)
        supportActionBar?.hide()

        val signInButton = findViewById<Button>(R.id.sign_in_button)

        signInButton.setOnClickListener {
            launchSignInIntent(it)
        }
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser!!
            Log.d(TAG, "onSignInResult:success")
            Toast.makeText(
                baseContext, "Successfully authenticated as\n${user.email}.",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        } else {
            Log.w(TAG, "onSignInResult:failure: ${response?.error?.errorCode}")
            Toast.makeText(
                baseContext, "Authentication failed.",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }

    private fun launchSignInIntent(view: View) {
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create and launch sign-in intent
        val intent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()

        signInLauncher.launch(intent)
    }

    companion object {
        private const val TAG = "AccountActivity"
    }
}

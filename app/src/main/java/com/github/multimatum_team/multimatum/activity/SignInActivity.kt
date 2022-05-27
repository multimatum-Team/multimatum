package com.github.multimatum_team.multimatum.activity

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.github.multimatum_team.multimatum.LogUtil
import com.github.multimatum_team.multimatum.R
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

/**
 * This activity allows an anonymous user to sign-in using a Google account and link the current
 * anonymous user to the new account.
 */
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
            LogUtil.debugLog("onSignInResult:success")
            Toast.makeText(
                baseContext, "Successfully authenticated as\n${user.email}.",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        } else {
            // Sign-in failed
            LogUtil.debugLog("onSignInResult:failure: ${response?.error?.errorCode}")
            Toast.makeText(
                baseContext, "Authentication failed.",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }

    private fun launchSignInIntent(view: View) {
        // For now we only support Google accounts
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
}

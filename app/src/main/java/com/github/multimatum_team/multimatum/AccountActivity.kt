package com.github.multimatum_team.multimatum

import android.annotation.SuppressLint
import android.content.Intent
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.inappmessaging.internal.Logging.TAG
import java.lang.Exception


const val RC_SIGN_IN = 123

class AccountActivity : AppCompatActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        //Important for testing
        FirebaseApp.initializeApp(this)


        //Configure sign-in to request user's credential
        val gso: GoogleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()
        //configure client
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        val account = GoogleSignIn.getLastSignedInAccount(this)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()


        // configure button
        findViewById<SignInButton>(R.id.sign_in_button).visibility = VISIBLE
        findViewById<SignInButton>(R.id.sign_in_button).setOnClickListener {
            Log.d(TAG, "onCreate begin Google SignIn:")
            signIn()
        }
    }

    override fun onResume() {
        super.onResume()
        checkUser()
    }

    // check if user is loged or not
    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            launchFrag()
        }
    }

    private fun launchFrag() {
        findViewById<SignInButton>(R.id.sign_in_button).visibility = GONE
        val fragment =
            supportFragmentManager.findFragmentById(R.id.profile_frag) ?: ProfileFragment()
        supportFragmentManager.beginTransaction().replace(R.id.accout, fragment).commit()
    }

    private fun signIn() {
        val signInIntent: Intent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    @SuppressLint("VisibleForTests")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogleAccount(account)
            } catch (e: Exception) {
                Log.d(TAG, "onActivityResult: ${e.message}")
            }
        } else {
            Toast.makeText(
                this@AccountActivity,
                "Login fail due too wrong request Code",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun firebaseAuthWithGoogleAccount(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnSuccessListener { authResult ->
            //log in success
            //get loggedIn user
            val firebasUser = firebaseAuth.currentUser
            //get user info
            val email = firebasUser!!.email
            if (authResult.additionalUserInfo!!.isNewUser) {
                //new account
                Toast.makeText(
                    this@AccountActivity,
                    "Account created...\n$email",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                //existing user
                Toast.makeText(this@AccountActivity, "LoggedIn...\n$email", Toast.LENGTH_SHORT)
                    .show()
            }
            launchFrag()

        }.addOnFailureListener { e ->
            //login failed
            Toast.makeText(
                this@AccountActivity,
                "Loggin Failed due to ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            // Signed in successfully, show authenticated UI.
            findViewById<SignInButton>(R.id.sign_in_button).visibility = GONE

        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            findViewById<SignInButton>(R.id.sign_in_button).visibility = VISIBLE
        }
    }
}

package com.github.multimatum_team.multimatum

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
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
import com.google.api.LogDescriptor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.inappmessaging.internal.Logging.TAG
import org.w3c.dom.Text
import java.lang.Exception
import kotlin.math.log


const val RC_SIGN_IN = 123

class AccountActivity: AppCompatActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.activity_account)

        //Configure sign-in to request user's credential
        val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()
        //configure client
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        val account = GoogleSignIn.getLastSignedInAccount(this)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        //TODO: checkUser() maybe usefull later

        //configure button
        findViewById<Button>(R.id.sign_in_button).setOnClickListener{
            Log.d(TAG, "onCreate begin Google SignIn:"+account.id)
            signIn()
        }

    }

    private fun checkUser(){
        //check if user is logged in or not
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null){
            //user is already logged in
        }
    }

    private fun signIn(){
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
            Log.d(TAG, "onActivityResult: Google SignIn intent result")

            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
            try{
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:"+account.id)
                firebaseAuthWithGoogleAccount(account)
            }catch (e: Exception){
                Log.d(TAG, "onActivityResult: ${e.message}")
            }
        }
    }

    private fun firebaseAuthWithGoogleAccount(account: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogleAccount: begin firebase auth with google account")
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnSuccessListener { authResult ->
            //log in success
            Log.d(TAG, "firebaseAuthWithGoogleAccount: LoggedIn")

            //get loggedIn user
            val firebasUser = firebaseAuth.currentUser
            //get user info
            val uid = firebasUser!!.uid
            val email = firebasUser.email

            Log.d(TAG, "firebaseAuthWithGoogleAccount: Uid: $uid")
            Log.d(TAG, "firebaseAuthWithGoogleAccount: Email: $email")

            if (authResult.additionalUserInfo!!.isNewUser){
                //new account
                Log.d(TAG, "firebaseAuthWithGoogleAccount: Account created...\n")
                Toast.makeText(this@AccountActivity, "Account created...\n$email", Toast.LENGTH_SHORT).show()
            }else{
                //existing user
                Log.d(TAG, "firebaseAuthWithGoogleAccount: Existing user...\n$email")
                Toast.makeText(this@AccountActivity, "LoggedIn...\n$email", Toast.LENGTH_SHORT).show()
            }
            //TODO:start profile activity

        }.addOnFailureListener{ e-> 
            //login failed
            Log.d(TAG, "firebaseAuthWithGoogleAccount: Loggin Failed du to ${e.message}")
            Toast.makeText(this@AccountActivity, "Loggin Failed due to ${e.message}", Toast.LENGTH_SHORT).show()

        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            // Signed in successfully, show authenticated UI.
            findViewById<SignInButton>(R.id.sign_in_button).visibility = View.GONE
            findViewById<TextView>(R.id.sign_in_text).visibility = View.GONE

        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            findViewById<SignInButton>(R.id.sign_in_button).visibility = View.VISIBLE
            findViewById<TextView>(R.id.sign_in_text).visibility = View.VISIBLE

        }
    }
}

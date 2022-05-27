package com.github.multimatum_team.multimatum.repository

import android.util.Log
import android.widget.Toast
import com.github.multimatum_team.multimatum.model.AnonymousUser
import com.github.multimatum_team.multimatum.model.SignedInUser
import com.github.multimatum_team.multimatum.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.mapbox.bindgen.None
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import javax.inject.Inject

/**
 * Concrete implementation for the AuthRepository.
 * Acts as a wrapper around the Firebase authentication library.
 */
class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository() {

    /**
     * We guarantee that an user is always logged in at any point of the application.
     * At the first execution of the application, an anonymous user is created, and logged into on
     * subsequent executions, until the user chooses to link the anonymous account to an actual
     * Google account (see SignInActivity).
     */
    init {
        when (val firebaseUser = auth.currentUser) {
            null -> {
                // When the user was never logged-in (when the app is started the first time), we create
                // an anonymous firebase account
                Log.d(TAG, "First time log-in, waiting for anonymous sign-in...")
                runBlocking { anonymousSignIn() }
                Log.d(TAG, "Successfully signed-in anonymously with user ID ${_user.id}")
            }
            else -> {
                // Otherwise, we check whether the user has signed in using a real account or if they
                // were signed in as an anonymous user
                _user = convertFirebaseUser(firebaseUser)
            }
        }
    }

    /**
     * Convert a Firebase user into our User model.
     */
    private fun convertFirebaseUser(firebaseUser: FirebaseUser): User =
        if (firebaseUser.isAnonymous) {
            AnonymousUser(firebaseUser.uid)
        } else {
            SignedInUser(firebaseUser.uid, firebaseUser.displayName!!, firebaseUser.email!!)
        }

    /**
     * Create an anonymous user so that the user can try the application before having to link it
     * to their Google account.
     */
    private suspend fun anonymousSignIn(): AnonymousUser {
        when (val authResultUser = auth.signInAnonymously().await()?.user) {
            null -> throw RuntimeException("Failed to sign-in anonymously")
            else -> {
                val newUser = AnonymousUser(authResultUser.uid)
                _user = newUser
                return newUser
            }
        }
    }

    /**
     * Sign-out from the current account.
     * To ensure that there is still an user to which we can associate the deadlines, an anonymous
     * user is created.
     */
    override suspend fun signOut(): AnonymousUser {
        auth.signOut()
        return anonymousSignIn()
    }

    /**
     * Notify all observers that the user changed.
     */
    override fun onUpdate(callback: (User) -> Unit) {
        auth.addAuthStateListener { auth ->
            auth.currentUser?.let { newUser ->
                _user = convertFirebaseUser(newUser)
                callback(_user)
            }
        }
    }

    companion object {
        private const val TAG = "FirebaseAuthRepository"
    }
}

package com.github.multimatum_team.multimatum.repository

import android.util.Log
import com.github.multimatum_team.multimatum.model.AnonymousUser
import com.github.multimatum_team.multimatum.model.SignedInUser
import com.github.multimatum_team.multimatum.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

/**
 * Concrete implementation for the AuthRepository.
 * Acts as a wrapper around the Firebase authentication library.
 */
class FirebaseAuthRepository : AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Convert a Firebase user into our User model.
     */
    private fun convertFirebaseUser(firebaseUser: FirebaseUser): User =
        if (firebaseUser.email == null) {
            AnonymousUser(firebaseUser.uid)
        } else {
            SignedInUser(firebaseUser.uid, firebaseUser.email!!)
        }

    /**
     * Create an anonymous user so that the user can try the application before having to link it
     * to their Google account.
     */
    private suspend fun anonymousSignIn(): AnonymousUser {
        val authResult = auth.signInAnonymously().await()
        if (authResult.user == null) {
            throw RuntimeException("Failed to sign-in anonymously")
        } else {
            return AnonymousUser(authResult.user!!.uid)
        }
    }

    /**
     * Get the currently logged-in user.
     * We guarantee that an user is always logged in at any point of the application.
     * At the first execution of the application, an anonymous user is created, and logged into on
     * subsequent executions, until the user chooses to link the anonymous account to an actual
     * Google account (see SignInActivity).
     */
    override suspend fun getCurrentUser(): User {
        lateinit var user: User
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            // When the user was never logged-in (when the app is started the first time), we create
            // an anonymous firebase account
            Log.d(TAG, "First time log-in, waiting for anonymous sign-in...")
            user = anonymousSignIn()
            Log.d(TAG, "Successfully signed-in anonymously with user ID ${user.id}")
        } else {
            // Otherwise, we check whether the user has signed in using a real account or if they
            // were signed in as an anonymous user
            user = convertFirebaseUser(firebaseUser)
        }
        Log.d(TAG, "current user: $user")
        return user
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
        // FIXME: this listener is executed more than necessary, other updates can trigger the
        //  callback even if the user hasn't changed (I think?).
        auth.addAuthStateListener { auth ->
            auth.currentUser?.let { newUser ->
                callback(convertFirebaseUser(newUser))
            }
        }
    }

    companion object {
        private const val TAG = "FirebaseAuthRepository"
    }
}
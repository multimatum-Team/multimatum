package com.github.multimatum_team.multimatum.repository

import android.util.Log
import com.github.multimatum_team.multimatum.model.AnonymousUser
import com.github.multimatum_team.multimatum.model.SignedInUser
import com.github.multimatum_team.multimatum.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository : AuthRepository {
    companion object {
        private const val TAG = "FirebaseAuthRepository"
    }

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private fun convertFirebaseUser(firebaseUser: FirebaseUser): User =
        if (firebaseUser.email == null) {
            AnonymousUser(firebaseUser.uid)
        } else {
            SignedInUser(firebaseUser.uid, firebaseUser.email!!)
        }

    private suspend fun anonymousSignIn(): AnonymousUser {
        val authResult = auth.signInAnonymously().await()
        if (authResult.user == null) {
            throw RuntimeException("Failed to sign-in anonymously")
        } else {
            return AnonymousUser(authResult.user!!.uid)
        }
    }

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

    override suspend fun signOut(): AnonymousUser {
        auth.signOut()
        return anonymousSignIn()
    }

    override fun onUpdate(callback: (User) -> Unit) {
        auth.addAuthStateListener { auth ->
            auth.currentUser?.let { newUser ->
                callback(convertFirebaseUser(newUser))
            }
        }
    }
}
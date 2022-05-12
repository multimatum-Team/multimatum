package com.github.multimatum_team.multimatum.util

import com.google.firebase.auth.FirebaseAuth
import org.mockito.Mockito

class MockFirebaseAuth {
    val auth: FirebaseAuth = Mockito.mock(FirebaseAuth::class.java)
}
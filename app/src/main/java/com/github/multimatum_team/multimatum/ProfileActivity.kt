package com.github.multimatum_team.multimatum

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.github.multimatum_team.multimatum.model.AnonymousUser
import com.github.multimatum_team.multimatum.model.SignedInUser
import com.github.multimatum_team.multimatum.model.User
import com.github.multimatum_team.multimatum.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.lang.IllegalStateException

/**
 * An activity to display the account that the user is currently logged in.
 * The view shows the e-mail of the current account, and provides a button to log out from the
 * current account.
 */
@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {
    private val userViewModel: UserViewModel by viewModels()

    private lateinit var loginMessage: TextView
    private lateinit var logOutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        loginMessage = findViewById(R.id.login_message)
        logOutButton = findViewById(R.id.log_out_button)

        logOutButton.setOnClickListener {
            Log.d(TAG, "Logging out...")
            userViewModel.signOut()
            AuthUI.getInstance().signOut(this)
            finish()
        }

        // Update the e-mail when the account changes.
        userViewModel.getUser().observe(this) { user ->
            if (user is SignedInUser) {
                updateUI(user)
            } else {
                throw IllegalStateException("ProfileActivity launched while the current user is not signed in")
            }
        }
    }

    /**
     * Update the e-mail with the new user.
     */
    private fun updateUI(user: SignedInUser) {
        loginMessage.text =
            getString(R.string.you_re_logged_in_as, user.email)
    }

    companion object {
        private const val TAG = "ProfileActivity"
    }
}
package com.github.multimatum_team.multimatum

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.multimatum_team.multimatum.model.SignedInUser
import com.github.multimatum_team.multimatum.model.User
import com.github.multimatum_team.multimatum.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileActivity.newInstance] factory method to
 * create an instance of this fragment.
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
            Log.d(TAG, "logging out...")
            userViewModel.signOut()
            finish()
        }

        userViewModel.getUser().observe(this) { user ->
            updateUI(user)
        }
    }

    private fun updateUI(user: User) {
        loginMessage.text =
            getString(R.string.you_re_logged_in_as, (user as SignedInUser).email)
    }

    companion object {
        private const val TAG = "ProfileFragment"
    }
}
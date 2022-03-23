package com.github.multimatum_team.multimatum

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.context?.let { FirebaseApp.initializeApp(it) }
        firebaseAuth = FirebaseAuth.getInstance()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        view.findViewById<Button>(R.id.log_out_button).setOnClickListener {
            firebaseAuth.signOut()
            checkUser(view)
        }
        updateUI(firebaseAuth.currentUser, view)
        return view
    }

    private fun checkUser(view: View) {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            //user not logged in
            startActivity(Intent(this.context, AccountActivity::class.java))
        } else {
            //user logged in
            //get info
            updateUI(firebaseUser, view)
        }
    }

    private fun updateUI(user: FirebaseUser?, view: View) {
        val email = user?.email
        view.findViewById<TextView>(R.id.Email).text = email
    }
}
package com.example.freelancerconnect.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import com.example.freelancerconnect.R
import com.example.freelancerconnect.viewmodels.AuthViewModel

class ForgotPasswordActivity : BaseActivity() {

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot)

        val emailField  = findViewById<EditText>(R.id.email)
        val resetButton = findViewById<Button>(R.id.resetButton)
        val backButton  = findViewById<TextView>(R.id.backButton)

        backButton.setOnClickListener { finish() }

        resetButton.setOnClickListener {
            val email = emailField.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            resetButton.isEnabled = false
            resetButton.text = "Sending…"

            viewModel.resetPassword(email) { success, error ->
                resetButton.isEnabled = true
                resetButton.text = "Send reset link  →"
                val message = if (success) {
                    "If an account with that email exists, a reset link has been sent."
                } else {
                    error ?: "Failed to send reset email"
                }
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                if (success) finish()
            }
        }
    }
}
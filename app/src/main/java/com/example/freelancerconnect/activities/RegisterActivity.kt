package com.example.freelancerconnect.activities

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import com.example.freelancerconnect.R
import com.example.freelancerconnect.viewmodels.AuthViewModel

class RegisterActivity : BaseActivity() {

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val nameField       = findViewById<EditText>(R.id.name)
        val emailField      = findViewById<EditText>(R.id.email)
        val passwordField   = findViewById<EditText>(R.id.password)
        val registerButton  = findViewById<Button>(R.id.registerButton)
        val loginLink       = findViewById<TextView>(R.id.loginLink)

        // Navigate back to login
        loginLink.setOnClickListener {
            finish()
        }

        registerButton.setOnClickListener {
            val name     = nameField.text.toString().trim()
            val email    = emailField.text.toString().trim()
            val password = passwordField.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and password are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerButton.isEnabled = false
            registerButton.text = "Creating account…"

            viewModel.register(email, password, name) { success, error, response ->
                registerButton.isEnabled = true
                registerButton.text = "Create account  →"
                if (success && response != null) {
                    // Save JWT token after registration
                    session.saveSession(response)
                    navigateTo(RoleSelectionActivity::class.java, finishCurrent = true)
                } else {
                    Toast.makeText(this, error ?: "Registration failed. Please try again.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
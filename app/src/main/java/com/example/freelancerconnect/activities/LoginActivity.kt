package com.example.freelancerconnect.activities

import android.os.Bundle
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.example.freelancerconnect.BuildConfig
import com.example.freelancerconnect.R
import com.example.freelancerconnect.notifications.PushTokenManager
import com.example.freelancerconnect.viewmodels.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class LoginActivity : BaseActivity() {

    private val viewModel: AuthViewModel by viewModels()
    private var requestedGoogleRole = "Freelancer"
    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                .getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken.isNullOrBlank()) {
                Toast.makeText(this, "Google did not return an ID token.", Toast.LENGTH_LONG).show()
                return@registerForActivityResult
            }
            viewModel.googleSignIn(idToken, requestedGoogleRole) { success, error, response ->
                if (success && response != null) {
                    session.saveSession(response)
                    PushTokenManager.register(this)
                    navigateAndClearStack(DashboardActivity::class.java)
                } else {
                    Toast.makeText(this, error ?: "Google sign-in failed.", Toast.LENGTH_LONG).show()
                }
            }
        } catch (_: ApiException) {
            Toast.makeText(this, "Google sign-in was cancelled or unavailable.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailField    = findViewById<EditText>(R.id.email)
        val passwordField = findViewById<EditText>(R.id.password)
        val loginButton   = findViewById<Button>(R.id.loginButton)

        loginButton.setOnClickListener {
            val email    = emailField.text.toString().trim()
            val password = passwordField.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter your email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginButton.isEnabled = false
            loginButton.text = "Signing in…"

            viewModel.login(email, password) { success, error, response ->
                loginButton.isEnabled = true
                loginButton.text = "Sign in  →"
                if (success && response != null) {
                    // Save JWT token and user info to SharedPreferences
                    session.saveSession(response)
                    navigateAndClearStack(DashboardActivity::class.java)
                } else {
                    Toast.makeText(this, error ?: "Login failed. Check your credentials.", Toast.LENGTH_LONG).show()
                }
            }
        }

        findViewById<TextView>(R.id.registerLink).setOnClickListener {
            navigateTo(RegisterActivity::class.java)
        }

        findViewById<TextView>(R.id.forgotLink).setOnClickListener {
            navigateTo(ForgotPasswordActivity::class.java)
        }

        findViewById<Button>(R.id.googleSignInButton).setOnClickListener {
            if (BuildConfig.GOOGLE_WEB_CLIENT_ID.isBlank()) {
                Toast.makeText(this, "Google sign-in needs the web client ID configured in Gradle.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            AlertDialog.Builder(this)
                .setTitle("Continue as")
                .setItems(arrayOf("Freelancer", "Client")) { _, choice ->
                    requestedGoogleRole = if (choice == 1) "Client" else "Freelancer"
                    val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                        .build()
                    googleSignInLauncher.launch(GoogleSignIn.getClient(this, options).signInIntent)
                }
                .show()
        }
    }
}
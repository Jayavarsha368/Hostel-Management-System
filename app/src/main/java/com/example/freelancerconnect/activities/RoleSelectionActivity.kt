package com.example.freelancerconnect.activities

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import com.example.freelancerconnect.R
import com.example.freelancerconnect.api.UpdateUserRequest
import com.example.freelancerconnect.viewmodels.ProfileViewModel

class RoleSelectionActivity : BaseActivity() {

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role)

        val freelancerButton = findViewById<Button>(R.id.freelancerButton)
        val clientButton     = findViewById<Button>(R.id.clientButton)

        // Tapping the card also triggers selection
        findViewById<LinearLayout>(R.id.freelancerCard).setOnClickListener {
            saveRole("Freelancer")
        }

        findViewById<LinearLayout>(R.id.clientCard).setOnClickListener {
            saveRole("Client")
        }

        freelancerButton.setOnClickListener {
            saveRole("Freelancer")
        }

        clientButton.setOnClickListener {
            saveRole("Client")
        }
    }

    private fun saveRole(role: String) {
        val userId = session.userId
        val token  = session.bearerToken

        if (userId.isEmpty()) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show()
            navigateAndClearStack(LoginActivity::class.java)
            return
        }

        viewModel.saveProfile(
            token   = token,
            userId  = userId,
            request = UpdateUserRequest(role = role)
        ) { success, error ->
            if (success) {
                session.updateUser(role = role)
                // After role, go to profile setup
                navigateTo(ProfileActivity::class.java, finishCurrent = true)
            } else {
                Toast.makeText(this, error ?: "Failed to save role. Please try again.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
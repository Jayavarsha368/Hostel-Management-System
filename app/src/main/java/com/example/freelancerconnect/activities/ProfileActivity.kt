package com.example.freelancerconnect.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.freelancerconnect.R
import com.example.freelancerconnect.api.ApiClient
import com.example.freelancerconnect.api.UpdateUserRequest
import com.example.freelancerconnect.viewmodels.ProfileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

class ProfileActivity : BaseActivity() {

    private val viewModel: ProfileViewModel by viewModels()

    private val resumePicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { attachSelectedFile(it, isResume = true, session.bearerToken) }
    }

    private val portfolioPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { attachSelectedFile(it, isResume = false, session.bearerToken) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val nameField        = findViewById<EditText>(R.id.name)
        val contactField     = findViewById<EditText>(R.id.contactInformation)
        val phoneNumberField = findViewById<EditText>(R.id.phoneNumber)
        val skillsField      = findViewById<EditText>(R.id.skills)
        val experienceField  = findViewById<EditText>(R.id.experience)
        val educationField   = findViewById<EditText>(R.id.education)
        val bioField         = findViewById<EditText>(R.id.bio)
        val githubField      = findViewById<EditText>(R.id.githubUrl)
        val linkedinField    = findViewById<EditText>(R.id.linkedinUrl)
        val resumeButton     = findViewById<Button>(R.id.uploadResume)
        val portfolioButton  = findViewById<Button>(R.id.uploadPortfolio)
        val resumeFileName   = findViewById<TextView>(R.id.resumeFileName)
        val portfolioFileName = findViewById<TextView>(R.id.portfolioFileName)
        val saveButton       = findViewById<Button>(R.id.saveProfile)
        val backButton       = findViewById<TextView>(R.id.backButton)
        val skipButton       = findViewById<TextView>(R.id.skipButton)

        val userId = session.userId
        val token  = session.bearerToken

        if (userId.isEmpty()) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show()
            navigateAndClearStack(LoginActivity::class.java)
            return
        }

        if (session.userName.isNotEmpty()) {
            nameField.setText(session.userName)
        }

        viewModel.profile.observe(this) { profile ->
            if (contactField.text.isNullOrBlank()) {
                contactField.setText(profile?.contactInformation.orEmpty())
            }
            if (phoneNumberField.text.isNullOrBlank()) {
                phoneNumberField.setText(profile?.phoneNumber.orEmpty())
            }
            if (profile?.resumeUrl?.isNotBlank() == true && resumeFileName.text.isNullOrBlank()) {
                resumeFileName.text = "Resume available · choose to replace"
            }
            if (profile?.portfolioUrl?.isNotBlank() == true && portfolioFileName.text.isNullOrBlank()) {
                portfolioFileName.text = "Portfolio available · choose to replace"
            }
        }
        viewModel.fetchProfile(token)

        resumeButton.setOnClickListener {
            resumePicker.launch(arrayOf(
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            ))
        }

        portfolioButton.setOnClickListener {
            portfolioPicker.launch(arrayOf(
                "application/pdf",
                "image/*",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            ))
        }

        backButton.setOnClickListener { finish() }

        skipButton.setOnClickListener {
            navigateTo(DashboardActivity::class.java, finishCurrent = true)
        }

        saveButton.setOnClickListener {
            val skills = skillsField.text.toString()
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            val request = UpdateUserRequest(
                name        = nameField.text.toString().trim(),
                contactInformation = contactField.text.toString().trim(),
                phoneNumber = phoneNumberField.text.toString().trim(),
                skills      = skills,
                experience  = experienceField.text.toString().trim(),
                education   = educationField.text.toString().trim(),
                bio         = bioField.text.toString().trim(),
                githubUrl   = githubField.text.toString().trim().ifBlank { null },
                linkedinUrl = linkedinField.text.toString().trim().ifBlank { null }
            )

            saveButton.isEnabled = false
            saveButton.text = "Saving…"

            viewModel.saveProfile(token, userId, request) { success, error ->
                saveButton.isEnabled = true
                saveButton.text = "Save profile  →"
                if (success) {
                    session.updateUser(
                        name = nameField.text.toString().trim(),
                        skills = skills
                    )
                    Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show()
                    navigateTo(DashboardActivity::class.java, finishCurrent = true)
                } else {
                    Toast.makeText(this, error ?: "Failed to save profile", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun attachSelectedFile(uri: Uri, isResume: Boolean, token: String) {
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
            // Ignore for non-persisted URI access.
        }

        val fileName = getDisplayName(uri)
        val extension = fileName.substringAfterLast('.', "").let { ".$it" }.lowercase()
        val kind = if (isResume) "resume" else "portfolio"
        val fileLabel = findViewById<TextView>(if (isResume) R.id.resumeFileName else R.id.portfolioFileName)
        val uploadButton = findViewById<Button>(if (isResume) R.id.uploadResume else R.id.uploadPortfolio)
        fileLabel.text = "Uploading $fileName..."
        uploadButton.isEnabled = false

        lifecycleScope.launch {
            try {
                val fileBytes = withContext(Dispatchers.IO) {
                    val output = ByteArrayOutputStream()
                    val buffer = ByteArray(8192)
                    var totalBytes = 0
                    val input = contentResolver.openInputStream(uri)
                        ?: throw IllegalStateException("The selected file could not be opened.")
                    input.use {
                        while (true) {
                            val count = it.read(buffer)
                            if (count < 0) break
                            totalBytes += count
                            if (totalBytes > 10 * 1024 * 1024) {
                                throw IllegalArgumentException("Files must be 10 MB or smaller.")
                            }
                            output.write(buffer, 0, count)
                        }
                    }
                    output.toByteArray()
                }
                val response = ApiClient.api.uploadProfileFile(
                    token,
                    kind,
                    extension,
                    fileBytes.toRequestBody("application/octet-stream".toMediaType())
                )
                if (!response.isSuccessful) {
                    throw IllegalStateException(response.errorBody()?.string() ?: "Upload failed (${response.code()}).")
                }
                fileLabel.text = fileName
                Toast.makeText(this@ProfileActivity, "$kind uploaded.", Toast.LENGTH_SHORT).show()
            } catch (error: Exception) {
                fileLabel.text = "Upload failed. Choose a file to retry."
                Toast.makeText(
                    this@ProfileActivity,
                    error.message ?: "The file could not be uploaded.",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                uploadButton.isEnabled = true
            }
        }
    }

    private fun getDisplayName(uri: Uri): String {
        return try {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val nameIndex = it.getColumnIndexOrThrow("_display_name")
                if (it.moveToFirst()) {
                    it.getString(nameIndex)
                } else {
                    uri.lastPathSegment ?: "Selected file"
                }
            } ?: uri.lastPathSegment ?: "Selected file"
        } catch (_: Exception) {
            uri.lastPathSegment ?: "Selected file"
        }
    }
}
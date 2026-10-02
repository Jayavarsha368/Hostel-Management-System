package com.example.freelancerconnect.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import com.example.freelancerconnect.R
import com.example.freelancerconnect.api.CreateJobRequest
import com.example.freelancerconnect.viewmodels.JobViewModel

/**
 * PostJobActivity — allows clients to publish a new job posting.
 * Sends job data to the MongoDB API backend via JobViewModel.
 */
class PostJobActivity : BaseActivity() {

    private val viewModel: JobViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_job)

        val titleField       = findViewById<EditText>(R.id.jobTitle)
        val descriptionField = findViewById<EditText>(R.id.jobDescription)
        val budgetField      = findViewById<EditText>(R.id.budget)
        val skillsField      = findViewById<EditText>(R.id.skills)
        val durationField    = findViewById<EditText>(R.id.duration)
        val deadlineField    = findViewById<EditText>(R.id.deadline)
        val saveButton       = findViewById<Button>(R.id.saveJob)

        // Back navigation
        findViewById<android.widget.TextView>(R.id.backButton).setOnClickListener { finish() }
        findViewById<Button>(R.id.manageJobPosts).setOnClickListener {
            navigateTo(JobListActivity::class.java)
        }

        saveButton.setOnClickListener {
            val title       = titleField.text.toString().trim()
            val description = descriptionField.text.toString().trim()

            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Title and description are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val skills = skillsField.text.toString()
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            val request = CreateJobRequest(
                title          = title,
                description    = description,
                budget         = budgetField.text.toString().trim(),
                skillsRequired = skills,
                duration       = durationField.text.toString().trim(),
                deadline       = deadlineField.text.toString().trim()
            )

            saveButton.isEnabled = false
            saveButton.text = "Publishing…"

            viewModel.createJob(session.bearerToken, request) { success, error ->
                saveButton.isEnabled = true
                saveButton.text = "Publish job  →"
                if (success) {
                    Toast.makeText(this, "Job published successfully!", Toast.LENGTH_SHORT).show()
                    navigateTo(JobListActivity::class.java, finishCurrent = true)
                } else {
                    Toast.makeText(this, error ?: "Failed to post job", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
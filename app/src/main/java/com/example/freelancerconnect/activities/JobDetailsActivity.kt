package com.example.freelancerconnect.activities

import android.os.Bundle
import android.content.Intent
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.activity.viewModels
import com.example.freelancerconnect.R
import com.example.freelancerconnect.viewmodels.JobViewModel

class JobDetailsActivity : BaseActivity() {

    private val viewModel: JobViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_details)

        val jobId = intent.getStringExtra(EXTRA_JOB_ID).orEmpty()
        if (jobId.isBlank()) {
            finish()
            return
        }

        findViewById<TextView>(R.id.backButton).setOnClickListener { finish() }
        val applyButton = findViewById<Button>(R.id.applyButton)
        val messageButton = findViewById<Button>(R.id.messageClientButton)
        val deleteButton = findViewById<Button>(R.id.deleteJobButton)
        applyButton.visibility = if (session.userRole == "Freelancer") android.view.View.VISIBLE else android.view.View.GONE
        messageButton.visibility = android.view.View.GONE

        viewModel.selectedJob.observe(this) { job ->
            if (job == null) return@observe
            findViewById<TextView>(R.id.jobTitle).text = job.title
            findViewById<TextView>(R.id.jobMeta).text = listOf(job.budget, job.duration)
                .filter { it.isNotBlank() }.joinToString("  •  ")
            findViewById<TextView>(R.id.jobDescription).text = job.description
            findViewById<TextView>(R.id.jobSkills).text = job.skillsRequired.joinToString(", ")
                .ifBlank { "No specific skills listed" }
            findViewById<TextView>(R.id.jobDeadline).text = job.deadline.ifBlank { "No deadline specified" }
            findViewById<TextView>(R.id.jobClient).text = job.clientId?.name
                ?.ifBlank { job.clientId.email } ?: "Client"
            val clientPhone = job.clientId?.phoneNumber.orEmpty().ifBlank {
                job.clientId?.contactInformation.orEmpty()
            }
            findViewById<TextView>(R.id.jobClientPhone).apply {
                visibility = if (clientPhone.isNotBlank()) android.view.View.VISIBLE else android.view.View.GONE
                text = if (clientPhone.isNotBlank()) "Phone: $clientPhone" else ""
                setOnClickListener {
                    if (clientPhone.isNotBlank()) {
                        startActivity(Intent(Intent.ACTION_DIAL, android.net.Uri.fromParts("tel", clientPhone, null)))
                    }
                }
            }
            val isOwner = session.userRole.equals("Client", ignoreCase = true) &&
                job.clientId?.id == session.userId
            deleteButton.visibility = if (isOwner) android.view.View.VISIBLE else android.view.View.GONE
            messageButton.visibility = if (job.clientId?.id == session.userId) {
                android.view.View.GONE
            } else {
                android.view.View.VISIBLE
            }

            applyButton.setOnClickListener {
                startActivity(Intent(this, ApplyJobActivity::class.java).apply {
                    putExtra(ApplyJobActivity.EXTRA_JOB_ID, job.id)
                    putExtra(ApplyJobActivity.EXTRA_CLIENT_ID, job.clientId?.id.orEmpty())
                })
            }
            messageButton.setOnClickListener {
                val client = job.clientId ?: return@setOnClickListener
                startActivity(Intent(this, ChatActivity::class.java).apply {
                    putExtra(ChatActivity.EXTRA_PEER_ID, client.id)
                    putExtra(ChatActivity.EXTRA_PEER_NAME, client.name.ifBlank { client.email })
                })
            }
            deleteButton.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Delete this job post?")
                    .setMessage("This also removes its applications and related notifications.")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Delete") { _, _ ->
                        deleteButton.isEnabled = false
                        viewModel.deleteJob(session.bearerToken, job.id) { success, error ->
                            deleteButton.isEnabled = true
                            if (success) {
                                Toast.makeText(this, "Job post deleted.", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                Toast.makeText(this, error ?: "Could not delete job post.", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    .show()
            }
        }
        viewModel.error.observe(this) { error ->
            if (!error.isNullOrBlank()) Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        }
        viewModel.fetchJob(session.bearerToken, jobId)
    }

    companion object {
        const val EXTRA_JOB_ID = "job_id"
    }
}

package com.example.freelancerconnect.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import com.example.freelancerconnect.R
import com.example.freelancerconnect.api.CreateApplicationRequest
import com.example.freelancerconnect.viewmodels.ApplicationViewModel

class ApplyJobActivity : BaseActivity() {

    private val viewModel: ApplicationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apply_job)

        val jobId = intent.getStringExtra(EXTRA_JOB_ID).orEmpty()
        val clientId = intent.getStringExtra(EXTRA_CLIENT_ID).orEmpty()
        if (jobId.isBlank() || clientId.isBlank()) {
            Toast.makeText(this, "Job information is missing.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        findViewById<TextView>(R.id.backButton).setOnClickListener { finish() }
        val coverLetter = findViewById<EditText>(R.id.coverLetter)
        val submitButton = findViewById<Button>(R.id.submitApplication)
        submitButton.setOnClickListener {
            submitButton.isEnabled = false
            viewModel.submitApplication(
                session.bearerToken,
                CreateApplicationRequest(jobId, clientId, coverLetter.text.toString().trim())
            ) { success, error ->
                submitButton.isEnabled = true
                if (success) {
                    Toast.makeText(this, "Application submitted.", Toast.LENGTH_SHORT).show()
                    navigateTo(AppliedJobsActivity::class.java, finishCurrent = true)
                } else {
                    Toast.makeText(this, error ?: "Application could not be submitted.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    companion object {
        const val EXTRA_JOB_ID = "job_id"
        const val EXTRA_CLIENT_ID = "client_id"
    }
}

package com.example.freelancerconnect.activities

import android.os.Bundle
import android.content.Intent
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.freelancerconnect.R
import com.example.freelancerconnect.adapters.ApplicationAdapter
import com.example.freelancerconnect.api.ApiApplication
import com.example.freelancerconnect.viewmodels.ApplicationViewModel

class AppliedJobsActivity : BaseActivity() {

    private val viewModel: ApplicationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_list)

        findViewById<TextView>(R.id.backButton).setOnClickListener { finish() }
        val isClient = session.userRole == "Client"
        requestedApplicationId = intent.getStringExtra(EXTRA_APPLICATION_ID).orEmpty()
        findViewById<TextView>(R.id.sectionLabel).text = if (isClient) "YOUR JOB POSTS" else "YOUR ACTIVITY"
        findViewById<TextView>(R.id.screenTitle).text = if (isClient) "Review applicants" else "My applications"
        findViewById<TextView>(R.id.screenSubtitle).text = if (isClient) {
            "Review, respond to, and message people who applied."
        } else {
            "Check the latest status of each application."
        }

        val emptyView = findViewById<TextView>(R.id.emptyMessage)
        val progress = findViewById<ProgressBar>(R.id.listProgress)
        val adapter = ApplicationAdapter(::showApplicationActions)
        findViewById<RecyclerView>(R.id.dataList).apply {
            layoutManager = LinearLayoutManager(this@AppliedJobsActivity)
            this.adapter = adapter
        }
        viewModel.applications.observe(this) { applications ->
            adapter.submitList(applications)
            if (requestedApplicationId.isNotBlank()) {
                applications.firstOrNull { it.id == requestedApplicationId }?.let { application ->
                    requestedApplicationId = ""
                    showApplicationActions(application)
                }
            }
            emptyView.visibility = if (applications.isEmpty()) View.VISIBLE else View.GONE
            if (applications.isEmpty() && viewModel.error.value == null) {
                emptyView.text = if (isClient) "No applications yet." else "You haven't applied to any jobs yet."
            }
        }
        viewModel.isLoading.observe(this) {
            progress.visibility = if (it && adapter.itemCount == 0) View.VISIBLE else View.GONE
        }
        viewModel.error.observe(this) { error ->
            if (!error.isNullOrBlank() && adapter.itemCount == 0) {
                emptyView.text = error
                emptyView.visibility = View.VISIBLE
            }
        }
        viewModel.fetchApplications(session.bearerToken)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        requestedApplicationId = intent.getStringExtra(EXTRA_APPLICATION_ID).orEmpty()
        viewModel.fetchApplications(session.bearerToken)
    }

    private fun showApplicationActions(application: ApiApplication) {
        if (session.userRole == "Client") {
            val applicant = application.freelancerId
            val details = buildList {
                add("Job: ${application.jobId?.title ?: "Job application"}")
                applicant?.name?.takeIf { it.isNotBlank() }?.let { add("Freelancer: $it") }
                applicant?.email?.takeIf { it.isNotBlank() }?.let { add("Email: $it") }
                applicant?.phoneNumber?.takeIf { it.isNotBlank() }?.let { add("Phone: $it") }
                applicant?.contactInformation?.takeIf { it.isNotBlank() }?.let { add("Contact: $it") }
                applicant?.experience?.takeIf { it.isNotBlank() }?.let { add("Experience: $it") }
                applicant?.skills?.takeIf { it.isNotEmpty() }?.let { add("Skills: ${it.joinToString(", ")}") }
                applicant?.bio?.takeIf { it.isNotBlank() }?.let { add("\n$it") }
                add("\nCover letter\n${application.coverLetter.ifBlank { "No cover letter was included." }}")
                add("\nStatus: ${application.status}")
            }.joinToString("\n")
            val dialog = AlertDialog.Builder(this)
                .setTitle(applicant?.name?.ifBlank { applicant.email } ?: "Applicant")
                .setMessage(details)
                .setNeutralButton("Message") { _, _ ->
                    applicant?.id?.takeIf { it.isNotBlank() }?.let {
                        openChat(it, applicant.name.ifBlank { applicant.email })
                    }
                }
                .setPositiveButton("Accept") { _, _ -> updateStatus(application, "Accepted") }
                .setNegativeButton("Reject") { _, _ -> updateStatus(application, "Rejected") }
                .create()
            dialog.show()
        } else {
            val dialog = AlertDialog.Builder(this)
                .setTitle(application.jobId?.title ?: "Job application")
                .setMessage("Status: ${application.status}\n\n${application.coverLetter.ifBlank { "No cover letter saved." }}")
                .setPositiveButton("Message client") { _, _ ->
                    val client = application.jobId?.clientId
                    client?.id?.takeIf { it.isNotBlank() }?.let {
                        openChat(it, client.name.ifBlank { client.email })
                    } ?: Toast.makeText(this, "Client details are unavailable.", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(
                    if (application.status == "Pending") "Withdraw" else "Close",
                    if (application.status == "Pending") { _, _ -> confirmWithdrawal(application) } else null
                )
                .create()
            dialog.show()
        }
    }

    private fun confirmWithdrawal(application: ApiApplication) {
        AlertDialog.Builder(this)
            .setTitle("Withdraw this application?")
            .setMessage("The client will no longer see this application.")
            .setNegativeButton("Keep application", null)
            .setPositiveButton("Withdraw") { _, _ ->
                viewModel.withdrawApplication(session.bearerToken, application.id) { success, error ->
                    Toast.makeText(
                        this,
                        if (success) "Application withdrawn." else error ?: "Could not withdraw application.",
                        Toast.LENGTH_LONG
                    ).show()
                    if (success) viewModel.fetchApplications(session.bearerToken)
                }
            }
            .show()
    }

    private fun updateStatus(application: ApiApplication, status: String) {
        viewModel.updateApplicationStatus(session.bearerToken, application.id, status) { success, error ->
            Toast.makeText(
                this,
                if (success) "Application ${status.lowercase()}." else error ?: "Could not update application.",
                Toast.LENGTH_LONG
            ).show()
            if (success) viewModel.fetchApplications(session.bearerToken)
        }
    }

    private fun openChat(userId: String, name: String) {
        startActivity(Intent(this, ChatActivity::class.java).apply {
            putExtra(ChatActivity.EXTRA_PEER_ID, userId)
            putExtra(ChatActivity.EXTRA_PEER_NAME, name)
        })
    }

    private var requestedApplicationId = ""

    companion object {
        const val EXTRA_APPLICATION_ID = "application_id"
    }
}

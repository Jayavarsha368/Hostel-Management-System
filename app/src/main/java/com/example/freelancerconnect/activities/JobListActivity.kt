package com.example.freelancerconnect.activities

import android.os.Bundle
import android.content.Intent
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.freelancerconnect.R
import com.example.freelancerconnect.adapters.JobAdapter
import com.example.freelancerconnect.viewmodels.JobViewModel

class JobListActivity : BaseActivity() {

    private val viewModel: JobViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_list)

        val isClient = session.userRole.equals("Client", ignoreCase = true)
        findViewById<TextView>(R.id.backButton).setOnClickListener { finish() }
        findViewById<TextView>(R.id.sectionLabel).text = if (isClient) "YOUR WORKSPACE" else "OPPORTUNITIES"
        findViewById<TextView>(R.id.screenTitle).text = if (isClient) "Your job posts" else "Find your next gig"
        findViewById<TextView>(R.id.screenSubtitle).text = if (isClient) {
            "Open a post to review applicants or remove it."
        } else {
            "Open a listing to review details and apply."
        }

        val emptyView = findViewById<TextView>(R.id.emptyMessage)
        val progress = findViewById<ProgressBar>(R.id.listProgress)
        val adapter = JobAdapter { job ->
            startActivity(Intent(this, JobDetailsActivity::class.java).apply {
                putExtra(JobDetailsActivity.EXTRA_JOB_ID, job.id)
            })
        }

        findViewById<RecyclerView>(R.id.dataList).apply {
            layoutManager = LinearLayoutManager(this@JobListActivity)
            this.adapter = adapter
        }

        viewModel.jobs.observe(this) { jobs ->
            val visibleJobs = if (isClient) jobs.filter { it.clientId?.id == session.userId } else jobs
            adapter.submitList(visibleJobs)
            emptyView.visibility = if (visibleJobs.isEmpty()) View.VISIBLE else View.GONE
            if (visibleJobs.isEmpty() && viewModel.error.value == null) {
                emptyView.text = if (isClient) "You haven't posted any jobs yet." else "No jobs posted yet."
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
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchJobs(session.bearerToken)
    }
}

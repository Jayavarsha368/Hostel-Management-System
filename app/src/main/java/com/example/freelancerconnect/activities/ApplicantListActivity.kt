package com.example.freelancerconnect.activities

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.freelancerconnect.R

class ApplicantListActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple)
        try { findViewById<TextView>(R.id.backBtn).setOnClickListener { finish() } } catch (_: Exception) {}
        findViewById<TextView>(R.id.screenLabel).text = "APPLICANTS"
        findViewById<TextView>(R.id.screenTitle).text = "Who's applied."
        findViewById<TextView>(R.id.screenSubtitle).text = "Review freelancer applications for your job posting here."
        findViewById<Button>(R.id.primaryButton).setOnClickListener { finish() }
    }
}

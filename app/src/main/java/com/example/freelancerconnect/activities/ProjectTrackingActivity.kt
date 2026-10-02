package com.example.freelancerconnect.activities

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.freelancerconnect.R

class ProjectTrackingActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple)
        try { findViewById<TextView>(R.id.backBtn).setOnClickListener { finish() } } catch (_: Exception) {}
        findViewById<TextView>(R.id.screenLabel).text = "PROJECT TRACKER"
        findViewById<TextView>(R.id.screenTitle).text = "Track progress."
        findViewById<TextView>(R.id.screenSubtitle).text = "Active projects and milestones will be displayed here."
        findViewById<Button>(R.id.primaryButton).setOnClickListener { finish() }
    }
}

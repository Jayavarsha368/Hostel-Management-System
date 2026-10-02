package com.example.freelancerconnect.activities

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.freelancerconnect.R

class ReviewActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple)
        try { findViewById<TextView>(R.id.backBtn).setOnClickListener { finish() } } catch (_: Exception) {}
        findViewById<TextView>(R.id.screenLabel).text = "REVIEWS"
        findViewById<TextView>(R.id.screenTitle).text = "Rate your experience."
        findViewById<TextView>(R.id.screenSubtitle).text = "Leave and view ratings for completed projects here."
        findViewById<Button>(R.id.primaryButton).setOnClickListener { finish() }
    }
}

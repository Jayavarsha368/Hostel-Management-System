package com.example.freelancerconnect.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.util.Linkify
import android.text.method.LinkMovementMethod
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.view.View
import android.widget.ScrollView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AlertDialog
import com.example.freelancerconnect.R
import com.example.freelancerconnect.adapters.FreelancerAdapter
import com.example.freelancerconnect.api.ApiClient
import com.example.freelancerconnect.api.ApiUser
import com.example.freelancerconnect.viewmodels.FreelancerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class FreelancerDirectoryActivity : BaseActivity() {

    private val viewModel: FreelancerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (session.userRole != "Client") {
            Toast.makeText(this, "Freelancer browsing is available to clients.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setContentView(R.layout.activity_data_list)
        findViewById<TextView>(R.id.backButton).setOnClickListener { finish() }
        findViewById<TextView>(R.id.sectionLabel).text = "TALENT DIRECTORY"
        findViewById<TextView>(R.id.screenTitle).text = "Find your next collaborator"
        findViewById<TextView>(R.id.screenSubtitle).text =
            "Browse freelancer profiles and contact them directly."

        val emptyView = findViewById<TextView>(R.id.emptyMessage)
        val progress = findViewById<ProgressBar>(R.id.listProgress)
        val adapter = FreelancerAdapter(
            onProfileClicked = ::showFreelancerProfile,
            onMessageClicked = ::messageFreelancer,
            onEmailClicked = ::emailFreelancer,
            onCallClicked = ::callFreelancer
        )
        findViewById<RecyclerView>(R.id.dataList).apply {
            layoutManager = LinearLayoutManager(this@FreelancerDirectoryActivity)
            this.adapter = adapter
        }

        viewModel.freelancers.observe(this) { freelancers ->
            adapter.submitList(freelancers)
            emptyView.visibility = if (freelancers.isEmpty()) View.VISIBLE else View.GONE
            if (freelancers.isEmpty() && viewModel.error.value == null) {
                emptyView.text = "No freelancer profiles are available yet."
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
        if (session.userRole == "Client") viewModel.fetchFreelancers(session.bearerToken)
    }

    private fun messageFreelancer(freelancer: ApiUser) {
        if (freelancer.id.isBlank()) {
            Toast.makeText(this, "Freelancer details are unavailable.", Toast.LENGTH_SHORT).show()
            return
        }
        startActivity(Intent(this, ChatActivity::class.java).apply {
            putExtra(ChatActivity.EXTRA_PEER_ID, freelancer.id)
            putExtra(ChatActivity.EXTRA_PEER_NAME, freelancer.name.ifBlank { freelancer.email })
        })
    }

    private fun showFreelancerProfile(freelancer: ApiUser) {
        val details = listOfNotNull(
            profileDetail("Email", freelancer.email),
            profileDetail("Phone", freelancer.phoneNumber),
            profileDetail("Contact", freelancer.contactInformation),
            profileDetail("Experience", freelancer.experience),
            profileDetail("Education", freelancer.education),
            profileDetail("Skills", freelancer.skills.joinToString(", ")),
            profileDetail("About", freelancer.bio),
            profileDetail("GitHub", freelancer.githubUrl),
            profileDetail("LinkedIn", freelancer.linkedinUrl),
            profileDetail(
                "Portfolio",
                freelancer.portfolioUrl.takeIf { it.startsWith("http", true) }
                    ?: if (freelancer.portfolioUrl.isNotBlank()) "File available" else ""
            ),
            profileDetail("Resume", if (freelancer.resumeUrl.isNotBlank()) "File available" else "")
        ).joinToString("\n\n")

        val density = resources.displayMetrics.density
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((20 * density).toInt(), (8 * density).toInt(), (20 * density).toInt(), 0)
        }
        val detailText = TextView(this).apply {
            text = details.ifBlank { "No additional profile details provided." }
            textSize = 15f
            setTextColor(getColor(R.color.ink))
            autoLinkMask = Linkify.WEB_URLS
            movementMethod = LinkMovementMethod.getInstance()
        }
        content.addView(detailText)

        if (freelancer.resumeUrl.isNotBlank()) {
            content.addView(profileFileButton("Open resume") {
                openProfileFile(freelancer, "resume", freelancer.resumeUrl)
            })
        }
        if (freelancer.portfolioUrl.isNotBlank()) {
            content.addView(profileFileButton("Open portfolio") {
                openProfileFile(freelancer, "portfolio", freelancer.portfolioUrl)
            })
        }

        val scrollView = ScrollView(this).apply { addView(content) }
        AlertDialog.Builder(this)
            .setTitle(freelancer.name.ifBlank { "Freelancer profile" })
            .setView(scrollView)
            .setPositiveButton("Close", null)
            .show()
    }

    private fun profileDetail(label: String, value: String): String? =
        value.takeIf { it.isNotBlank() }?.let { "$label\n$it" }

    private fun profileFileButton(label: String, onClick: () -> Unit): Button = Button(this).apply {
        text = label
        isAllCaps = false
        setOnClickListener { onClick() }
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            val margin = (8 * resources.displayMetrics.density).toInt()
            topMargin = margin
        }
    }

    private fun openProfileFile(freelancer: ApiUser, kind: String, storedValue: String) {
        if (storedValue.startsWith("http://", true) || storedValue.startsWith("https://", true)) {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(storedValue)))
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(this, "No app can open this link.", Toast.LENGTH_LONG).show()
            }
            return
        }

        lifecycleScope.launch {
            try {
                val response = ApiClient.api.getProfileFile(session.bearerToken, freelancer.id, kind)
                if (!response.isSuccessful) {
                    response.errorBody()?.close()
                    throw IllegalStateException("This file is unavailable. Ask the freelancer to upload it again.")
                }
                val body = response.body() ?: throw IllegalStateException("The server returned an empty file.")
                val mimeType = response.headers()["Content-Type"]
                    ?.substringBefore(';')
                    ?.takeIf { it.isNotBlank() } ?: "application/octet-stream"
                val extension = when (mimeType) {
                    "application/pdf" -> "pdf"
                    "application/msword" -> "doc"
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "docx"
                    "image/jpeg" -> "jpg"
                    "image/png" -> "png"
                    else -> "bin"
                }
                val file = withContext(Dispatchers.IO) {
                    val directory = File(cacheDir, "profile-files").apply { mkdirs() }
                    File(directory, "${freelancer.id}-$kind.$extension").also { destination ->
                        body.use { responseBody ->
                            responseBody.byteStream().use { input ->
                                destination.outputStream().use { output -> input.copyTo(output) }
                            }
                        }
                    }
                }
                val fileUri = FileProvider.getUriForFile(
                    this@FreelancerDirectoryActivity,
                    "$packageName.fileprovider",
                    file
                )
                startActivity(Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(fileUri, mimeType)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                })
            } catch (error: ActivityNotFoundException) {
                Toast.makeText(this@FreelancerDirectoryActivity, "No document viewer is available.", Toast.LENGTH_LONG).show()
            } catch (error: Exception) {
                Toast.makeText(
                    this@FreelancerDirectoryActivity,
                    error.message ?: "Could not open this file.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun emailFreelancer(freelancer: ApiUser) {
        try {
            startActivity(Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.fromParts("mailto", freelancer.email, null)
            })
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, "No email app is available on this phone.", Toast.LENGTH_LONG).show()
        }
    }

    private fun callFreelancer(freelancer: ApiUser) {
        try {
            val phoneNumber = freelancer.phoneNumber.ifBlank { freelancer.contactInformation }
            startActivity(Intent(Intent.ACTION_DIAL).apply {
                data = Uri.fromParts("tel", phoneNumber, null)
            })
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, "No phone app is available on this phone.", Toast.LENGTH_LONG).show()
        }
    }
}

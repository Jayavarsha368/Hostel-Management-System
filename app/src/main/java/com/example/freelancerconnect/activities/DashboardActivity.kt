package com.example.freelancerconnect.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.freelancerconnect.R
import com.example.freelancerconnect.notifications.PushTokenManager
import com.example.freelancerconnect.utils.SkillMatcher

class DashboardActivity : BaseActivity() {

    private val notificationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        PushTokenManager.register(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // ── Personalise greeting ───────────────────────────────────────────────
        val name = session.userName.ifEmpty { null }
        val role = session.userRole
        val isClient = role.equals("Client", ignoreCase = true)

        val greetingText = if (name != null && name.isNotEmpty()) {
            "Welcome ${name.split(" ").first()}"
        } else {
            "Welcome"
        }

        findViewById<TextView>(R.id.greetingText).text = greetingText
        findViewById<TextView>(R.id.roleChip).text = role.ifEmpty { "Member" }
        findViewById<TextView>(R.id.dashboardSubtitle).text = if (isClient) {
            "Find skilled people and move your projects forward."
        } else {
            "Find work, build your reputation, and keep projects moving."
        }
        findViewById<TextView>(R.id.profileCardTitle).text = if (isClient) {
            "Build your client profile"
        } else {
            "Complete your profile to get discovered"
        }
        findViewById<TextView>(R.id.profileCardSubtitle).text = if (isClient) {
            "Add your company details and project needs to help freelancers understand your work."
        } else {
            "Add skills, experience and a bio to stand out."
        }
        findViewById<Button>(R.id.profileActionButton).text =
            if (isClient) "Edit client profile  →" else "Edit profile  →"
        findViewById<TextView>(R.id.quickActionsLabel).text =
            if (isClient) "CLIENT WORKSPACE" else "FREELANCER WORKSPACE"
        findViewById<View>(R.id.skillMatchCard).visibility = if (isClient) View.GONE else View.VISIBLE
        listOf(
            R.id.achievementsLabel,
            R.id.achievementsText,
            R.id.projectProgressLabel,
            R.id.projectProgressCard
        ).forEach { findViewById<View>(it).visibility = if (isClient) View.GONE else View.VISIBLE }

        val targetSkills = listOf("Java", "Android", "Firebase", "UI Design")
        val matchScore = SkillMatcher.score(targetSkills, session.userSkills)
        findViewById<TextView>(R.id.skillMatchValue).text =
            if (session.userSkills.isEmpty()) "Add skills" else "$matchScore%"
        findViewById<TextView>(R.id.skillMatchSubtitle).text =
            if (session.userSkills.isEmpty()) {
                "Add skills to your profile to see your local match score."
            } else {
                "Your profile matches a suggested Android project."
            }
        findViewById<android.widget.ProgressBar>(R.id.skillMatchProgress).progress = matchScore

        // ── Top bar actions ───────────────────────────────────────────────────
        findViewById<TextView>(R.id.profileButton).setOnClickListener {
            navigateTo(ProfileActivity::class.java)
        }

        findViewById<TextView>(R.id.logoutButton).setOnClickListener {
            PushTokenManager.unregister(this, session.bearerToken)
            session.clearSession()
            navigateAndClearStack(LoginActivity::class.java)
        }

        // ── Card CTA ──────────────────────────────────────────────────────────
        findViewById<Button>(R.id.editProfileButton).setOnClickListener {
            navigateTo(ProfileActivity::class.java)
        }

        findViewById<Button>(R.id.profileActionButton).setOnClickListener {
            navigateTo(ProfileActivity::class.java)
        }

        // ── Main navigation ───────────────────────────────────────────────────
        findViewById<Button>(R.id.jobsButton).setOnClickListener {
            navigateTo(if (isClient) FreelancerDirectoryActivity::class.java else JobListActivity::class.java)
        }

        findViewById<Button>(R.id.postJobButton).setOnClickListener {
            navigateTo(if (isClient) PostJobActivity::class.java else AppliedJobsActivity::class.java)
        }

        findViewById<Button>(R.id.jobsButton).text =
            if (isClient) "◎\nFind Freelancers" else "⌕\nFind Jobs"
        findViewById<Button>(R.id.postJobButton).text =
            if (isClient) "＋\nPost a Job" else "▣\nMy Applications"

        findViewById<Button>(R.id.chatButton).setOnClickListener {
            navigateTo(ChatListActivity::class.java)
        }

        findViewById<Button>(R.id.notificationsButton).setOnClickListener {
            navigateTo(NotificationActivity::class.java)
        }

        findViewById<Button>(R.id.appliedJobsButton).apply {
            text = "Review Applicants"
            visibility = if (isClient) View.VISIBLE else View.GONE
            setOnClickListener {
                navigateTo(AppliedJobsActivity::class.java)
            }
        }

        findViewById<TextView>(R.id.jobsNav).apply {
            text = if (isClient) "◎\nPeople" else "⌕\nJobs"
            setOnClickListener {
                navigateTo(if (isClient) FreelancerDirectoryActivity::class.java else JobListActivity::class.java)
            }
        }

        findViewById<TextView>(R.id.chatNav).setOnClickListener {
            navigateTo(ChatListActivity::class.java)
        }

        findViewById<TextView>(R.id.profileNav).setOnClickListener {
            navigateTo(ProfileActivity::class.java)
        }
    }
}
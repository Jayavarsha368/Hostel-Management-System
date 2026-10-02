package com.example.freelancerconnect.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.freelancerconnect.R
import com.example.freelancerconnect.adapters.NotificationAdapter
import com.example.freelancerconnect.api.ApiNotification
import com.example.freelancerconnect.viewmodels.NotificationViewModel

class NotificationActivity : BaseActivity() {

    private val viewModel: NotificationViewModel by viewModels()
    private val refreshHandler = Handler(Looper.getMainLooper())
    private val refreshNotifications = object : Runnable {
        override fun run() {
            viewModel.fetchNotifications(session.bearerToken)
            refreshHandler.postDelayed(this, 8_000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_list)

        findViewById<TextView>(R.id.backButton).setOnClickListener { finish() }
        findViewById<TextView>(R.id.sectionLabel).text = "UPDATES"
        findViewById<TextView>(R.id.screenTitle).text = "Notifications"
        findViewById<TextView>(R.id.screenSubtitle).text = "Messages and application activity for your account."

        val markAllButton = findViewById<Button>(R.id.headerAction)
        markAllButton.text = "Mark all read"
        markAllButton.visibility = View.VISIBLE
        markAllButton.setOnClickListener { viewModel.markAllRead(session.bearerToken) }

        val emptyView = findViewById<TextView>(R.id.emptyMessage)
        val progress = findViewById<ProgressBar>(R.id.listProgress)
        val adapter = NotificationAdapter(::openNotification)
        findViewById<RecyclerView>(R.id.dataList).apply {
            layoutManager = LinearLayoutManager(this@NotificationActivity)
            this.adapter = adapter
        }
        viewModel.notifications.observe(this) { notifications ->
            adapter.submitList(notifications)
            emptyView.visibility = if (notifications.isEmpty()) View.VISIBLE else View.GONE
            if (notifications.isEmpty() && viewModel.error.value == null) {
                emptyView.text = "You're all caught up."
            }
        }
        viewModel.isLoading.observe(this) {
            progress.visibility = if (it && adapter.itemCount == 0) View.VISIBLE else View.GONE
        }
        viewModel.error.observe(this) { error ->
            if (!error.isNullOrBlank()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                if (adapter.itemCount == 0) {
                    emptyView.text = error
                    emptyView.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshHandler.post(refreshNotifications)
    }

    override fun onPause() {
        refreshHandler.removeCallbacks(refreshNotifications)
        super.onPause()
    }

    private fun openNotification(notification: ApiNotification) {
        if (!notification.read) viewModel.markRead(session.bearerToken, notification.id)

        val relatedType = notification.relatedType.ifBlank {
            when {
                notification.type.contains("message", ignoreCase = true) -> "message"
                notification.type.contains("application", ignoreCase = true) -> "application"
                else -> ""
            }
        }

        when (relatedType) {
            "message" -> {
                if (notification.peerId.isBlank() || notification.conversationId.isBlank()) {
                    navigateTo(ChatListActivity::class.java)
                    return
                }
                startActivity(Intent(this, ChatActivity::class.java).apply {
                    putExtra(ChatActivity.EXTRA_PEER_ID, notification.peerId)
                    putExtra(ChatActivity.EXTRA_PEER_NAME, notification.peerName)
                    putExtra(ChatActivity.EXTRA_CONVERSATION_ID, notification.conversationId)
                })
            }
            "application" -> {
                startActivity(Intent(this, AppliedJobsActivity::class.java).apply {
                    putExtra(AppliedJobsActivity.EXTRA_APPLICATION_ID, notification.relatedId)
                })
            }
            else -> Toast.makeText(this, "This notification has no linked page.", Toast.LENGTH_SHORT).show()
        }
    }
}

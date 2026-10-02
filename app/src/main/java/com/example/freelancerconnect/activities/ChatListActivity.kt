package com.example.freelancerconnect.activities

import android.os.Bundle
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.freelancerconnect.R
import com.example.freelancerconnect.adapters.ConversationAdapter
import com.example.freelancerconnect.viewmodels.MessageViewModel

class ChatListActivity : BaseActivity() {

    private val viewModel: MessageViewModel by viewModels()
    private val refreshHandler = Handler(Looper.getMainLooper())
    private val refreshConversations = object : Runnable {
        override fun run() {
            viewModel.fetchConversations(session.bearerToken)
            refreshHandler.postDelayed(this, 8_000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inbox)

        findViewById<TextView>(R.id.backButton).setOnClickListener { finish() }
        val isClient = session.userRole == "Client"
        findViewById<TextView>(R.id.inboxSubtitle).text = if (isClient) {
            "Browse freelancer profiles and start a conversation."
        } else {
            "Message a client from a job or an application."
        }
        findViewById<Button>(R.id.startChatButton).apply {
            text = if (isClient) "Browse freelancers" else "Browse jobs"
            setOnClickListener {
                navigateTo(if (isClient) FreelancerDirectoryActivity::class.java else JobListActivity::class.java)
            }
        }
        val emptyView = findViewById<TextView>(R.id.emptyMessage)
        val progress = findViewById<ProgressBar>(R.id.listProgress)
        val adapter = ConversationAdapter(session.userId) { message, peerId, peerName ->
            startActivity(Intent(this, ChatActivity::class.java).apply {
                putExtra(ChatActivity.EXTRA_PEER_ID, peerId)
                putExtra(ChatActivity.EXTRA_PEER_NAME, peerName)
                putExtra(ChatActivity.EXTRA_CONVERSATION_ID, message.conversationId)
            })
        }

        findViewById<RecyclerView>(R.id.dataList).apply {
            layoutManager = LinearLayoutManager(this@ChatListActivity)
            this.adapter = adapter
        }

        viewModel.conversations.observe(this) { items ->
            adapter.submitList(items)
            emptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            if (items.isEmpty() && viewModel.error.value == null) {
                emptyView.text = if (isClient) {
                    "No conversations yet. Browse freelancers and tap Message to get started."
                } else {
                    "No conversations yet. Open a job or application and tap Message client."
                }
            }
        }
        viewModel.isLoading.observe(this) {
            progress.visibility = if (it && adapter.itemCount == 0) View.VISIBLE else View.GONE
        }
        viewModel.error.observe(this) { message ->
            if (!message.isNullOrBlank() && adapter.itemCount == 0) {
                emptyView.text = message
                emptyView.visibility = View.VISIBLE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshHandler.post(refreshConversations)
    }

    override fun onPause() {
        refreshHandler.removeCallbacks(refreshConversations)
        super.onPause()
    }
}

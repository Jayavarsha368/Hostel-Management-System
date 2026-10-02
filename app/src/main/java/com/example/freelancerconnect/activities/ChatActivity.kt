package com.example.freelancerconnect.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.freelancerconnect.R
import com.example.freelancerconnect.adapters.MessageAdapter
import com.example.freelancerconnect.api.SendMessageRequest
import com.example.freelancerconnect.viewmodels.MessageViewModel

class ChatActivity : BaseActivity() {

    private val viewModel: MessageViewModel by viewModels()
    private val refreshHandler = Handler(Looper.getMainLooper())
    private var conversationId = ""
    private val refreshMessages = object : Runnable {
        override fun run() {
            if (conversationId.isNotBlank()) {
                viewModel.fetchMessages(session.bearerToken, conversationId)
                refreshHandler.postDelayed(this, 5000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val peerId = intent.getStringExtra(EXTRA_PEER_ID).orEmpty()
        if (peerId.isBlank() || session.userId.isBlank()) {
            Toast.makeText(this, "Could not open this conversation.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        conversationId = intent.getStringExtra(EXTRA_CONVERSATION_ID)
            ?: listOf(session.userId, peerId).sorted().joinToString("_")

        findViewById<TextView>(R.id.backButton).setOnClickListener { finish() }
        findViewById<TextView>(R.id.peerName).text =
            intent.getStringExtra(EXTRA_PEER_NAME).orEmpty().ifBlank { "Conversation" }

        val emptyView = findViewById<TextView>(R.id.emptyMessages)
        val messageInput = findViewById<EditText>(R.id.messageInput)
        val sendButton = findViewById<Button>(R.id.sendButton)
        val adapter = MessageAdapter(session.userId)
        findViewById<RecyclerView>(R.id.messageList).apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            this.adapter = adapter
        }

        viewModel.messages.observe(this) { messages ->
            adapter.submitList(messages) {
                if (adapter.itemCount > 0) {
                    findViewById<RecyclerView>(R.id.messageList)
                        .scrollToPosition(adapter.itemCount - 1)
                }
            }
            emptyView.visibility = if (messages.isEmpty()) View.VISIBLE else View.GONE
            if (messages.isEmpty() && viewModel.error.value == null) {
                emptyView.text = "No messages yet. Send the first one."
            }
        }
        viewModel.error.observe(this) { message ->
            if (!message.isNullOrBlank() && adapter.itemCount == 0) {
                emptyView.text = message
                emptyView.visibility = View.VISIBLE
            }
        }

        sendButton.setOnClickListener {
            val text = messageInput.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            sendButton.isEnabled = false
            viewModel.sendMessage(
                session.bearerToken,
                SendMessageRequest(conversationId, peerId, text)
            ) { success, error ->
                sendButton.isEnabled = true
                if (success) {
                    messageInput.text.clear()
                    viewModel.fetchMessages(session.bearerToken, conversationId)
                } else {
                    Toast.makeText(this, error ?: "Message could not be sent.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (conversationId.isNotBlank()) refreshHandler.post(refreshMessages)
    }

    override fun onPause() {
        refreshHandler.removeCallbacks(refreshMessages)
        super.onPause()
    }

    companion object {
        const val EXTRA_PEER_ID = "peer_id"
        const val EXTRA_PEER_NAME = "peer_name"
        const val EXTRA_CONVERSATION_ID = "conversation_id"
    }
}

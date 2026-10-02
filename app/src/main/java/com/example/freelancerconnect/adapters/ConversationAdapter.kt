package com.example.freelancerconnect.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.freelancerconnect.api.ApiMessage

class ConversationAdapter(
    private val currentUserId: String,
    private val onConversationClicked: (ApiMessage, String, String) -> Unit
) : ListAdapter<ApiMessage, ConversationAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val titleView: TextView = view.findViewById(android.R.id.text1)
        private val previewView: TextView = view.findViewById(android.R.id.text2)

        fun bind(message: ApiMessage) {
            val outgoing = message.senderId?.id == currentUserId
            val otherUser = if (outgoing) message.receiverId else message.senderId
            titleView.text = otherUser?.name?.ifBlank { otherUser.email } ?: "Conversation"
            previewView.text = (if (outgoing) "You: " else "") + message.text
            itemView.setOnClickListener {
                otherUser?.id?.takeIf { it.isNotBlank() }?.let { peerId ->
                    onConversationClicked(message, peerId, titleView.text.toString())
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class DiffCallback : DiffUtil.ItemCallback<ApiMessage>() {
        override fun areItemsTheSame(oldItem: ApiMessage, newItem: ApiMessage) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ApiMessage, newItem: ApiMessage) =
            oldItem == newItem
    }
}
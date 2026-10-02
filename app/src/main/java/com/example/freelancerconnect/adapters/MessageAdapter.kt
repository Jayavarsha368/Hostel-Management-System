package com.example.freelancerconnect.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.freelancerconnect.api.ApiMessage

class MessageAdapter(
    private val currentUserId: String
) : ListAdapter<ApiMessage, MessageAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val messageView: TextView = view.findViewById(android.R.id.text1)
        private val timeView: TextView = view.findViewById(android.R.id.text2)

        fun bind(message: ApiMessage) {
            val prefix = if (message.senderId?.id == currentUserId) "You" else "Them"
            messageView.text = "$prefix: ${message.text}"
            timeView.text = message.createdAt.substringAfter('T').take(5)
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
        override fun areItemsTheSame(oldItem: ApiMessage, newItem: ApiMessage) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ApiMessage, newItem: ApiMessage) = oldItem == newItem
    }
}

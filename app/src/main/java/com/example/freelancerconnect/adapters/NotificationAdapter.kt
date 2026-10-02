package com.example.freelancerconnect.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.freelancerconnect.api.ApiNotification

class NotificationAdapter(
    private val onNotificationClicked: ((ApiNotification) -> Unit)? = null
) : ListAdapter<ApiNotification, NotificationAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val typeView: TextView = view.findViewById(android.R.id.text1)
        private val messageView: TextView = view.findViewById(android.R.id.text2)

        fun bind(notification: ApiNotification) {
            typeView.text = notification.type
            messageView.text = notification.message
            itemView.alpha = if (notification.read) 0.6f else 1.0f
            itemView.setOnClickListener { onNotificationClicked?.invoke(notification) }
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

    private class DiffCallback : DiffUtil.ItemCallback<ApiNotification>() {
        override fun areItemsTheSame(oldItem: ApiNotification, newItem: ApiNotification) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ApiNotification, newItem: ApiNotification) =
            oldItem == newItem
    }
}

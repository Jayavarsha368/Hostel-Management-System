package com.example.freelancerconnect.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.freelancerconnect.api.ApiApplication

class ApplicationAdapter(
    private val onApplicationClicked: ((ApiApplication) -> Unit)? = null
) : ListAdapter<ApiApplication, ApplicationAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val titleView: TextView = view.findViewById(android.R.id.text1)
        private val subtitleView: TextView = view.findViewById(android.R.id.text2)

        fun bind(application: ApiApplication) {
            titleView.text = application.jobId?.title ?: "Job application"
            subtitleView.text = buildString {
                append(application.status)
                application.freelancerId?.name?.takeIf { it.isNotBlank() }?.let {
                    append("  •  ")
                    append(it)
                }
            }
            itemView.setOnClickListener { onApplicationClicked?.invoke(application) }
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

    private class DiffCallback : DiffUtil.ItemCallback<ApiApplication>() {
        override fun areItemsTheSame(oldItem: ApiApplication, newItem: ApiApplication) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ApiApplication, newItem: ApiApplication) =
            oldItem == newItem
    }
}

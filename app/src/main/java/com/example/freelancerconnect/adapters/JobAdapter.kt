package com.example.freelancerconnect.adapters

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.freelancerconnect.api.ApiJob

class JobAdapter(
    private val onJobClicked: (ApiJob) -> Unit
) : ListAdapter<ApiJob, JobAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val titleView: TextView = view.findViewById(android.R.id.text1)
        private val subtitleView: TextView = view.findViewById(android.R.id.text2)

        init {
            subtitleView.maxLines = 3
            subtitleView.ellipsize = TextUtils.TruncateAt.END
        }

        fun bind(job: ApiJob) {
            titleView.text = job.title
            subtitleView.text = buildString {
                val details = listOf(job.budget, job.duration).filter { it.isNotBlank() }
                append(details.joinToString("  \u2022  "))
                val description = job.description.replace(Regex("\\s+"), " ").trim()
                if (description.isNotEmpty()) {
                    if (isNotEmpty()) append("  \u2022  ")
                    append(description.take(100))
                    if (description.length > 100) append("...")
                }
            }
            itemView.setOnClickListener { onJobClicked(job) }
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

    private class DiffCallback : DiffUtil.ItemCallback<ApiJob>() {
        override fun areItemsTheSame(oldItem: ApiJob, newItem: ApiJob) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ApiJob, newItem: ApiJob) = oldItem == newItem
    }
}

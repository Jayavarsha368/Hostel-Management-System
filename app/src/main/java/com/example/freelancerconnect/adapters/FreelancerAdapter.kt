package com.example.freelancerconnect.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.freelancerconnect.R
import com.example.freelancerconnect.api.ApiUser

class FreelancerAdapter(
    private val onProfileClicked: (ApiUser) -> Unit,
    private val onMessageClicked: (ApiUser) -> Unit,
    private val onEmailClicked: (ApiUser) -> Unit,
    private val onCallClicked: (ApiUser) -> Unit
) : ListAdapter<ApiUser, FreelancerAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nameView: TextView = view.findViewById(R.id.freelancerName)
        private val experienceView: TextView = view.findViewById(R.id.freelancerExperience)
        private val emailView: TextView = view.findViewById(R.id.freelancerEmail)
        private val skillsView: TextView = view.findViewById(R.id.freelancerSkills)
        private val bioView: TextView = view.findViewById(R.id.freelancerBio)
        private val contactView: TextView = view.findViewById(R.id.freelancerContact)
        private val profileButton: Button = view.findViewById(R.id.viewFreelancerProfile)
        private val messageButton: Button = view.findViewById(R.id.messageFreelancer)
        private val emailButton: Button = view.findViewById(R.id.emailFreelancer)
        private val callButton: Button = view.findViewById(R.id.callFreelancer)

        fun bind(freelancer: ApiUser) {
            nameView.text = freelancer.name.ifBlank { "Freelancer" }
            experienceView.text = freelancer.experience.ifBlank { "Experience not listed" }
            emailView.text = freelancer.email
            emailView.visibility = if (freelancer.email.isBlank()) View.GONE else View.VISIBLE
            skillsView.text = if (freelancer.skills.isEmpty()) {
                "Skills not listed"
            } else {
                "Skills: ${freelancer.skills.joinToString("  |  ")}"
            }
            bioView.text = freelancer.bio
            bioView.visibility = if (freelancer.bio.isBlank()) View.GONE else View.VISIBLE
            contactView.text = freelancer.phoneNumber.ifBlank {
                freelancer.contactInformation.ifBlank { "No contact details provided." }
            }

            profileButton.setOnClickListener { onProfileClicked(freelancer) }
            messageButton.setOnClickListener { onMessageClicked(freelancer) }
            emailButton.isEnabled = freelancer.email.isNotBlank()
            emailButton.setOnClickListener { onEmailClicked(freelancer) }
            val phoneNumber = freelancer.phoneNumber.ifBlank { freelancer.contactInformation }
            callButton.visibility = if (isPhoneNumber(phoneNumber)) View.VISIBLE else View.GONE
            callButton.setOnClickListener { onCallClicked(freelancer) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_freelancer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private fun isPhoneNumber(value: String): Boolean {
        val trimmed = value.trim()
        return trimmed.count(Char::isDigit) >= 7 && trimmed.all {
            it.isDigit() || it in "+()-. "
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<ApiUser>() {
        override fun areItemsTheSame(oldItem: ApiUser, newItem: ApiUser) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ApiUser, newItem: ApiUser) = oldItem == newItem
    }
}

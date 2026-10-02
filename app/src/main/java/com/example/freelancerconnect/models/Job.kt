package com.example.freelancerconnect.models

data class Job(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val budget: String = "",
    val skillsRequired: List<String> = emptyList(),
    val duration: String = "",
    val deadline: String = "",
    val clientId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

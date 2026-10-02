package com.example.freelancerconnect.models

data class Application(
    val id: String = "",
    val jobId: String = "",
    val freelancerId: String = "",
    val clientId: String = "",
    val coverLetter: String = "",
    val status: String = "Pending",
    val createdAt: Long = System.currentTimeMillis()
)

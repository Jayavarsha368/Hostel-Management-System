package com.example.freelancerconnect.models

data class Project(
    val id: String = "",
    val applicationId: String = "",
    val status: String = "Not Started",
    val progress: Int = 0
)

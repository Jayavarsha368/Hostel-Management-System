package com.example.freelancerconnect.models

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "",
    val skills: List<String> = emptyList(),
    val bio: String = "",
    val photoUrl: String = "",
    val companyName: String = "",
    val companyDescription: String = "",
    val industry: String = "",
    val phoneNumber: String = "",
    val contactInformation: String = "",
    val experience: String = "",
    val education: String = "",
    val resumeUrl: String = "",
    val portfolioUrl: String = "",
    val githubUrl: String = "",
    val linkedinUrl: String = ""
)

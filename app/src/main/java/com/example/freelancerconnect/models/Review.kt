package com.example.freelancerconnect.models

data class Review(
    val id: String = "",
    val reviewerId: String = "",
    val reviewedUserId: String = "",
    val rating: Float = 0f,
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

package com.example.freelancerconnect.models

data class Notification(
    val id: String = "",
    val userId: String = "",
    val type: String = "",
    val message: String = "",
    val read: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

package com.example.freelancerconnect.api

import com.google.gson.annotations.SerializedName

// ── Auth ──────────────────────────────────────────────────────────────────────

data class LoginRequest(
    val email: String,
    val password: String
)

data class GoogleAuthRequest(
    val idToken: String,
    val role: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String = ""
)

data class ResetPasswordRequest(
    val email: String
)

/**
 * Auth response user — backend returns "id" (not "_id") for login/register
 */
data class AuthUserResponse(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = ""
)

data class AuthResponse(
    val token: String,
    val user: AuthUserResponse
)

// ── User ──────────────────────────────────────────────────────────────────────

data class ApiUser(
    @SerializedName("_id")   val id: String = "",
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

data class UpdateUserRequest(
    val name: String? = null,
    val role: String? = null,
    val skills: List<String>? = null,
    val phoneNumber: String? = null,
    val bio: String? = null,
    val contactInformation: String? = null,
    val experience: String? = null,
    val education: String? = null,
    val companyName: String? = null,
    val resumeUrl: String? = null,
    val portfolioUrl: String? = null,
    val githubUrl: String? = null,
    val linkedinUrl: String? = null
)

data class PushTokenRequest(
    val token: String
)

// ── Job ───────────────────────────────────────────────────────────────────────

data class ApiJob(
    @SerializedName("_id")   val id: String = "",
    val title: String = "",
    val description: String = "",
    val budget: String = "",
    val skillsRequired: List<String> = emptyList(),
    val duration: String = "",
    val deadline: String = "",
    val clientId: ApiUser? = null,
    val createdAt: String = ""
)

data class CreateJobRequest(
    val title: String,
    val description: String,
    val budget: String,
    val skillsRequired: List<String>,
    val duration: String,
    val deadline: String
)

// ── Application ───────────────────────────────────────────────────────────────

data class ApiApplication(
    @SerializedName("_id")   val id: String = "",
    val jobId: ApiJob? = null,
    val freelancerId: ApiUser? = null,
    val clientId: String = "",
    val coverLetter: String = "",
    val status: String = "Pending",
    val createdAt: String = ""
)

data class CreateApplicationRequest(
    val jobId: String,
    val clientId: String,
    val coverLetter: String
)

data class UpdateStatusRequest(
    val status: String
)

// ── Message ───────────────────────────────────────────────────────────────────

data class ApiMessage(
    @SerializedName("_id")   val id: String = "",
    val conversationId: String = "",
    val senderId: ApiUser? = null,
    val receiverId: ApiUser? = null,
    val text: String = "",
    val createdAt: String = ""
)

data class SendMessageRequest(
    val conversationId: String,
    val receiverId: String,
    val text: String
)

// ── Notification ──────────────────────────────────────────────────────────────

data class ApiNotification(
    @SerializedName("_id")   val id: String = "",
    val userId: String = "",
    val type: String = "",
    val message: String = "",
    val relatedType: String = "",
    val relatedId: String = "",
    val conversationId: String = "",
    val peerId: String = "",
    val peerName: String = "",
    val read: Boolean = false,
    val createdAt: String = ""
)

// ── Generic ───────────────────────────────────────────────────────────────────

data class MessageResponse(
    val message: String
)

data class ApiError(
    val error: String
)

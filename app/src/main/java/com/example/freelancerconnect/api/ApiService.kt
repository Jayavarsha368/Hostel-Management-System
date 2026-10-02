package com.example.freelancerconnect.api

import retrofit2.Response
import retrofit2.http.*
import okhttp3.RequestBody
import okhttp3.ResponseBody

/**
 * Retrofit interface defining all FreelancerConnect API endpoints.
 *
 * Requests use localhost:3000, forwarded to the development machine by ADB.
 */
interface ApiService {

    // ── Auth ─────────────────────────────────────────────────────────────────

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @POST("auth/google")
    suspend fun googleSignIn(
        @Body request: GoogleAuthRequest
    ): Response<AuthResponse>

    @POST("auth/reset-password")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<MessageResponse>

    // ── Users ─────────────────────────────────────────────────────────────────

    @GET("users/me")
    suspend fun getMe(
        @Header("Authorization") token: String
    ): Response<ApiUser>

    @POST("users/push-token")
    suspend fun registerPushToken(
        @Header("Authorization") token: String,
        @Body request: PushTokenRequest
    ): Response<MessageResponse>

    @HTTP(method = "DELETE", path = "users/push-token", hasBody = true)
    suspend fun unregisterPushToken(
        @Header("Authorization") token: String,
        @Body request: PushTokenRequest
    ): Response<MessageResponse>

    @GET("users/freelancers")
    suspend fun getFreelancers(
        @Header("Authorization") token: String
    ): Response<List<ApiUser>>

    @PUT("users/me/files/{kind}")
    suspend fun uploadProfileFile(
        @Header("Authorization") token: String,
        @Path("kind") kind: String,
        @Header("X-File-Extension") extension: String,
        @Body file: RequestBody
    ): Response<MessageResponse>

    @GET("users/{userId}/files/{kind}")
    suspend fun getProfileFile(
        @Header("Authorization") token: String,
        @Path("userId") userId: String,
        @Path("kind") kind: String
    ): Response<ResponseBody>

    @GET("users/{id}")
    suspend fun getUserById(
        @Header("Authorization") token: String,
        @Path("id") userId: String
    ): Response<ApiUser>

    @PUT("users/{id}")
    suspend fun updateUser(
        @Header("Authorization") token: String,
        @Path("id") userId: String,
        @Body request: UpdateUserRequest
    ): Response<ApiUser>

    // ── Jobs ─────────────────────────────────────────────────────────────────

    @GET("jobs")
    suspend fun getJobs(
        @Header("Authorization") token: String
    ): Response<List<ApiJob>>

    @GET("jobs/{id}")
    suspend fun getJobById(
        @Header("Authorization") token: String,
        @Path("id") jobId: String
    ): Response<ApiJob>

    @POST("jobs")
    suspend fun createJob(
        @Header("Authorization") token: String,
        @Body request: CreateJobRequest
    ): Response<ApiJob>

    @DELETE("jobs/{id}")
    suspend fun deleteJob(
        @Header("Authorization") token: String,
        @Path("id") jobId: String
    ): Response<MessageResponse>

    // ── Applications ──────────────────────────────────────────────────────────

    @GET("applications")
    suspend fun getApplications(
        @Header("Authorization") token: String
    ): Response<List<ApiApplication>>

    @POST("applications")
    suspend fun createApplication(
        @Header("Authorization") token: String,
        @Body request: CreateApplicationRequest
    ): Response<ApiApplication>

    @DELETE("applications/{id}")
    suspend fun withdrawApplication(
        @Header("Authorization") token: String,
        @Path("id") applicationId: String
    ): Response<MessageResponse>

    @PUT("applications/{id}/status")
    suspend fun updateApplicationStatus(
        @Header("Authorization") token: String,
        @Path("id") applicationId: String,
        @Body request: UpdateStatusRequest
    ): Response<MessageResponse>

    // ── Messages ──────────────────────────────────────────────────────────────

    @GET("messages/{conversationId}")
    suspend fun getMessages(
        @Header("Authorization") token: String,
        @Path("conversationId") conversationId: String
    ): Response<List<ApiMessage>>

    @POST("messages")
    suspend fun sendMessage(
        @Header("Authorization") token: String,
        @Body request: SendMessageRequest
    ): Response<ApiMessage>

    @GET("messages/conversations")
    suspend fun getConversations(
        @Header("Authorization") token: String
    ): Response<List<ApiMessage>>

    // ── Notifications ─────────────────────────────────────────────────────────

    @GET("notifications")
    suspend fun getNotifications(
        @Header("Authorization") token: String
    ): Response<List<ApiNotification>>

    @PUT("notifications/{id}/read")
    suspend fun markNotificationRead(
        @Header("Authorization") token: String,
        @Path("id") notificationId: String
    ): Response<ApiNotification>

    @PUT("notifications/mark-all-read")
    suspend fun markAllNotificationsRead(
        @Header("Authorization") token: String
    ): Response<MessageResponse>
}

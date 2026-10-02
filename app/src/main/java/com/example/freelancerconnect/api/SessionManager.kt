package com.example.freelancerconnect.api

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages the user session — JWT token and basic user info — using SharedPreferences.
 *
 * This replaces Firebase Auth's session management.
 * The JWT token is sent as a Bearer token with every authenticated API request.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME   = "freelancerconnect_session"
        private const val KEY_TOKEN    = "jwt_token"
        private const val KEY_USER_ID  = "user_id"
        private const val KEY_EMAIL    = "user_email"
        private const val KEY_ROLE     = "user_role"
        private const val KEY_NAME     = "user_name"
        private const val KEY_SKILLS   = "user_skills"
    }

    // ── Session State ─────────────────────────────────────────────────────────

    val isLoggedIn: Boolean
        get() = prefs.getString(KEY_TOKEN, null) != null

    val bearerToken: String
        get() = "Bearer ${prefs.getString(KEY_TOKEN, "")}"

    val userId: String
        get() = prefs.getString(KEY_USER_ID, "") ?: ""

    val userEmail: String
        get() = prefs.getString(KEY_EMAIL, "") ?: ""

    val userRole: String
        get() = prefs.getString(KEY_ROLE, "") ?: ""

    val userName: String
        get() = prefs.getString(KEY_NAME, "") ?: ""

    val userSkills: List<String>
        get() = prefs.getString(KEY_SKILLS, "")
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()

    // ── Session Lifecycle ──────────────────────────────────────────────────────

    /** Save session after a successful login or register response. */
    fun saveSession(authResponse: AuthResponse) {
        prefs.edit()
            .putString(KEY_TOKEN,   authResponse.token)
            .putString(KEY_USER_ID, authResponse.user.id)
            .putString(KEY_EMAIL,   authResponse.user.email)
            .putString(KEY_ROLE,    authResponse.user.role)
            .putString(KEY_NAME,    authResponse.user.name)
            .apply()
    }

    /** Update cached user name and role after a profile save. */
    fun updateUser(name: String? = null, role: String? = null, skills: List<String>? = null) {
        prefs.edit().apply {
            name?.let { putString(KEY_NAME, it) }
            role?.let { putString(KEY_ROLE, it) }
            skills?.let { putString(KEY_SKILLS, it.joinToString(",")) }
            apply()
        }
    }

    /** Clear session on logout. */
    fun clearSession() {
        prefs.edit().clear().apply()
    }
}

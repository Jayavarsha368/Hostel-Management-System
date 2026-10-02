package com.example.freelancerconnect.api

import org.json.JSONObject
import retrofit2.Response

/**
 * Sealed class representing every possible API call outcome.
 * Use this in ViewModels to drive UI state.
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

/**
 * Extension function to safely execute a Retrofit call and wrap it in [ApiResult].
 * Handles network errors, HTTP errors, and successful responses uniformly.
 * Parses backend error JSON like { "error": "..." } or { "message": "..." }.
 */
suspend fun <T> safeApiCall(call: suspend () -> Response<T>): ApiResult<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                ApiResult.Success(body)
            } else {
                ApiResult.Error("Server returned an empty response.")
            }
        } else {
            val errorBodyStr = response.errorBody()?.string()
            // Try to parse the JSON error message from { "error": "..." }
            val errorMessage = try {
                if (!errorBodyStr.isNullOrBlank()) {
                    val json = JSONObject(errorBodyStr)
                    json.optString("error")
                        .ifBlank { json.optString("message") }
                        .ifBlank { "Request failed (${response.code()})" }
                } else {
                    "Request failed with code ${response.code()}"
                }
            } catch (_: Exception) {
                errorBodyStr ?: "Request failed with code ${response.code()}"
            }
            ApiResult.Error(errorMessage)
        }
    } catch (e: Exception) {
        val msg = e.message ?: "Unknown error"
        // Make connection errors friendlier
        val friendly = when {
            msg.contains("connect", ignoreCase = true) ||
            msg.contains("timeout", ignoreCase = true) ||
            msg.contains("refused", ignoreCase = true) ->
                "Could not connect to the server. Make sure it's running and you're on the same network."
            msg.contains("Unable to resolve host", ignoreCase = true) ->
                "Network error — cannot reach the server."
            else -> msg
        }
        ApiResult.Error(friendly)
    }
}

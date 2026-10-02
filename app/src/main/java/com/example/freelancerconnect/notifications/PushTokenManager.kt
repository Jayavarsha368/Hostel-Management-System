package com.example.freelancerconnect.notifications

import android.content.Context
import android.util.Log
import com.example.freelancerconnect.api.ApiClient
import com.example.freelancerconnect.api.PushTokenRequest
import com.example.freelancerconnect.api.SessionManager
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object PushTokenManager {

    fun register(context: Context) {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token -> registerToken(context, token) }
            .addOnFailureListener { error ->
                Log.w(TAG, "Could not get push token", error)
            }
    }

    fun registerToken(context: Context, token: String) {
        val session = SessionManager(context.applicationContext)
        if (!session.isLoggedIn || token.isBlank()) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.api.registerPushToken(
                    session.bearerToken,
                    PushTokenRequest(token)
                )
                if (!response.isSuccessful) {
                    Log.w(TAG, "Push token registration failed with HTTP ${response.code()}")
                }
            } catch (error: Exception) {
                Log.w(TAG, "Could not register push token", error)
            }
        }
    }

    fun unregister(context: Context, authorization: String) {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        ApiClient.api.unregisterPushToken(
                            authorization,
                            PushTokenRequest(token)
                        )
                    } catch (error: Exception) {
                        Log.w(TAG, "Could not unregister push token", error)
                    }
                }
            }
    }

    private const val TAG = "PushTokenManager"
}

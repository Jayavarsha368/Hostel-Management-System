package com.example.freelancerconnect.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.freelancerconnect.R
import com.example.freelancerconnect.activities.AppliedJobsActivity
import com.example.freelancerconnect.activities.ChatActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessageFirebaseService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        PushTokenManager.registerToken(applicationContext, token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val type = data["type"].orEmpty()
        if (type != TYPE_MESSAGE && type != TYPE_APPLICATION) return

        createNotificationChannel()
        val title = data["title"].orEmpty().ifBlank {
            if (type == TYPE_MESSAGE) "New message" else "New application"
        }
        val body = data["body"].orEmpty()
        val destination = when (type) {
            TYPE_MESSAGE -> Intent(this, ChatActivity::class.java).apply {
                putExtra(ChatActivity.EXTRA_PEER_ID, data["peerId"].orEmpty())
                putExtra(ChatActivity.EXTRA_PEER_NAME, data["peerName"].orEmpty())
                putExtra(ChatActivity.EXTRA_CONVERSATION_ID, data["conversationId"].orEmpty())
            }
            else -> Intent(this, AppliedJobsActivity::class.java).apply {
                putExtra(AppliedJobsActivity.EXTRA_APPLICATION_ID, data["applicationId"].orEmpty())
            }
        }.apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val notificationKey = data["messageId"] ?: data["applicationId"] ?: body
        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationKey.hashCode(),
            destination,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()

        try {
            NotificationManagerCompat.from(this).notify(notificationKey.hashCode(), notification)
        } catch (_: SecurityException) {
            // Android 13+ requires notification permission from the user.
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Messages and applications",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "New messages and job applications"
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "freelancer_connect_updates"
        private const val TYPE_MESSAGE = "message"
        private const val TYPE_APPLICATION = "application"
    }
}

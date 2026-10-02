package com.example.freelancerconnect.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.freelancerconnect.api.SessionManager

/**
 * Base class for all activities.
 * Provides common navigation helpers and access to the session manager.
 */
abstract class BaseActivity : AppCompatActivity() {

    /** Shared session manager backed by SharedPreferences (JWT token + user info). */
    protected val session: SessionManager by lazy { SessionManager(applicationContext) }

    /** Navigate to another activity. */
    protected fun navigateTo(target: Class<*>, finishCurrent: Boolean = false) {
        startActivity(Intent(this, target))
        if (finishCurrent) finish()
    }

    /** Navigate to another activity and clear the entire back stack. */
    protected fun navigateAndClearStack(target: Class<*>) {
        val intent = Intent(this, target).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
package com.example.freelancerconnect.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.freelancerconnect.R

/**
 * Entry point of the app.
 * Checks JWT session state and routes to Login or Dashboard accordingly.
 * Replaces Firebase Auth's currentUser check.
 */
class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            if (session.isLoggedIn) {
                navigateAndClearStack(DashboardActivity::class.java)
            } else {
                navigateAndClearStack(LoginActivity::class.java)
            }
        }, 700)
    }
}
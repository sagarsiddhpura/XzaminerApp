package com.xzaminer.app

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import com.simplemobiletools.commons.activities.BaseSplashActivity
import com.simplemobiletools.commons.extensions.appLaunched
import com.simplemobiletools.commons.extensions.baseConfig
import com.xzaminer.app.category.MainActivity
import com.xzaminer.app.extensions.config
import com.xzaminer.app.extensions.dataSource
import com.xzaminer.app.login.EmailPasswordActivity
import com.xzaminer.app.utils.logEvent

class SplashActivity : BaseSplashActivity() {
    private var mIsPasswordProtectionPending = false

    override fun initActivity() {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logEvent("ActivitySplash")
        if (baseConfig.appRunCount == 0) {
            baseConfig.backgroundColor = ContextCompat.getColor(this, R.color.background_grey)
            baseConfig.textColor = ContextCompat.getColor(this, R.color.text_black)
            baseConfig.primaryColor = ContextCompat.getColor(this, R.color.text_black)
            config.enablePullToRefresh = false
        }
        baseConfig.primaryColor = ContextCompat.getColor(this, R.color.md_grey_black_dark)
        appLaunched(BuildConfig.APPLICATION_ID)
        navigate()
    }

    private fun navigate() {
//        Log.d("Xz_", "Splash:"+System.nanoTime())
        val currentUser = config.getLoggedInUser()
        if(currentUser == null) {
            startActivity(Intent(this, EmailPasswordActivity::class.java))
            finish()
            return
        } else {
            if(currentUser.phone != null) {
                if(!config.isOtpVerified) {
                    dataSource.logout()
                    config.setLoggedInUser(null)
                    config.isOtpVerified = false
                } else {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    return
                }
            }
        }
        startActivity(Intent(this, EmailPasswordActivity::class.java))
        finish()
    }
}

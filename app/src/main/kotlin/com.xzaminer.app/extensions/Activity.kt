package com.xzaminer.app.extensions

import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.utils.logEvent

fun SimpleActivity.launchAbout() {
    logEvent("launchAbout")
//    Intent(applicationContext, AboutActivity::class.java).apply {
//        startActivity(this)
//    }
}
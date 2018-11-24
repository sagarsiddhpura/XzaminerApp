package com.xzaminer.app

import android.support.multidex.MultiDexApplication
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import com.github.ajalt.reprint.core.Reprint
import com.simplemobiletools.commons.extensions.checkUseEnglish
import com.xzaminer.app.utils.logEvent
import io.fabric.sdk.android.Fabric

class App : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        checkUseEnglish()
        Reprint.initialize(this)
        Fabric.with(this, Crashlytics())
        Fabric.with(this, Answers())
        logEvent("VersionCode" + BuildConfig.VERSION_CODE)
    }
}

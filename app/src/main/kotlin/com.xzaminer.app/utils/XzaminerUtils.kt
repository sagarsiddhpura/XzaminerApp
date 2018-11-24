package com.xzaminer.app.utils

import com.xzaminer.app.studymaterial.StudyMaterial
import java.text.SimpleDateFormat
import java.util.*

fun getProductName(id: String): String {
    if(id == IAP_SUB_YEARLY) {
        return "Yearly Subscription"
    }
    if(id == IAP_SUB_MONTHLY) {
        return "Monthly Subscription"
    }
    return QUESTION_BANK_NAME
}

fun getProductType(id: String): String {
    if(id == IAP_SUB_YEARLY) {
        return PURCHASE_TYPE_SUBSCRIPTION
    }
    if(id == IAP_SUB_MONTHLY) {
        return PURCHASE_TYPE_SUBSCRIPTION
    }
    return PURCHASE_TYPE_IAP
}

fun getExpiry(id: String): String? {
    if(id == IAP_SUB_YEARLY) {
        val currentDate = Date()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val cal = Calendar.getInstance()
        cal.time = currentDate
        cal.add(Calendar.YEAR, 1)
        return sdf.format(cal.time)
    }
    if(id == IAP_SUB_MONTHLY) {
        val currentDate = Date()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val cal = Calendar.getInstance()
        cal.time = currentDate
        cal.add(Calendar.MONTH, 1)
        return sdf.format(cal.time)
    }
    return null
}

fun getNowDate(): String? {
    val currentDate = Date()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    val cal = Calendar.getInstance()
    cal.time = currentDate
    return sdf.format(cal.time)
}

fun isAvailableForSubscription(questionBank: StudyMaterial): Boolean {
    var isAvailableForSubscription = false
    questionBank.purchaseInfo.forEach {
        if(it.type == PURCHASE_TYPE_SUBSCRIPTION) {
            isAvailableForSubscription = true
        }
    }
    return isAvailableForSubscription
}

fun isAvailableForPurchase(questionBank: StudyMaterial): Boolean {
    var isAvailableForPurchase = false
    questionBank.purchaseInfo.forEach {
        if(it.type == PURCHASE_TYPE_IAP) {
            isAvailableForPurchase = true
        }
    }
    return isAvailableForPurchase
}

fun convertToText(totalSecs: Long): String {
    var secsString : String = ""
    try {
        var mins = ((totalSecs/60).toString())
        if(mins.length < 2) { mins = "0$mins" }
        var secs = ((totalSecs%60).toString())
        if(secs.length < 2) { secs = "0$secs" }
        secsString = "$mins:$secs"
    } catch (ex : Exception) { }
    return secsString
}
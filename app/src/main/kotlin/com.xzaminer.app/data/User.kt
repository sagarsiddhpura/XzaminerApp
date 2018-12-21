package com.xzaminer.app.data

import com.xzaminer.app.billing.Purchase
import com.xzaminer.app.course.Course
import com.xzaminer.app.course.CourseSection
import com.xzaminer.app.studymaterial.StudyMaterial
import com.xzaminer.app.utils.QB_STATUS_IN_PROGRESS
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by SIDD on 24-Sep-17.
 */
data class User(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    var token: String = "",
    val userType: String? = null,
    var phone: String? = null,
    var purchases: ArrayList<Purchase> = arrayListOf(),
    val lastLoggedIn: String = "",
    val status: String = "enabled",
    val quizzes: ArrayList<StudyMaterial> = arrayListOf()) {
    fun getId(): String {
        return replace(email) + replace(phone?: "")
    }

    fun hasPurchase(productId: String): Boolean {
        purchases.forEach {
            if(it != null && it.id == productId) {
                return true
            }
        }
        return false
    }

    fun checkPurchase(questionBank: StudyMaterial): Boolean {
        questionBank.purchaseInfo.forEach { audioBookPurchase ->
            if(purchases.any { it != null && it.id == audioBookPurchase.id }) {
                return true
            }
        }
        return false
    }

    private fun isExpired(expiry: String?): Boolean {
        if(expiry == null) {
            return false
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val expDate = sdf.parse(expiry).time
        val nowDate = Calendar.getInstance().time.time
        return expDate < nowDate
    }

    fun removePurchase(purchaseId: String) {
        purchases = purchases.filter {  it -> it == null || it.id != purchaseId } as ArrayList<Purchase>
    }

    fun startQuiz(questionBank: StudyMaterial) {
        questionBank.status = QB_STATUS_IN_PROGRESS
        quizzes.add(questionBank)
    }

    fun isStudyMaterialPurchased(course: Course, section: CourseSection, studyMaterial: StudyMaterial): Boolean {

        if(!studyMaterial.fetchVisiblePurchases().isEmpty() && hasPurchase(studyMaterial.fetchVisiblePurchases().first().id)) {
            return true
        }
        if(!section.fetchVisiblePurchases().isEmpty() && hasPurchase(section.fetchVisiblePurchases().first().id)) {
            return true
        }
        if(!course.fetchVisiblePurchases().isEmpty() && hasPurchase(course.fetchVisiblePurchases().first().id)) {
            return true
        }

        return false
    }

    companion object {
        fun replace(string: String): String {
            return string.replace("@", "").replace("/","").replace(".","").replace("#","")
                    .replace("$","").replace("[","").replace("]","")
        }
    }
}

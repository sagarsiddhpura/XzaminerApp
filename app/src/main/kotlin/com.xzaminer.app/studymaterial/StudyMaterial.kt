package com.xzaminer.app.studymaterial

import com.xzaminer.app.billing.Purchase
import com.xzaminer.app.utils.*

data class StudyMaterial (
    var id: Long = 0,
    var name: String? = null,
    var description: String? = null,
    var imageIcon: String? = null,
    var openCount: Int = 0,
    var properties : HashMap<String, ArrayList<String>> = hashMapOf(),
    var questions: ArrayList<Question> = arrayListOf(),
    var videos: ArrayList<Video> = arrayListOf(),
    val purchaseInfo: ArrayList<Purchase> = arrayListOf(),
    var status: String = "",
    var type: String = STUDY_MATERIAL_TYPE_STUDY_MATERIAL
    ) {
    fun fetchIapPurchase(): Purchase? {
        purchaseInfo.forEach {
            if(it.type == PURCHASE_TYPE_IAP) {
                return it
            }
        }
        return null
    }

    fun fetchCorrectPoints(): Int {
        var correct = 0
        questions.forEach {question ->
            if(question.correctAnswer == question.selectedAnswer) {
                correct++
            }
        }
        return correct
    }

    fun fetchInCorrectPoints(): Int {
        var incorrect = 0
        questions.forEach {question ->
            if(question.correctAnswer != question.selectedAnswer && question.selectedAnswer != 0L) {
                incorrect++
            }
        }
        return incorrect
    }

    fun fetchNotAttemptedPoints(): Int {
        var notAttempted = 0
        questions.forEach {question ->
            if(question.selectedAnswer == 0L) {
                notAttempted++
            }
        }
        return notAttempted
    }

    fun fetchResult(): Int? {
        if(questions.isEmpty()) {
            return 0
        }
        return (fetchCorrectPoints()*100)/questions.size
    }

    private fun fetchTotalTimer(): Long? {
        if(properties[TIMER_TOTAL_TIME] != null) {
            var secs = 0L
            try {
                val lengthString = properties[TIMER_TOTAL_TIME]?.first()
                val split = lengthString!!.split(":")
                secs = split[0].toLong() * 60
                secs += split[1].toInt()
            } catch (ex : Exception) { return null }
            return secs
        }
        return null
    }

    fun saveTimer(runningTimer: Long) {
        properties[TIMER_RUNNING_TIME] = arrayListOf(convertToText(runningTimer))
    }

    fun fetchTotalOrResumeTimer(): Long? {
        if(fetchResumeTimer() != null) { return fetchResumeTimer() } else { return fetchTotalTimer() }
    }

    fun fetchResumeTimer(): Long? {
        if(properties[TIMER_RUNNING_TIME] != null) {
            var secs = 0L
            try {
                val lengthString = properties[TIMER_RUNNING_TIME]?.first()
                val split = lengthString!!.split(":")
                secs = split[0].toLong() * 60
                secs += split[1].toInt()
            } catch (ex : Exception) { return null }
            return secs
        }
        return null
    }

    fun addTotalTimer(timer: String) {
        properties[TIMER_RUNNING_TIME] = arrayListOf(timer)
    }

    fun fetchVideo(videoId: Long): Video? {
        return videos.find { it -> it.id == videoId }
    }

    fun fetchVisiblePurchases(): List<Purchase> {
        if(!purchaseInfo.isEmpty()) {
            return purchaseInfo.filter { it.showPurchase }
        }
        return arrayListOf()
    }
}

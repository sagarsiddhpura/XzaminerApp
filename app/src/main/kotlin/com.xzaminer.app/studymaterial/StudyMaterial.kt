package com.xzaminer.app.studymaterial

import com.xzaminer.app.billing.Purchase
import com.xzaminer.app.utils.*

data class StudyMaterial (
    var id: Long = 0,
    var name: String? = null,
    var desc: String? = null,
    var image: String? = null,
    var openCount: Int = 0,
    var properties : HashMap<String, ArrayList<String>> = hashMapOf(),
    var questions: ArrayList<Question> = arrayListOf(),
    var videos: ArrayList<Video> = arrayListOf(),
    val purchaseInfo: ArrayList<Purchase> = arrayListOf(),
    var status: String = "",
    var type: String = STUDY_MATERIAL_TYPE_STUDY_MATERIAL,
    var order: Int = 0,
    var isVisible: Boolean = true
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

    fun fetchResult(): Int {
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

    fun updateLastAccessed() {
        properties[QB_LAST_ACCESSED] = arrayListOf(getNowDateTime())
    }

    fun startQuiz() {
        status = QB_STATUS_IN_PROGRESS
        properties[QB_STARTED_ON] = arrayListOf(getNowDateTime())
    }

    fun updateCompleted() {
        properties[QB_COMPLETED] = arrayListOf(getNowDateTime())
    }

    fun fetchVideoIndex(videoId: Long): Int {
        fetchVisibleVideos().forEachIndexed { index, video ->
            if(video.id == videoId) {
                return index
            }
        }
        return 0
    }

    fun fetchVisibleVideos(): ArrayList<Video> {
        val arrayList =
            ArrayList(videos.filter { it != null && it.isVisible })
        if(!arrayList.isEmpty()) {arrayList.sortWith (compareBy { it.order })}
        return arrayList
    }
}

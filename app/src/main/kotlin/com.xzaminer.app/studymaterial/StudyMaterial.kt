package com.xzaminer.app.studymaterial

import com.xzaminer.app.billing.Purchase
import com.xzaminer.app.quiz.Question
import com.xzaminer.app.utils.PURCHASE_TYPE_IAP
import com.xzaminer.app.utils.TIMER_RUNNING_TIME
import com.xzaminer.app.utils.TIMER_TOTAL_TIME
import com.xzaminer.app.utils.convertToText

data class StudyMaterial (
    var id: Long = 0,
    var name: String? = null,
    var description: String? = null,
    var imageIcon: String? = null,
    var openCount: Int = 0,
    var properties : HashMap<String, ArrayList<String>> = hashMapOf(),
    var questions: ArrayList<Question> = arrayListOf(),
    val purchaseInfo: ArrayList<Purchase> = arrayListOf(),
    var status: String = ""
    ) {
    fun getIapPurchase(): Purchase? {
        purchaseInfo.forEach {
            if(it.type == PURCHASE_TYPE_IAP) {
                return it
            }
        }
        return null
    }

    fun getCorrectPoints(): Int {
        var correct = 0
        questions.forEach {question ->
            if(question.correctAnswer == question.selectedAnswer) {
                correct++
            }
        }
        return correct
    }

    fun getInCorrectPoints(): Int {
        var incorrect = 0
        questions.forEach {question ->
            if(question.correctAnswer != question.selectedAnswer && question.selectedAnswer != 0L) {
                incorrect++
            }
        }
        return incorrect
    }

    fun getNotAttemptedPoints(): Int {
        var notAttempted = 0
        questions.forEach {question ->
            if(question.selectedAnswer == 0L) {
                notAttempted++
            }
        }
        return notAttempted
    }

    fun getResult(): Int? {
        if(questions.isEmpty()) {
            return 0
        }
        return (getCorrectPoints()*100)/questions.size
    }

    private fun getTotalTimer(): Long? {
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

    fun getTotalOrResumeTimer(): Long? {
        if(getResumeTimer() != null) { return getResumeTimer() } else { return getTotalTimer() }
    }

    fun getResumeTimer(): Long? {
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
}

package com.xzaminer.app.quiz

import com.xzaminer.app.billing.Purchase
import com.xzaminer.app.utils.PURCHASE_TYPE_IAP

data class QuestionBank (
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
        return (getCorrectPoints()*100)/questions.size
    }
}

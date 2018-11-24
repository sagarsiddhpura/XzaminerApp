package com.xzaminer.app.course

import com.xzaminer.app.billing.Purchase
import com.xzaminer.app.quiz.QuestionBank
import com.xzaminer.app.studymaterial.StudyMaterial

data class Course (
    var id: Long = 0,
    var name: String? = null,
    var description: String? = null,
    var imageIcon: String? = null,
    var concepts: HashMap<String, StudyMaterial> = hashMapOf(),
    var questionBanks: HashMap<String, QuestionBank> = hashMapOf(),
    var reviewManuals: HashMap<String, StudyMaterial> = hashMapOf(),
    var flashCards: HashMap<String, StudyMaterial> = hashMapOf(),
    val purchaseInfo: ArrayList<Purchase> = arrayListOf()
    ) {
        override fun toString(): String {
            return "$name:::$description"
        }

    fun getConceptById(studyMaterialId: Long): StudyMaterial? {
        if(!concepts.isEmpty()) {
            return concepts.values.find { it -> (it != null && it.id == studyMaterialId) }
        }
        return null
    }

    fun getReviewManualsById(studyMaterialId: Long): @ParameterName(name = "course") StudyMaterial? {
        if(!reviewManuals.isEmpty()) {
            return reviewManuals.values.find { it -> (it != null && it.id == studyMaterialId) }
        }
        return null
    }

    fun getFlashCardsById(studyMaterialId: Long): @ParameterName(name = "course") StudyMaterial? {
        if(!flashCards.isEmpty()) {
            return flashCards.values.find { it -> (it != null && it.id == studyMaterialId) }
        }
        return null
    }
}

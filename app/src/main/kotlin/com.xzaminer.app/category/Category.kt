package com.xzaminer.app.category

import com.xzaminer.app.quiz.QuestionBank

data class Category (
    var id: Long = 0,
    var name: String? = null,
    var description: String? = null,
    var image: String? = null,
    var subCategories: HashMap<String, Category>? = null,
    var questionBanks: HashMap<String, QuestionBank>? = null,
    var isAudioBook: Boolean = false)
    {
        override fun toString(): String {
            return "$name:::$description"
        }
}

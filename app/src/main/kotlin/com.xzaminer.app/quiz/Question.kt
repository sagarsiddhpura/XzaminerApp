package com.xzaminer.app.quiz

data class Question (
    var id: Long = 0,
    var text: String? = null,
    var description: String? = null,
    var options: ArrayList<QuestionOption> = arrayListOf(),
    var correctAnswer: Long? = null,
    var selectedAnswer: Long? = 0
    ) {

    // genre
}

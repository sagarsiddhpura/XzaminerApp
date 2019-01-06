package com.xzaminer.app.studymaterial

data class Question (
    var id: Long = 0,
    var text: String? = null,
    var desc: String? = null,
    var options: ArrayList<QuestionOption> = arrayListOf(),
    var correctAnswer: Long? = null,
    var selectedAnswer: Long? = 0,
    var isMarkedForLater: Boolean = false,
    var audios: ArrayList<Video> = arrayListOf()
    ) {
    fun isCorrect(): Boolean {
        if(selectedAnswer == correctAnswer) {
            return true
        }
        return false
    }

    fun isIncorrect(): Boolean {
        if(selectedAnswer != 0L && selectedAnswer != correctAnswer) {
            return true
        }
        return false
    }

    fun isNotAttempeted(): Boolean {
        if(selectedAnswer == 0L) {
            return true
        }
        return false
    }

    // genre
}

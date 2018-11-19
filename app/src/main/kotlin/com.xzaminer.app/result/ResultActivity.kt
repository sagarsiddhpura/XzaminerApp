package com.xzaminer.app.result

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.Window
import com.simplemobiletools.commons.extensions.toast
import com.xzaminer.app.R
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.data.User
import com.xzaminer.app.extensions.config
import com.xzaminer.app.extensions.dataSource
import com.xzaminer.app.utils.CAT_ID
import com.xzaminer.app.utils.QUIZ_ID
import kotlinx.android.synthetic.main.activity_result.*

class ResultActivity : SimpleActivity() {

    private var quizId: Long? = null
    private var toolbar: Toolbar? = null
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_ACTION_MODE_OVERLAY)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar?.setNavigationOnClickListener { onBackPressed() }
        title = "Result"

        intent.apply {
            quizId = getLongExtra(QUIZ_ID, -1)
            if(quizId == (-1).toLong()) {
                toast("Error Opening Result")
                finish()
            }
        }

        user = config.getLoggedInUser() as User

        dataSource.getQuestionBankFromUser(user.getId(), quizId) { loadedQuiz ->
            if (loadedQuiz != null) {
                result_message.text = "Congratulations! You have completed the quiz.\n\nYour Score is " + loadedQuiz.getResult() + "%"

                val correct = loadedQuiz.getCorrectPoints()
                val incorrect = loadedQuiz.getInCorrectPoints()
                val notAttempted = loadedQuiz.getNotAttemptedPoints()

                correct_text.text = "Correct:\n" + correct.toString() + "/" + loadedQuiz.questions.size.toString()
                incorrect_text.text = "Incorrect:\n" + incorrect.toString() + "/" + loadedQuiz.questions.size.toString()
                unattempted_text.text = "Not Attempted:\n" + notAttempted.toString() + "/" + loadedQuiz.questions.size.toString()

                see_answers.setOnClickListener {
                    Intent(this, AnswersActivity::class.java).apply {
                        putExtra(CAT_ID, quizId)
                        startActivity(this)
                    }
                }
            } else {
                toast("Error Opening Result")
                finish()
            }
        }
    }
}

package com.xzaminer.app.result

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.Toolbar
import android.view.Window
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.views.MyGridLayoutManager
import com.xzaminer.app.R
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.data.User
import com.xzaminer.app.extensions.config
import com.xzaminer.app.extensions.dataSource
import com.xzaminer.app.quiz.Question
import com.xzaminer.app.studymaterial.StudyMaterial
import com.xzaminer.app.utils.CAT_ID
import com.xzaminer.app.utils.QB_STATUS_FINISHED
import kotlinx.android.synthetic.main.activity_quiz.*



class QuizAnswersActivity : SimpleActivity() {

    private var quizId: Long? = null
    private var toolbar: Toolbar? = null
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_ACTION_MODE_OVERLAY)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar?.setNavigationOnClickListener { onBackPressed() }

        intent.apply {
            quizId = getLongExtra(CAT_ID, -1)
            if(quizId == (-1).toLong()) {
                toast("Error Showing Answers")
                finish()
                return
            }
        }
        user = config.getLoggedInUser() as User
        setupGridLayoutManager()

        dataSource.getQuestionBankFromUser(user.getId(), quizId) { loadedQuiz ->
            if(loadedQuiz != null && loadedQuiz.status == QB_STATUS_FINISHED) {
                loadQuestionBank(loadedQuiz)
            } else {
                toast("Error Showing Answers")
                finish()
                return@getQuestionBankFromUser
            }
        }
    }

    private fun loadQuestionBank(loadedQuiz: StudyMaterial) {
        if (loadedQuiz.questions.size <= 0) {
            toast("Error Showing Answers. No Questions in this Question Bank")
            finish()
            return
        }

        supportActionBar?.title = loadedQuiz.name

        user.startQuiz(loadedQuiz)
        config.setLoggedInUser(user)
        dataSource.addUser(user)

        setupAdapter(loadedQuiz.questions)
    }

    private fun getRecyclerAdapter() = quiz_grid.adapter as? QuestionsAnswerAdapter

    private fun setupGridLayoutManager() {
        val layoutManager = quiz_grid.layoutManager as MyGridLayoutManager
        layoutManager.orientation = GridLayoutManager.VERTICAL
        layoutManager.spanCount = 1
    }

    private fun setupAdapter(questions: ArrayList<Question>) {
        val currAdapter = quiz_grid.adapter
        if (currAdapter == null) {
            QuestionsAnswerAdapter(this, questions.clone() as ArrayList<Question>, quiz_grid) {
            }.apply {
                quiz_grid.adapter = this
            }
        } else {
            (currAdapter as QuestionsAnswerAdapter).updateQuestions(questions)
        }
    }
}

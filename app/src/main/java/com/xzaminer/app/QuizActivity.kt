package com.xzaminer.app

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.Toolbar
import android.view.Window
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.views.MyGridLayoutManager
import com.xzaminer.app.category.CategoriesAdapter
import com.xzaminer.app.data.User
import com.xzaminer.app.extensions.config
import com.xzaminer.app.extensions.dataSource
import com.xzaminer.app.quiz.Question
import com.xzaminer.app.quiz.QuestionBank
import com.xzaminer.app.quiz.QuestionsAdapter
import com.xzaminer.app.utils.CAT_ID
import kotlinx.android.synthetic.main.activity_quiz.*

class QuizActivity : SimpleActivity() {

    private var abId: Long? = null
    private var toolbar: Toolbar? = null

    private var questionBank: QuestionBank? = null

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
            abId = getLongExtra(CAT_ID, -1)
            if(abId == (-1).toLong()) {
                toast("Error Opening AudioBook")
                finish()
            }
        }

        setupGridLayoutManager()
        dataSource.getQuestionBank(abId) { loadedQuestionBank ->
            if (loadedQuestionBank != null) {
                questionBank = loadedQuestionBank

                if (loadedQuestionBank.questions == null || loadedQuestionBank.questions!!.size <= 0) {
                    toast("Error Opening Question Bank. No Questions in this Question Bank")
                    finish()
                    return@getQuestionBank
                }
                supportActionBar?.title = loadedQuestionBank.name

                val user = config.getLoggedInUser() as User
                user.startQuiz(loadedQuestionBank)
                config.setLoggedInUser(user)
                dataSource.addUser(user)

                setupAdapter(loadedQuestionBank.questions!!)
            } else {
                toast("Error Opening Question Bank")
                finish()
            }
        }
    }

    private fun getRecyclerAdapter() = quiz_grid.adapter as? CategoriesAdapter

    private fun setupGridLayoutManager() {
        val layoutManager = quiz_grid.layoutManager as MyGridLayoutManager
        layoutManager.orientation = GridLayoutManager.VERTICAL
        layoutManager.spanCount = 1
    }

    private fun setupAdapter(questions: ArrayList<Question>) {
        val currAdapter = quiz_grid.adapter
        if (currAdapter == null) {
            QuestionsAdapter(this, questions.clone() as ArrayList<Question>, quiz_grid) {
            }.apply {
                quiz_grid.adapter = this
            }
        } else {
            (currAdapter as QuestionsAdapter).updateQuestions(questions)
        }
    }
}

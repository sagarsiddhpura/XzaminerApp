package com.xzaminer.app.quiz

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.views.MyGridLayoutManager
import com.xzaminer.app.R
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.data.User
import com.xzaminer.app.extensions.config
import com.xzaminer.app.extensions.dataSource
import com.xzaminer.app.result.ResultActivity
import com.xzaminer.app.utils.CAT_ID
import com.xzaminer.app.utils.QB_STATUS_FINISHED
import com.xzaminer.app.utils.QUIZ_ID
import kotlinx.android.synthetic.main.activity_quiz.*

class QuizActivity : SimpleActivity() {

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
                toast("Error Opening Question Bank")
                finish()
            }
        }
        user = config.getLoggedInUser() as User
        setupGridLayoutManager()

        dataSource.getQuestionBank(quizId) { loadedQuiz ->
            if (loadedQuiz != null) {
                if (loadedQuiz.questions.size <= 0) {
                    toast("Error Opening Question Bank. No Questions in this Question Bank")
                    finish()
                    return@getQuestionBank
                }
                supportActionBar?.title = loadedQuiz.name

                user.startQuiz(loadedQuiz)
                config.setLoggedInUser(user)
                dataSource.addUser(user)

                setupAdapter(loadedQuiz.questions)
            } else {
                toast("Error Opening Question Bank")
                finish()
            }
        }
    }

    private fun getRecyclerAdapter() = quiz_grid.adapter as? QuestionsAdapter

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

    fun optionClicked(question: Question, id: Long) {
        val questions = user.quizzes.find { it.id == quizId }!!.questions
        val questionQuiz = questions.find { it.id == question.id }
        if(questionQuiz!!.selectedAnswer == id) {
            questionQuiz.selectedAnswer = 0L
        } else {
            questionQuiz.selectedAnswer = id
        }
        dataSource.addUser(user)
        getRecyclerAdapter()?.updateQuestions(questions)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_quiz, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.finish_quiz -> finishQuiz()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun finishQuiz() {
        ConfirmDialog(this, "Are you sure you want to finish Quiz?") {
            user.quizzes.find { it.id == quizId }!!.status = QB_STATUS_FINISHED
            dataSource.addUser(user)
            Intent(this, ResultActivity::class.java).apply {
                putExtra(QUIZ_ID, quizId)
                startActivity(this)
            }
            finish()
        }
    }
}

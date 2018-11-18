package com.xzaminer.app.quiz

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import com.simplemobiletools.commons.extensions.beVisible
import com.simplemobiletools.commons.extensions.highlightTextPart
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.views.MyGridLayoutManager
import com.xzaminer.app.R
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.data.User
import com.xzaminer.app.extensions.config
import com.xzaminer.app.extensions.dataSource
import com.xzaminer.app.result.ResultActivity
import com.xzaminer.app.utils.*
import kotlinx.android.synthetic.main.activity_quiz.*



class QuizActivity : SimpleActivity() {

    private var quizId: Long? = null
    private var toolbar: Toolbar? = null
    private lateinit var user: User
    private var isMarkedForLaterQuestionsShown: Boolean = false
    private var isNewQuiz: Boolean = true
    private var runningTimer: Long = -1L
    private var timer : CountDownTimer? = null

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
            isNewQuiz = getBooleanExtra(IS_NEW_QUIZ, true)
            if(quizId == (-1).toLong()) {
                toast("Error Opening Question Bank")
                finish()
                return
            }
        }
        user = config.getLoggedInUser() as User
        setupGridLayoutManager()

        if(isNewQuiz) {
            dataSource.getQuestionBank(quizId) { loadedQuiz ->
                loadQuestionBank(loadedQuiz)
            }
        } else {
            dataSource.getQuestionBankFromUser(user.getId(), quizId) { loadedQuiz ->
                if(loadedQuiz != null && loadedQuiz.status == QB_STATUS_IN_PROGRESS) {
                    loadQuestionBank(loadedQuiz)
                } else {
                    toast("Error Resuming Quiz. Please start quiz first to resume it.")
                    finish()
                    return@getQuestionBankFromUser
                }
            }
        }
    }

    private fun loadQuestionBank(loadedQuiz: QuestionBank?) {
        if (loadedQuiz != null) {
            if (loadedQuiz.questions.size <= 0) {
                toast("Error Opening Question Bank. No Questions in this Question Bank")
                finish()
                return
            }

            supportActionBar?.title = loadedQuiz.name

            user.startQuiz(loadedQuiz)
            config.setLoggedInUser(user)
            dataSource.addUser(user)

            setupAdapter(loadedQuiz.questions)
            setupTimer(loadedQuiz.getTotalOrResumeTimer())
        } else {
            toast("Error Opening Question Bank")
            finish()
        }
    }

    private fun setupTimer(timerSecs: Long?) {
        if(timerSecs != null) {
            quiz_timer_label.text = convertToText(timerSecs)
            quiz_timer_label.beVisible()

            timer = object : CountDownTimer(timerSecs*1000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val timeText = convertToText(millisUntilFinished / 1000)
                    if(millisUntilFinished < 120000) { quiz_timer_label.text = timeText.highlightTextPart(timeText, resources.getColor(R.color.md_red)) } else { quiz_timer_label.text = timeText }
                    runningTimer = millisUntilFinished / 1000
                }
                override fun onFinish() {
                    finishQuiz()
                }
            }
            (timer as CountDownTimer).start()
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
        val questions = getCurrentQuizQuestionsFromUser()
        val questionQuiz = questions.find { it.id == question.id }
        if(questionQuiz!!.selectedAnswer == id) {
            questionQuiz.selectedAnswer = 0L
        } else {
            questionQuiz.selectedAnswer = id
        }
        dataSource.addUser(user)
        refreshQuestions(questions)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_quiz, menu)
        menu.apply {
            findItem(R.id.show_all).isVisible = isMarkedForLaterQuestionsShown
            findItem(R.id.show_marked_later).isVisible = !isMarkedForLaterQuestionsShown
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.finish_quiz -> confirmAndFinishQuiz()
            R.id.show_marked_later -> showMarkedLaterQuestions()
            R.id.show_all -> showMarkedLaterQuestions()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    fun markForLater(question: Question) {
        val questions = getCurrentQuizQuestionsFromUser()
        val currentQuestion = questions.find { it.id == question.id }
        currentQuestion!!.isMarkedForLater = !currentQuestion.isMarkedForLater
        dataSource.addUser(user)
        refreshQuestions(questions)
        toast( if(currentQuestion.isMarkedForLater) "Marking Question for Later" else "Question removed from Marked for Later" )
        dataSource.addUser(user)
    }

    private fun confirmAndFinishQuiz() {
        ConfirmDialog(this, "Are you sure you want to finish Quiz?") {
            finishQuiz()
        }
    }

    private fun finishQuiz() {
        getCurrentQuizFromUser()!!.status = QB_STATUS_FINISHED
        dataSource.addUser(user)
        Intent(this, ResultActivity::class.java).apply {
            putExtra(QUIZ_ID, quizId)
            startActivity(this)
        }
        finish()
    }

    private fun getCurrentQuizFromUser(): QuestionBank? {
        return user.quizzes.find { it.id == quizId }
    }

    private fun showMarkedLaterQuestions() {
        isMarkedForLaterQuestionsShown = !isMarkedForLaterQuestionsShown
        val questions = getCurrentQuizQuestionsFromUser()
        refreshQuestions(questions)
        toast( if(!isMarkedForLaterQuestionsShown) "Showing all Questions" else "Showing Marked for Later questions" )
        invalidateOptionsMenu()
    }

    private fun getCurrentQuizQuestionsFromUser(): ArrayList<Question> {
        return getCurrentQuizFromUser()!!.questions
    }

    private fun refreshQuestions(questions: ArrayList<Question>) {
        getRecyclerAdapter()?.updateQuestions(questions.filter { it -> ((it.isMarkedForLater == isMarkedForLaterQuestionsShown) || !isMarkedForLaterQuestionsShown) } as java.util.ArrayList<Question>)
    }

    override fun onPause() {
        super.onPause()
        if(runningTimer != -1L) {
            getCurrentQuizFromUser()!!.saveTimer(runningTimer)
            dataSource.addUser(user)
        }
        timer?.cancel()
    }

    override fun onResume() {
        super.onResume()
        setupTimer(getCurrentQuizFromUser()?.getResumeTimer())
    }
}

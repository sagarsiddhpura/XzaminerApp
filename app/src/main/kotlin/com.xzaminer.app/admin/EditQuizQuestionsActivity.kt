package com.xzaminer.app.admin

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.views.MyGridLayoutManager
import com.xzaminer.app.R
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.extensions.dataSource
import com.xzaminer.app.studymaterial.*
import com.xzaminer.app.utils.*
import kotlinx.android.synthetic.main.activity_quiz.*


class EditQuizQuestionsActivity : SimpleActivity() {

    private var quizId: Long = -1
    private var toolbar: Toolbar? = null
    private var courseId: Long = -1
    private var sectionId: Long = -1
    private lateinit var studyMaterial: StudyMaterial

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
            quizId = getLongExtra(QUIZ_ID, -1)
            courseId = getLongExtra(COURSE_ID, -1)
            sectionId = getLongExtra(SECTION_ID, -1)
            if(quizId == (-1).toLong()) {
                showErrorAndExit()
                return
            }
        }
        setupGridLayoutManager()

        dataSource.getCourseById(courseId) { course ->
            if(course != null) {
                val section = course.fetchSection(sectionId)
                if(section != null) {
                    val studyMaterial = section.fetchStudyMaterialById(quizId)
                    if(studyMaterial != null) {
                        loadQuestionBank(studyMaterial)
                    } else {
                        showErrorAndExit()
                        return@getCourseById
                    }
                }  else {
                    showErrorAndExit()
                    return@getCourseById
                }
            } else {
                showErrorAndExit()
                return@getCourseById
            }
        }
    }

    private fun showErrorAndExit() {
        toast("Error Opening Question Bank")
        finish()
    }

    private fun loadQuestionBank(loadedQuiz: StudyMaterial) {
        this.studyMaterial = loadedQuiz
        if (loadedQuiz != null) {
            if (loadedQuiz.questions.size <= 0) {
                toast("Error Opening Question Bank. No Questions in this Question Bank")
                finish()
                return
            }

            supportActionBar?.title = loadedQuiz.name
            quizId = loadedQuiz.id
            setupAdapter(loadedQuiz.questions)
        } else {
            toast("Error Opening Question Bank")
            finish()
        }
    }

    private fun getRecyclerAdapter() = quiz_grid.adapter as? EditQuestionsAdapter

    private fun setupGridLayoutManager() {
        val layoutManager = quiz_grid.layoutManager as MyGridLayoutManager
        layoutManager.orientation = GridLayoutManager.VERTICAL
        layoutManager.spanCount = 1
    }

    private fun setupAdapter(questions: ArrayList<Question>) {
        val currAdapter = quiz_grid.adapter
        if (currAdapter == null) {
            EditQuestionsAdapter(
                this,
                questions.clone() as ArrayList<Question>,
                quiz_grid
            ) {
            }.apply {
                quiz_grid.adapter = this
            }
        } else {
            (currAdapter as EditQuestionsAdapter).updateQuestions(questions)
        }
    }

    fun optionClicked(question: Question, id: Long) {
        val questions = studyMaterial.questions
        val currentQuestion = questions.find { it.id == question.id }
        currentQuestion!!.correctAnswer = id
        refreshQuestions(questions)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_manage, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.manage_finish -> validateAndSaveEntity()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun refreshQuestions(questions: ArrayList<Question>) {
        getRecyclerAdapter()?.updateQuestions(questions)
    }

    private fun validateAndSaveEntity() {
        ConfirmDialog(this, "Are you sure you want to update the Entity?") {
            dataSource.updateQuizQuestions(courseId, sectionId, studyMaterial)
            ConfirmationDialog(this, "Entity has been updated successfully", R.string.yes, R.string.ok, 0) { }
        }
    }

    fun editQuestion(question: Question) {
        EditQuestionDialog(this, question) {
            refreshQuestions(studyMaterial.questions)
        }
    }

    fun deleteQuestion(question: Question) {
        studyMaterial.questions = studyMaterial.questions.filter {  it.id != question.id } as ArrayList<Question>
        refreshQuestions(studyMaterial.questions)
    }
}

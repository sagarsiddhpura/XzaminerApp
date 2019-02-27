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
import com.xzaminer.app.studymaterial.ConfirmDialog
import com.xzaminer.app.studymaterial.Question
import com.xzaminer.app.studymaterial.StudyMaterial
import com.xzaminer.app.utils.COURSE_ID
import com.xzaminer.app.utils.QUIZ_ID
import com.xzaminer.app.utils.SECTION_ID
import kotlinx.android.synthetic.main.activity_quiz.*


class EditStudyMaterialQuestionsActivity : SimpleActivity() {

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
        toast("Error Opening Study Material")
        finish()
    }

    private fun loadQuestionBank(loadedQuiz: StudyMaterial) {
        this.studyMaterial = loadedQuiz
        if (loadedQuiz != null) {
            if (loadedQuiz.questions.size <= 0) {
                toast("Error Opening Study Material. No Questions in this Study Material")
                finish()
                return
            }

            supportActionBar?.title = loadedQuiz.name
            quizId = loadedQuiz.id
            setupAdapter(loadedQuiz.questions)
        } else {
            toast("Error Opening Study Material")
            finish()
        }
    }

    private fun getRecyclerAdapter() = quiz_grid.adapter as? EditStudyMaterialsQuestionsAdapter

    private fun setupGridLayoutManager() {
        val layoutManager = quiz_grid.layoutManager as MyGridLayoutManager
        layoutManager.orientation = GridLayoutManager.VERTICAL
        layoutManager.spanCount = 1
    }

    private fun setupAdapter(questions: ArrayList<Question>) {
        val currAdapter = quiz_grid.adapter
        if (currAdapter == null) {
            EditStudyMaterialsQuestionsAdapter(
                this,
                questions.clone() as ArrayList<Question>,
                quiz_grid
            ) {
            }.apply {
                quiz_grid.adapter = this
            }
        } else {
            (currAdapter as EditStudyMaterialsQuestionsAdapter).updateQuestions(questions)
        }
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
        EditStudyMaterialQuestionDialog(this, question) {
            refreshQuestions(studyMaterial.questions)
        }
    }

    fun deleteQuestion(question: Question) {
        studyMaterial.questions = studyMaterial.questions.filter {  it.id != question.id } as ArrayList<Question>
        refreshQuestions(studyMaterial.questions)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(com.xzaminer.app.R.menu.menu_manage, menu)
        menu.apply {
            findItem(R.id.manage_add).isVisible = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.manage_finish -> validateAndSaveEntity()
            R.id.manage_add -> addQuestion()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun addQuestion() {
        val highestId = studyMaterial.questions.maxBy { it.id }
        val question = Question(highestId!!.id + 1, "")
        EditStudyMaterialQuestionDialog(this, question) {
            studyMaterial.questions.add(it)
            refreshQuestions(studyMaterial.questions)
        }
    }
}
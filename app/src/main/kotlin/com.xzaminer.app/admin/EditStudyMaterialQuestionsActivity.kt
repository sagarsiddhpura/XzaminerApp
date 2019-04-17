package com.xzaminer.app.admin

import android.os.Bundle
import android.os.Environment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.dialogs.FilePickerDialog
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.views.MyGridLayoutManager
import com.xzaminer.app.R
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.extensions.dataSource
import com.xzaminer.app.studymaterial.ConfirmDialog
import com.xzaminer.app.studymaterial.Question
import com.xzaminer.app.studymaterial.QuestionOption
import com.xzaminer.app.studymaterial.StudyMaterial
import com.xzaminer.app.utils.COURSE_ID
import com.xzaminer.app.utils.QUIZ_ID
import com.xzaminer.app.utils.SECTION_ID
import kotlinx.android.synthetic.main.activity_quiz.*
import java.io.BufferedReader
import java.io.FileReader


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
        EditStudyMaterialQuestionDialog(this, question, courseId) {
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
            findItem(R.id.manage_remove_all).isVisible = true
            findItem(R.id.manage_import_questions).isVisible = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.manage_finish -> validateAndSaveEntity()
            R.id.manage_add -> addQuestion()
            R.id.manage_remove_all -> removeAllQuestions()
            R.id.manage_import_questions -> importQuestions()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun addQuestion() {
        val highestId = studyMaterial.questions.maxBy { it.id }
        var nextId = 1L
        if(highestId != null) {
            nextId = highestId.id.plus(1)
        }
        val question = Question(nextId, "")
        EditStudyMaterialQuestionDialog(this, question, courseId) {
            studyMaterial.questions.add(it)
            refreshQuestions(studyMaterial.questions)
        }
    }

    private fun importQuestions() {
        FilePickerDialog(this, Environment.getExternalStorageDirectory().toString(), true, false, false) {
            toast("Importing questions...")
            parseQuestions(it)
            refreshQuestions(studyMaterial.questions)
        }
    }

    private fun removeAllQuestions() {
        studyMaterial.questions.clear()
        refreshQuestions(studyMaterial.questions)
    }

    private fun parseQuestions(path: String) {
        var questions = arrayListOf<Question>()
        var line: String?
        val fileReader = BufferedReader(FileReader(path))

        // Read the file line by line starting from the second line
        line = fileReader.readLine()
        var counterQues = 1L
        while (line != null) {
            val ques = Question()
            var counter = 1L
            ques.id = counterQues++

            // first line is text
            ques.text = line

            line = fileReader.readLine()
            while(line != null && line != "") {
                ques.options.add(QuestionOption(counter++, line, ""))
                line = fileReader.readLine()
            }
            questions.add(ques)

            if(line == null) {
                break
            }
            line = fileReader.readLine()
        }

        ConfirmationDialog(this, questions.size.toString() + " questions imported.", R.string.yes, R.string.ok, 0) { }
        studyMaterial.questions.addAll(questions)
    }
}

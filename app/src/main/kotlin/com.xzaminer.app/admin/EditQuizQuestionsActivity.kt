package com.xzaminer.app.admin

import android.os.Bundle
import android.os.Environment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import com.opencsv.CSVIterator
import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.dialogs.FilePickerDialog
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.views.MyGridLayoutManager
import com.xzaminer.app.R
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.extensions.dataSource
import com.xzaminer.app.studymaterial.*
import com.xzaminer.app.utils.COURSE_ID
import com.xzaminer.app.utils.QUIZ_ID
import com.xzaminer.app.utils.SECTION_ID
import kotlinx.android.synthetic.main.activity_quiz.*
import java.io.FileInputStream
import java.io.InputStreamReader


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
        menu.apply {
            findItem(R.id.manage_add).isVisible = true
            findItem(R.id.manage_remove_all).isVisible = true
            findItem(R.id.manage_import_questions).isVisible = true
            findItem(R.id.manage_re_order).isVisible = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.manage_finish -> validateAndSaveEntity()
            R.id.manage_add -> addQuestion()
            R.id.manage_remove_all -> removeAllQuestions()
            R.id.manage_import_questions -> importQuestions()
            R.id.manage_re_order -> reorderQuestions()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
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

    private fun refreshQuestions(questions: ArrayList<Question>) {
        getRecyclerAdapter()?.updateQuestions(questions)
    }

    private fun validateAndSaveEntity() {
        studyMaterial.questions.forEachIndexed { index, it ->
            if(it.correctAnswer == null || it.correctAnswer == 0L) {
                toast("Question " + (index+1) + " does not have correct answer. Please mark correct answer to save.")
                return
            }
        }

        ConfirmDialog(this, "Are you sure you want to update the Entity?") {
            dataSource.updateQuizQuestions(courseId, sectionId, studyMaterial)
            ConfirmationDialog(this, "Entity has been updated successfully", R.string.yes, R.string.ok, 0) { }
        }
    }

    fun editQuestion(question: Question) {
        EditQuestionDialog(this, question, courseId) {
            refreshQuestions(studyMaterial.questions)
        }
    }

    fun deleteQuestion(question: Question) {
        studyMaterial.questions = studyMaterial.questions.filter {  it.id != question.id } as ArrayList<Question>
        refreshQuestions(studyMaterial.questions)
    }

    private fun addQuestion() {
        val highestId = studyMaterial.questions.maxBy { it.id }
        var nextId = 1L
        if(highestId != null) {
            nextId = highestId.id.plus(1)
        }
        val question = Question(nextId, "", "", arrayListOf(QuestionOption(1, ""), QuestionOption(2, ""), QuestionOption(3, ""),
            QuestionOption(4, "")))
        EditQuestionDialog(this, question, courseId) {
            studyMaterial.questions.add(it)
            refreshQuestions(studyMaterial.questions)
        }
    }

    private fun parseQuestions(path: String) {
        var questions = arrayListOf<Question>()
        var counterQues = 1L
        var lineCounter = 1L
        var tokens: Array<String>

        val parser = CSVParserBuilder()
            .withSeparator(',')
            .build()
        val reader = CSVReaderBuilder(InputStreamReader(FileInputStream(path), "Windows-1250"))
            .withCSVParser(parser)
            .build()


        val iterator = CSVIterator(reader)
        tokens = iterator.next()

        while (tokens != null) {
            val ques = Question()
            var corrAns = ""
            var corrAnsText = ""
            var counter = 1L
            if(tokens.size < 3) {
                ConfirmationDialog(this, "Invalid line found of Line number:" + lineCounter + ". Line should have atleast three columns seperated by ','") { }
                return
            }
            if(tokens[1] == "" || tokens[2] == "") {
                ConfirmationDialog(this, "Question Text and Correct answer text are required. Error on line:" + lineCounter) { }
                return
            }
            ques.id = counterQues++
            // first line is text
            ques.text = removeQuotes(tokens[1])
            corrAns = tokens[2]
            if(tokens.size > 3) {
                corrAnsText = tokens[3]
            }

            // second line is first option
            if(!iterator.hasNext()) {
                ConfirmationDialog(this, "Invalid line found of Line number:" + lineCounter + ". Expecting Option1 line after question line") { }
                return
            }
            tokens = iterator.next()
            lineCounter++
            if(tokens == null) {
                ConfirmationDialog(this, "Invalid line found of Line number:" + lineCounter + ". Expecting Option1 line after question line") { }
                return
            }
            if(tokens.size < 2) {
                ConfirmationDialog(this, "Invalid line found of Line number:" + lineCounter + ". Line should have atleast three columns seperated by ','") { }
                return
            }
            if(tokens[0] == "" || tokens[1] == "") {
                ConfirmationDialog(this, "Answer line must start with a. b. c. or d. and answer text should not be empty. Error on line:" + lineCounter) { }
                return
            }
            if(corrAns == tokens[0]) { ques.correctAnswer = counter }
            ques.options.add(QuestionOption(counter++, removeQuotes(tokens[1]), corrAnsText))

            // third line is second option
            if(!iterator.hasNext()) {
                ConfirmationDialog(this, "Invalid line found of Line number:" + lineCounter + ". Expecting Option1 line after question line") { }
                return
            }
            tokens = iterator.next()
            lineCounter++
            if(tokens == null) {
                ConfirmationDialog(this, "Invalid line found of Line number:" + lineCounter + ". Expecting Option2 line Option1 line") { }
                return
            }
            if(tokens.size < 2) {
                ConfirmationDialog(this, "Invalid line found of Line number:" + lineCounter + ". Line should have atleast three columns seperated by ','") { }
                return
            }
            if(tokens[0] == "" || tokens[1] == "") {
                ConfirmationDialog(this, "Answer line must start with a. b. c. or d. and answer text should not be empty. Error on line:" + lineCounter) { }
                return
            }
            if(corrAns == tokens[0]) { ques.correctAnswer = counter }
            ques.options.add(QuestionOption(counter++, removeQuotes(tokens[1]), corrAnsText))

            // fourth line is third option
            if(!iterator.hasNext()) {
                ConfirmationDialog(this, "Invalid line found of Line number:" + lineCounter + ". Expecting Option1 line after question line") { }
                return
            }
            tokens = iterator.next()
            lineCounter++
            if(tokens == null) {
                ConfirmationDialog(this, "Invalid line found of Line number:" + lineCounter + ". Expecting Option3 line after Option2 line") { }
                return
            }
            if(tokens.size < 2) {
                ConfirmationDialog(this, "Invalid line found of Line number:" + lineCounter + ". Line should have atleast three columns seperated by ','") { }
                return
            }
            if(tokens[0] == "" || tokens[1] == "") {
                ConfirmationDialog(this, "Answer line must start with a. b. c. or d. and answer text should not be empty. Error on line:" + lineCounter) { }
                return
            }
            if(corrAns == tokens[0]) { ques.correctAnswer = counter }
            ques.options.add(QuestionOption(counter++, removeQuotes(tokens[1]), corrAnsText))

            // fifth line is fourth option
            if(!iterator.hasNext()) {
                ConfirmationDialog(this, "Invalid line found of Line number:" + lineCounter + ". Expecting Option1 line after question line") { }
                return
            }
            tokens = iterator.next()
            lineCounter++
            if(tokens == null) {
                ConfirmationDialog(this, "Invalid line found of Line number:" + lineCounter + ". Expecting Option4 line after Qption3 line") { }
                return
            }
            if(tokens.size < 2) {
                ConfirmationDialog(this, "Invalid line found of Line number:" + lineCounter + ". Line should have atleast three columns seperated by ','") { }
                return
            }
            if(tokens[0] == "" || tokens[1] == "") {
                ConfirmationDialog(this, "Answer line must start with a. b. c. or d. and answer text should not be empty. Error on line:" + lineCounter) { }
                return
            }
            if(corrAns == tokens[0]) { ques.correctAnswer = counter }
            ques.options.add(QuestionOption(counter++, removeQuotes(tokens[1]), corrAnsText))

            if(ques.correctAnswer == 0L) {
                ConfirmationDialog(this, "Cannot determine correct answer for question. Error on line:" + lineCounter) { }
                return
            }
            questions.add(ques)

            if(!iterator.hasNext()) {
                ConfirmationDialog(this, questions.size.toString() + " questions imported.") { }
                studyMaterial.questions.addAll(questions)
                return
            }
            tokens = iterator.next()
            lineCounter++
            if(!iterator.hasNext()) {
                ConfirmationDialog(this, questions.size.toString() + " questions imported.") { }
                studyMaterial.questions.addAll(questions)
                return
            }
            tokens = iterator.next()
            lineCounter++
            while(tokens != null && (tokens[1] == "" || tokens[2] == "") && iterator.hasNext()) {
                tokens = iterator.next()
                lineCounter++
            }
            if(!iterator.hasNext()) {
                ConfirmationDialog(this, questions.size.toString() + " questions imported.") { }
                studyMaterial.questions.addAll(questions)
                return
            }
        }

        ConfirmationDialog(this, questions.size.toString() + " questions imported.") { }
        studyMaterial.questions.addAll(questions)
    }

    private fun removeQuotes(s: String): String? {
        var retVal = s
        if(retVal != null && retVal.isNotEmpty()) {
            if(retVal.startsWith("\"")) {
                retVal = retVal.substringAfter("\"")
            }
            if(retVal.endsWith("\"")) {
                retVal = retVal.substringBeforeLast("\"")
            }
        }
        return retVal
    }

    private fun reorderQuestions() {
        val list = arrayListOf<String>()
        studyMaterial.questions.forEach {
            if(it.text != null) {
                list.add(it.text!!)
            }
        }
        SortEntitiesDialog(this, list) {
            var questionsNew: ArrayList<Question> = arrayListOf()
            it.forEachIndexed { index, entity ->
                val updatedEntity = studyMaterial.questions?.find { it.text == entity }
                if (updatedEntity != null) {
                    questionsNew.add(updatedEntity)
                }
            }
            studyMaterial.questions.clear()
            studyMaterial.questions.addAll(questionsNew)
            dataSource.updateQuizQuestions(courseId, sectionId, studyMaterial)
            toast("Question re-ordering saved successfully")
            refreshQuestions(studyMaterial.questions)
        }
    }
}

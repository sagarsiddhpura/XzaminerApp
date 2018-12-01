package com.xzaminer.app.admin

import android.os.Bundle
import android.os.Environment
import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.dialogs.FilePickerDialog
import com.simplemobiletools.commons.extensions.getAdjustedPrimaryColor
import com.xzaminer.app.R
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.extensions.dataSource
import com.xzaminer.app.extensions.debugDataSource
import com.xzaminer.app.studymaterial.Question
import com.xzaminer.app.studymaterial.QuestionOption
import com.xzaminer.app.studymaterial.StudyMaterial
import kotlinx.android.synthetic.main.activity_add_question_bank.*
import java.io.BufferedReader
import java.io.FileReader
import java.util.*

class AddQuestionBankActivity : SimpleActivity() {

    var questionBank = StudyMaterial()
    var selectedPath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_question_bank)
        supportActionBar?.title = "Import Question Bank"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        image_edit_category.setColorFilter(getAdjustedPrimaryColor())
        image_edit_file.setColorFilter(getAdjustedPrimaryColor())
        category_root.setOnClickListener {
            CategoryPickerDialog(this) { section ->
                selectedPath = section.description!!
                category_value.text = section.name
            }
        }

        file_root.setOnClickListener {
            FilePickerDialog(this, Environment.getExternalStorageDirectory().toString(), true, false, false) {
                file_value.text = it
            }
        }

        import_questions.setOnClickListener { view ->
            validateAndImport()
        }
    }

    private fun importFile(path: String) {
        var line: String?
        val fileReader = BufferedReader(FileReader(path))

        // Read the file line by line starting from the second line
        line = fileReader.readLine()
        var counterQues = 1L
        while (line != null) {
            val ques = Question()
            var corrAns = ""
            var counter = 1L
            var tokens = line.split(",")
            ques.id = counterQues++

            // first line is text
            ques.text = tokens[1]
            corrAns = tokens[2]

            // second line is first option
            line = fileReader.readLine()
            tokens = line.split(",")
            if(corrAns == tokens[0]) { ques.correctAnswer = counter }
            ques.options.add(QuestionOption(counter++, tokens[1], tokens[2]))

            // third line is second option
            line = fileReader.readLine()
            tokens = line.split(",")
            if(corrAns == tokens[0]) { ques.correctAnswer = counter }
            ques.options.add(QuestionOption(counter++, tokens[1], tokens[2]))

            // fourth line is third option
            line = fileReader.readLine()
            tokens = line.split(",")
            if(corrAns == tokens[0]) { ques.correctAnswer = counter }
            ques.options.add(QuestionOption(counter++, tokens[1], tokens[2]))

            // fifth line is fourth option
            line = fileReader.readLine()
            tokens = line.split(",")
            if(corrAns == tokens[0]) { ques.correctAnswer = counter }
            ques.options.add(QuestionOption(counter++, tokens[1], tokens[2]))

            questionBank.questions.add(ques)

            line = fileReader.readLine()
            if(line == null) { break }
            line = fileReader.readLine()
        }
    }

    private fun validateAndImport() {
        if(category_value.text == null || category_value.text == "") {
            ConfirmationDialog(this, "Please select Category", R.string.yes, R.string.ok, 0) { }
            return
        }
        if(name_value.text == null || name_value.text.toString() == "") {
            ConfirmationDialog(this, "Please enter name for Question Bank", R.string.yes, R.string.ok, 0) { }
            return
        }
        if(file_value.text == null || file_value.text == "") {
            ConfirmationDialog(this, "Please select file to import", R.string.yes, R.string.ok, 0) { }
            return
        }
        if(timer_value.text != null && timer_value.text.toString() != "") {
            try {
                var secs = 0L
                val lengthString = timer_value.text.toString()
                val split = lengthString.split(":")
                secs = split[0].toLong() * 60
                secs += split[1].toInt()
            } catch (ex : Exception) {
                ConfirmationDialog(this, "Please enter timer in format of MM:SS", R.string.yes, R.string.ok, 0) { }
                return
            }
            questionBank.addTotalTimer(timer_value.text.toString())
        }
        questionBank.name = name_value.text.toString()
        questionBank.id = Date().time
        importFile(file_value.text.toString())
        if(questionBank.questions.isEmpty()) {
            ConfirmationDialog(this, "Error importing questions. Please select file with proper syntax", R.string.yes, R.string.ok, 0) { }
            return
        } else {
            dataSource.addQuestionBank(selectedPath, questionBank)
            questionBank.description = selectedPath
            debugDataSource.addDebugObject(dataSource, "studyMaterials/" + Date().time, questionBank)
            ConfirmationDialog(this, "Question Bank is Imported successfully", R.string.yes, R.string.ok, 0) { }
        }
    }
}

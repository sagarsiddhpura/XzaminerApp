package com.xzaminer.app.admin

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.opencsv.CSVIterator
import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.dialogs.FilePickerDialog
import com.simplemobiletools.commons.extensions.beGone
import com.simplemobiletools.commons.extensions.beVisible
import com.simplemobiletools.commons.extensions.getAdjustedPrimaryColor
import com.simplemobiletools.commons.extensions.toast
import com.xzaminer.app.BuildConfig
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.billing.Purchase
import com.xzaminer.app.extensions.dataSource
import com.xzaminer.app.extensions.debugDataSource
import com.xzaminer.app.studymaterial.Question
import com.xzaminer.app.studymaterial.QuestionOption
import com.xzaminer.app.studymaterial.StudyMaterial
import com.xzaminer.app.utils.*
import kotlinx.android.synthetic.main.activity_add_question_bank.*
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.*

class AddQuestionBankActivity : SimpleActivity() {

    var questionBank = StudyMaterial()
    var selectedPath = ""
    var courseId = ""
    var sectionId = ""
    var monetization = ""
    var sectionIdLong = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.xzaminer.app.R.layout.activity_add_question_bank)
        supportActionBar?.title = "Import Question Bank"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        image_edit_category.setColorFilter(getAdjustedPrimaryColor())
        image_edit_file.setColorFilter(getAdjustedPrimaryColor())
        image_edit_image.setColorFilter(getAdjustedPrimaryColor())
        questionBank.id = Date().time
        questionBank.type = STUDY_MATERIAL_TYPE_QUESTION_BANK
        purchase_id.text = PURCHASE_SECTION_STUDY_MATERIAL + questionBank.id

        if(BuildConfig.DEBUG) {
            name_value.setText("QB")
            selectedPath = "/1/courses/101/sections/1015/studyMaterials"
            category_value.text = "Question Banks"
            courseId = "101"
            sectionId = "1015"
        } else {
            intent.apply {
                sectionIdLong = getLongExtra(SECTION_ID, -1)
                val name = getStringExtra(QUESTION_BANK_NAME)
                if(name != null && sectionIdLong != (-1).toLong()) {
                    dataSource.getSectionPathById(sectionIdLong) {
                        if(it != null) {
                            selectedPath = "$it/studyMaterials"
                            category_value.text = name
                        }
                    }
                }
            }
        }

        val options = arrayOf("None", "Trial", "Monetized")
        monetization_spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)

        //item selected listener for spinner
        monetization_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, selected: Int, p3: Long) {
                monetization = options[selected]
                if(monetization == "None" || monetization == "Trial") {
                    monetization_root.beGone()
                } else {
                    monetization_root.beVisible()
                }
            }
        }

        category_root.setOnClickListener {
            CategoryPickerDialog(this) { section ->
                if(section.type != STUDY_MATERIAL_TYPE_QUESTION_BANK) {
                    toast("Selected section is not of type Question Bank. You can only add question bank to Question Bank type section")
                } else {
                    selectedPath = section.desc!!
                    category_value.text = section.name

                    val courseStart = selectedPath.indexOf("courses/")
                    val sectionStart = selectedPath.indexOf("sections/")
                    courseId = selectedPath.substring(courseStart+8, sectionStart - 1)
                    sectionId = section.id.toString()
                }
            }
        }

        file_root.setOnClickListener {
            FilePickerDialog(this, Environment.getExternalStorageDirectory().toString(), true, false, false) {
                file_value.text = it
            }
        }

        image_root.setOnClickListener {
            FilePickerDialog(this, Environment.getExternalStorageDirectory().toString(), true, false, false) {
                image_value.text = it
            }
        }

        import_questions.setOnClickListener { view ->
            validateAndImport()
        }
    }

    private fun parseQuestions(path: String) {
        questionBank.questions.clear()
        var counterQues = 1L
        var lineCounter = 1L
        var tokens: Array<String>

        val parser = CSVParserBuilder()
            .withSeparator(',')
            .build()

        val reader = CSVReaderBuilder(InputStreamReader(FileInputStream(path), "Windows-1250"))
            .withCSVParser(parser)
            .build()
        // UTF-8 -> Questionmarks
        // ISO-8859-1 -> boxes

        val iterator = CSVIterator(reader)
        tokens = iterator.next()

        while (tokens != null) {
            val ques = Question()
            var corrAns = ""
            var corrAnsText = ""
            var counter = 1L
            if(tokens.size < 3) {
                ConfirmationDialog(this, "Invalid line found of Line number:" + lineCounter + ". Line should have atleast three columns seperated by ','") { }
                questionBank.questions.clear()
                return
            }
            if(tokens[1] == "" || tokens[2] == "") {
                ConfirmationDialog(this, "Question Text and Correct answer text are required. Error on line:" + lineCounter) { }
                questionBank.questions.clear()
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
                questionBank.questions.clear()
                return
            }
            tokens = iterator.next()
            lineCounter++
            if(tokens == null) {
                ConfirmationDialog(this, "Invalid line found of Line number:" + lineCounter + ". Expecting Option1 line after question line") { }
                questionBank.questions.clear()
                return
            }
            if(tokens.size < 2) {
                ConfirmationDialog(this, "Invalid line found of Line number:" + lineCounter + ". Line should have atleast three columns seperated by ','") { }
                questionBank.questions.clear()
                return
            }
            if(tokens[0] == "" || tokens[1] == "") {
                ConfirmationDialog(this, "Answer line must start with a. b. c. or d. and answer text should not be empty. Error on line:" + lineCounter) { }
                questionBank.questions.clear()
                return
            }
            if(corrAns == tokens[0]) { ques.correctAnswer = counter }
            ques.options.add(QuestionOption(counter++, removeQuotes(tokens[1]), corrAnsText))

            // third line is second option
            if(!iterator.hasNext()) {
                ConfirmationDialog(this, "Invalid line found of Line number:" + lineCounter + ". Expecting Option1 line after question line") { }
                questionBank.questions.clear()
                return
            }
            tokens = iterator.next()
            lineCounter++
            if(tokens == null) {
                ConfirmationDialog(this, "Invalid line found of Line number:" + lineCounter + ". Expecting Option2 line Option1 line") { }
                questionBank.questions.clear()
                return
            }
            if(tokens.size < 2) {
                ConfirmationDialog(this, "Invalid line found of Line number:" + lineCounter + ". Line should have atleast three columns seperated by ','") { }
                questionBank.questions.clear()
                return
            }
            if(tokens[0] == "" || tokens[1] == "") {
                ConfirmationDialog(this, "Answer line must start with a. b. c. or d. and answer text should not be empty. Error on line:" + lineCounter) { }
                questionBank.questions.clear()
                return
            }
            if(corrAns == tokens[0]) { ques.correctAnswer = counter }
            ques.options.add(QuestionOption(counter++, removeQuotes(tokens[1]), corrAnsText))

            // fourth line is third option
            if(!iterator.hasNext()) {
                ConfirmationDialog(this, "Invalid line found of Line number:" + lineCounter + ". Expecting Option1 line after question line") { }
                questionBank.questions.clear()
                return
            }
            tokens = iterator.next()
            lineCounter++
            if(tokens == null) {
                ConfirmationDialog(this, "Invalid line found of Line number:" + lineCounter + ". Expecting Option3 line after Option2 line") { }
                questionBank.questions.clear()
                return
            }
            if(tokens.size < 2) {
                ConfirmationDialog(this, "Invalid line found of Line number:" + lineCounter + ". Line should have atleast three columns seperated by ','") { }
                questionBank.questions.clear()
                return
            }
            if(tokens[0] == "" || tokens[1] == "") {
                ConfirmationDialog(this, "Answer line must start with a. b. c. or d. and answer text should not be empty. Error on line:" + lineCounter) { }
                questionBank.questions.clear()
                return
            }
            if(corrAns == tokens[0]) { ques.correctAnswer = counter }
            ques.options.add(QuestionOption(counter++, removeQuotes(tokens[1]), corrAnsText))

            // fifth line is fourth option
            if(!iterator.hasNext()) {
                ConfirmationDialog(this, "Invalid line found of Line number:" + lineCounter + ". Expecting Option1 line after question line") { }
                questionBank.questions.clear()
                return
            }
            tokens = iterator.next()
            lineCounter++
            if(tokens == null) {
                ConfirmationDialog(this, "Invalid line found of Line number:" + lineCounter + ". Expecting Option4 line after Qption3 line") { }
                questionBank.questions.clear()
                return
            }
            if(tokens.size < 2) {
                ConfirmationDialog(this, "Invalid line found of Line number:" + lineCounter + ". Line should have atleast three columns seperated by ','") { }
                questionBank.questions.clear()
                return
            }
            if(tokens[0] == "" || tokens[1] == "") {
                ConfirmationDialog(this, "Answer line must start with a. b. c. or d. and answer text should not be empty. Error on line:" + lineCounter) { }
                questionBank.questions.clear()
                return
            }
            if(corrAns == tokens[0]) { ques.correctAnswer = counter }
            ques.options.add(QuestionOption(counter++, removeQuotes(tokens[1]), corrAnsText))

            if(ques.correctAnswer == 0L) {
                ConfirmationDialog(this, "Cannot determine correct answer for question. Error on line:" + lineCounter) { }
                questionBank.questions.clear()
                return
            }
            questionBank.questions.add(ques)

            if(!iterator.hasNext()) {
                return
            }
            tokens = iterator.next()
            lineCounter++
            if(!iterator.hasNext()) {
                return
            }
            tokens = iterator.next()
            lineCounter++
            while(tokens != null && (tokens[1] == "" || tokens[2] == "") && iterator.hasNext()) {
                tokens = iterator.next()
                lineCounter++
            }
            if(!iterator.hasNext()) {
                return
            }
        }
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

    private fun validateAndImport() {
        if(category_value.text == null || category_value.text == "") {
            ConfirmationDialog(this, "Please select Category", com.xzaminer.app.R.string.yes, com.xzaminer.app.R.string.ok, 0) { }
            return
        }
        if(name_value.text == null || name_value.text.toString() == "") {
            ConfirmationDialog(this, "Please enter name for Question Bank", com.xzaminer.app.R.string.yes, com.xzaminer.app.R.string.ok, 0) { }
            return
        }
        if(file_value.text == null || file_value.text == "") {
            ConfirmationDialog(this, "Please select file to import", com.xzaminer.app.R.string.yes, com.xzaminer.app.R.string.ok, 0) { }
            return
        }
        if(monetization == "Monetized") {
            if(purchase_name.text == null || purchase_name.text.toString() == "") {
                ConfirmationDialog(this, "Please fill purchase name", com.xzaminer.app.R.string.yes, com.xzaminer.app.R.string.ok, 0) { }
                return
            }
            if(purchase_actual.text == null || purchase_actual.text.toString() == "") {
                ConfirmationDialog(this, "Please fill Actual Price", com.xzaminer.app.R.string.yes, com.xzaminer.app.R.string.ok, 0) { }
                return
            }
        }
        if(timer_value.text != null && timer_value.text.toString() != "") {
            try {
                var secs = 0L
                val lengthString = timer_value.text.toString()
                val split = lengthString.split(":")
                secs = split[0].toLong() * 60
                secs += split[1].toInt()
            } catch (ex : Exception) {
                ConfirmationDialog(this, "Please enter timer in format of MM:SS", com.xzaminer.app.R.string.yes, com.xzaminer.app.R.string.ok, 0) { }
                return
            }
            questionBank.addTotalTimer(timer_value.text.toString())
        }
        toast("Reading file...")
        parseQuestions(file_value.text.toString())
        if(questionBank.questions.isEmpty()) {
            ConfirmationDialog(this, "Error importing questions. Please select file with proper syntax", com.xzaminer.app.R.string.yes, com.xzaminer.app.R.string.ok, 0) { }
            return
        }
        toast("Importing Question Bank. Please wait...")

        if(image_value.text != null && image_value.text != "") {
            val imageFile = File(image_value.text.toString())
            val file = Uri.fromFile(imageFile)
            val name = sectionId + "_" + questionBank.id +  "_" + imageFile.name
            val riversRef = dataSource.getStorage().getReference("courses/" + courseId + "/").child(name)
            val uploadTask = riversRef.putFile(file)

            uploadTask.addOnFailureListener {
                toast("Failed to Upload Image and Question Bank")
            }.addOnSuccessListener {
                questionBank.image = "courses/" + courseId + "/" + name
                saveQuestionBank()
            }
        } else {
            saveQuestionBank()
        }
    }

    private fun saveQuestionBank() {
        questionBank.name = name_value.text.toString()
        questionBank.desc = desc_value.text.toString()
        questionBank.properties[QB_IMPORT_PATH] = arrayListOf(selectedPath)
        if(monetization == "Trial") {
            questionBank.purchaseInfo.addAll(
            arrayListOf(
                Purchase(
                    PURCHASE_SECTION_STUDY_MATERIAL + questionBank.id, purchase_name.text.toString(), purchase_desc.text.toString(),
                    PURCHASE_TYPE_TRIAL, "100", "", true, null, null, questionBank.desc)
            ))
        } else if (monetization == "Monetized") {
            questionBank.purchaseInfo.addAll(
                arrayListOf(
                    Purchase(
                        PURCHASE_SECTION_STUDY_MATERIAL + questionBank.id, purchase_name.text.toString(), purchase_desc.text.toString(),
                        PURCHASE_TYPE_IAP, purchase_actual.text.toString(), purchase_orignal.text.toString(), true, null, null, purchase_extra_info.text.toString())
                ))
        }
        dataSource.addQuestionBank(selectedPath, questionBank)
        debugDataSource.addDebugObject(dataSource, "studyMaterials/" + questionBank.id, questionBank)
        ConfirmationDialog(this, "Question Bank is Imported successfully with " + questionBank.questions.size + " Questions", com.xzaminer.app.R.string.yes, com.xzaminer.app.R.string.ok, 0) { }
    }
}

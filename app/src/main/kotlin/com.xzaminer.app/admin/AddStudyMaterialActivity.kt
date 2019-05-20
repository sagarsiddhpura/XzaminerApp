package com.xzaminer.app.admin

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.dialogs.FilePickerDialog
import com.simplemobiletools.commons.extensions.beGone
import com.simplemobiletools.commons.extensions.beVisible
import com.simplemobiletools.commons.extensions.getAdjustedPrimaryColor
import com.simplemobiletools.commons.extensions.toast
import com.xzaminer.app.BuildConfig
import com.xzaminer.app.R
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.billing.Purchase
import com.xzaminer.app.extensions.dataSource
import com.xzaminer.app.extensions.debugDataSource
import com.xzaminer.app.studymaterial.Question
import com.xzaminer.app.studymaterial.QuestionOption
import com.xzaminer.app.studymaterial.StudyMaterial
import com.xzaminer.app.utils.*
import kotlinx.android.synthetic.main.activity_add_question_bank.*
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.*

class AddStudyMaterialActivity : SimpleActivity() {

    var questionBank = StudyMaterial()
    var selectedPath = ""
    var courseId = ""
    var sectionId = ""
    var sectionIdLong = -1L
    var monetization = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_question_bank)
        supportActionBar?.title = "Import Study Material"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        image_edit_category.setColorFilter(getAdjustedPrimaryColor())
        image_edit_file.setColorFilter(getAdjustedPrimaryColor())
        image_edit_image.setColorFilter(getAdjustedPrimaryColor())
        questionBank.id = Date().time
        questionBank.type = STUDY_MATERIAL_TYPE_STUDY_MATERIAL
        purchase_id.text = PURCHASE_SECTION_STUDY_MATERIAL + questionBank.id

        timer_value.beGone()
        timer_label.beGone()
        file_label.text = "Study Material File:"
        if(BuildConfig.DEBUG) {
            name_value.setText("SM")
            selectedPath = "/1/courses/101/sections/1014/studyMaterials"
            category_value.text = "Flash Cards"
            courseId = "101"
            sectionId = "1014"
        } else {
            intent.apply {
                sectionIdLong = getLongExtra(SECTION_ID, -1)
                var name = getStringExtra(QUESTION_BANK_NAME)
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
                if(section.type != STUDY_MATERIAL_TYPE_STUDY_MATERIAL) {
                    toast("Selected section is not of type Study Material. You can only add Study Material to Study Material type section")
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
        var line: String?
        val fileReader = BufferedReader(InputStreamReader(FileInputStream(path), "Windows-1250"))

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
            questionBank.questions.add(ques)

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
            ConfirmationDialog(this, "Please enter name for Study Material", R.string.yes, R.string.ok, 0) { }
            return
        }
        if(file_value.text == null || file_value.text == "") {
            ConfirmationDialog(this, "Please select file to import", R.string.yes, R.string.ok, 0) { }
            return
        }
        if(monetization == "Monetized") {
            if(purchase_name.text == null || purchase_name.text.toString() == "") {
                ConfirmationDialog(this, "Please fill purchase name", R.string.yes, R.string.ok, 0) { }
                return
            }
            if(purchase_actual.text == null || purchase_actual.text.toString() == "") {
                ConfirmationDialog(this, "Please fill Actual Price", R.string.yes, R.string.ok, 0) { }
                return
            }
        }

        questionBank.name = name_value.text.toString()
        questionBank.id = Date().time
        parseQuestions(file_value.text.toString())
        if(questionBank.questions.isEmpty()) {
            ConfirmationDialog(this, "Error importing questions. Please select file with proper syntax", R.string.yes, R.string.ok, 0) { }
            return
        }
        toast("Importing Study Material. Please wait...")

        if(image_value.text != null && image_value.text != "") {
            val imageFile = File(image_value.text.toString())
            val file = Uri.fromFile(imageFile)
            val name = sectionId + "_" + questionBank.id +  "_" + imageFile.name
            val riversRef = dataSource.getStorage().getReference("courses/" + courseId + "/").child(name)
            val uploadTask = riversRef.putFile(file)

            uploadTask.addOnFailureListener {
                toast("Failed to Upload Image and Quesion Bank")
            }.addOnSuccessListener {
                questionBank.image = "courses/" + courseId + "/" + name
                saveQuestionBank()
            }
        } else {
            saveQuestionBank()
        }
        toast("Importing Study Material. Please wait...")
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
        ConfirmationDialog(this, "Study Material is Imported successfully", R.string.yes, R.string.ok, 0) { }
    }
}

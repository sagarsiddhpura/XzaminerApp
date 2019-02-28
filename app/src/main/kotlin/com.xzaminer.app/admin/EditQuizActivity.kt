package com.xzaminer.app.admin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.dialogs.FilePickerDialog
import com.simplemobiletools.commons.extensions.beGone
import com.simplemobiletools.commons.extensions.beVisible
import com.simplemobiletools.commons.extensions.getAdjustedPrimaryColor
import com.simplemobiletools.commons.extensions.toast
import com.xzaminer.app.R
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.billing.Purchase
import com.xzaminer.app.extensions.dataSource
import com.xzaminer.app.extensions.loadIconImageView
import com.xzaminer.app.extensions.loadImageImageView
import com.xzaminer.app.studymaterial.ConfirmDialog
import com.xzaminer.app.studymaterial.StudyMaterial
import com.xzaminer.app.utils.*
import kotlinx.android.synthetic.main.activity_edit_course.*
import java.io.File

class EditQuizActivity : SimpleActivity() {

    private var courseId: Long? = null
    var monetization = ""
    private lateinit var studyMaterial: StudyMaterial
    private var sectionId: Long = -1
    private var quizId: Long = -1
    var isNew : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_course)
        supportActionBar?.title = "Edit Quiz/Study Material"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        intent.apply {
            quizId = getLongExtra(QUIZ_ID, -1)
            courseId = getLongExtra(COURSE_ID, -1)
            sectionId = getLongExtra(SECTION_ID, -1)
            isNew = getBooleanExtra(IS_NEW_QUIZ, false)
            if(quizId == (-1).toLong()) {
                showErrorAndExit()
                return
            }
        }

        dataSource.getCourseById(courseId) { course ->
            if(course != null) {
                val section = course.fetchSection(sectionId)
                if(section != null) {
                    if(isNew) {
                        val studyMaterial = StudyMaterial(System.nanoTime())
                        studyMaterial.type = STUDY_MATERIAL_TYPE_VIDEO
                        quizId = studyMaterial.id
                        loadQuestionBank(studyMaterial)
                    } else {
                        val studyMaterial = section.fetchStudyMaterialById(quizId)
                        if(studyMaterial != null) {
                            loadQuestionBank(studyMaterial)
                        } else {
                            showErrorAndExit()
                            return@getCourseById
                        }
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

        edit_edit_image.setOnClickListener {
            FilePickerDialog(this, Environment.getExternalStorageDirectory().toString(), true, false, false) {
                toast("Uploading image. Please wait till image is uploaded.")
                val imageFile = File(it)
                val file = Uri.fromFile(imageFile)
                val name = studyMaterial.id.toString() + "_" + imageFile.name
                val riversRef = dataSource.getStorage().getReference("courses/" + courseId + "/").child(name)
                val uploadTask = riversRef.putFile(file)

                uploadTask.addOnFailureListener {
                    toast("Failed to Upload Image")
                }.addOnSuccessListener {
                    toast("Image Uploaded successfully. Save to update course")
                    studyMaterial.image = "courses/" + courseId + "/" + name
                    loadImageImageView(studyMaterial.image!!, edit_course_image, false, null, false, R.drawable.im_placeholder_video)
                }
            }
        }

        edit_delete_image.setOnClickListener {
            studyMaterial.image = null
            edit_course_image.setImageDrawable(null)
        }

        val options = arrayOf("None", "Trial", "Monetized")
        monetization_spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)

        //item selected listener for spinner
        monetization_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, selected: Int, p3: Long) {
                monetization = options[selected]
                if(monetization == "None") {
                    monetization_root.beGone()
                } else if(monetization == "Trial") {
                    monetization_root.beGone()
                } else {
                    monetization_root.beVisible()
                }
            }
        }

        edit_edit_image.setColorFilter(getAdjustedPrimaryColor())
        edit_delete_image.setColorFilter(getAdjustedPrimaryColor())
    }

    private fun loadQuestionBank(studyMaterial_: StudyMaterial) {
        this.studyMaterial = studyMaterial_
        edit_name.setText(studyMaterial_.name)
        edit_desc.setText(studyMaterial_.desc)
        monetization_root.beGone()
        if(studyMaterial_.image != null && studyMaterial_.image != "") {
            loadImageImageView(studyMaterial_.image!!, edit_course_image, false, null, false, R.drawable.im_placeholder_video)
        } else {
            val img : Int = R.drawable.im_placeholder
            loadIconImageView(img, edit_course_image, false)
        }

        if(studyMaterial_.fetchVisiblePurchases().isEmpty()) {
            monetization_spinner.setSelection(0)
        } else if(!studyMaterial_.fetchVisiblePurchases().isEmpty()) {
            val purchase = studyMaterial_.fetchVisiblePurchases().first()
            if(purchase.type == PURCHASE_TYPE_TRIAL) {
                monetization_spinner.setSelection(1)
            } else {
                monetization_spinner.setSelection(2)
                monetization_root.beVisible()
                purchase_id.setText(purchase.id)
                purchase_name.setText(purchase.name)
                purchase_desc.setText(purchase.desc)
                purchase_orignal.setText(purchase.originalPrice)
                purchase_actual.setText(purchase.actualPrice)
                purchase_extra_info.setText(purchase.extraPurchaseInfo)
            }
        }

        edit_content.beVisible()
        edit_content.setOnClickListener {
            if(studyMaterial.type == STUDY_MATERIAL_TYPE_QUESTION_BANK) {
                Intent(this, EditQuizQuestionsActivity::class.java).apply {
                    putExtra(QUIZ_ID, quizId)
                    putExtra(SECTION_ID, sectionId)
                    putExtra(COURSE_ID, courseId)
                    startActivity(this)
                }
            } else if(studyMaterial.type == STUDY_MATERIAL_TYPE_STUDY_MATERIAL) {
                Intent(this, EditStudyMaterialQuestionsActivity::class.java).apply {
                    putExtra(QUIZ_ID, quizId)
                    putExtra(SECTION_ID, sectionId)
                    putExtra(COURSE_ID, courseId)
                    startActivity(this)
                }
            } else {
                Intent(this, ManageCourseSectionVideosDomainActivity::class.java).apply {
                    putExtra(QUIZ_ID, quizId)
                    putExtra(SECTION_ID, sectionId)
                    putExtra(COURSE_ID, courseId)
                    startActivity(this)
                }
            }
        }
        if(studyMaterial.type == STUDY_MATERIAL_TYPE_VIDEO) {
            edit_content.text = "Edit Videos"
            supportActionBar?.title = "Edit Video Domain"
        }
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

    private fun validateAndSaveEntity() {
        if(edit_name.text == null || edit_name.text.toString() == "") {
            ConfirmationDialog(this, "Please enter Name", R.string.yes, R.string.ok, 0) { }
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
            if(purchase_id.text == null || purchase_id.text.toString() == "") {
                ConfirmationDialog(this, "Please fill Purchase id", R.string.yes, R.string.ok, 0) { }
                return
            }
        }

        ConfirmDialog(this, "Are you sure you want to update the Entity?") {
            studyMaterial.name = edit_name.text.toString()
            studyMaterial.desc = edit_desc.text.toString()

            studyMaterial.purchaseInfo.clear()
            if (monetization == "Monetized") {
                studyMaterial.purchaseInfo.addAll(
                    arrayListOf(
                        Purchase(
                            purchase_id.text.toString() , purchase_name.text.toString(), purchase_desc.text.toString(),
                            PURCHASE_TYPE_IAP, purchase_actual.text.toString(), purchase_orignal.text.toString(), true, null, null, purchase_extra_info.text.toString())
                    ))
            } else if(monetization == "Trial") {
                studyMaterial.purchaseInfo.addAll(
                    arrayListOf(
                        Purchase(
                            PURCHASE_SECTION_STUDY_MATERIAL + studyMaterial.id, purchase_name.text.toString(), purchase_desc.text.toString(),
                            PURCHASE_TYPE_TRIAL, "100", "", true, null, null, studyMaterial.desc)
                    ))
            }

            if(isNew) {
                dataSource.addStudyMaterial(courseId, sectionId, studyMaterial)
            } else {
                dataSource.updateQuizProperties(courseId, sectionId, studyMaterial)
            }
            ConfirmationDialog(this, "Entity has been updated successfully", R.string.yes, R.string.ok, 0) { }
        }
    }

    private fun showErrorAndExit() {
        toast("Error Opening Entity")
        finish()
    }

    override fun onBackPressed() {
        ConfirmationDialog(this, "Are you sure you want to exit? All unsaved changes will be lost", R.string.yes, R.string.ok, 0) {
            super.onBackPressed()
        }
    }
}

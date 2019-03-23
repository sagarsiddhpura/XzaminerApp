package com.xzaminer.app.admin

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.extensions.beGone
import com.simplemobiletools.commons.extensions.beVisible
import com.simplemobiletools.commons.extensions.toast
import com.xzaminer.app.R
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.billing.Purchase
import com.xzaminer.app.course.CourseSection
import com.xzaminer.app.extensions.dataSource
import com.xzaminer.app.studymaterial.ConfirmDialog
import com.xzaminer.app.utils.*
import kotlinx.android.synthetic.main.activity_edit_course.*


class EditSectionActivity : SimpleActivity() {

    private var courseId: Long? = null
    var monetization = ""
    private lateinit var section: CourseSection
    private var sectionId: Long = -1
    private var isNew: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_course)
        supportActionBar?.title = "Edit Section"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        intent.apply {
            courseId = getLongExtra(COURSE_ID, -1)
            sectionId = getLongExtra(SECTION_ID, -1)
            isNew = getBooleanExtra(IS_NEW_QUIZ, false)
            if(courseId == (-1).toLong() || sectionId == (-1).toLong()) {
                toast("Error opening Section")
                finish()
                return
            }
        }

        dataSource.getCourseById(courseId) { course ->
            if(course != null) {
                if(isNew) {
                    val section = CourseSection(System.nanoTime())
                    loadSection(section)
                } else {
                    if(course.sections[sectionId.toString()] != null) {
                        loadSection(course.sections[sectionId.toString()]!!)
                    } else {
                        toast("Error opening Course and Section")
                        finish()
                        return@getCourseById
                    }
                }
            } else {
                toast("Error opening Course and Section")
                finish()
                return@getCourseById
            }
        }

        val options = arrayOf("None", "Monetized")
        monetization_spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)

        //item selected listener for spinner
        monetization_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, selected: Int, p3: Long) {
                monetization = options[selected]
                if(monetization == "None") {
                    monetization_root.beGone()
                } else {
                    monetization_root.beVisible()
                }
            }
        }
    }

    private fun loadSection(section: CourseSection) {
        this.section = section
        edit_name.setText(section.name)
        edit_desc.setText(section.desc)

        edit_image_root.beGone()
        edit_content.beVisible()
        val img = resources.getDrawable(com.xzaminer.app.R.drawable.ic_add)
        img.setBounds(0, 0, 0, 0)
        edit_content.setCompoundDrawables(img, null, null, null)
        if(!section.studyMaterials.isEmpty()) {
            val studyMaterial = section.studyMaterials.values.first()
            when {
                studyMaterial.type == STUDY_MATERIAL_TYPE_STUDY_MATERIAL -> edit_content.text = "Add Study Material"
                studyMaterial.type == STUDY_MATERIAL_TYPE_QUESTION_BANK -> edit_content.text = "Add Question Bank"
                studyMaterial.type == STUDY_MATERIAL_TYPE_VIDEO -> edit_content.text = "Add Video Domain"
            }
        } else {
            edit_content.text = "Add QB / SM / Video Domain"
        }

        if(section.fetchVisiblePurchases().isEmpty()) {
            monetization_root.beGone()
        } else if(!section.fetchVisiblePurchases().isEmpty()) {
            monetization_spinner.setSelection(1)
            monetization_root.beVisible()
            val purchase = section.fetchVisiblePurchases().first()
            purchase_id.setText(purchase.id)
            purchase_name.setText(purchase.name)
            purchase_desc.setText(purchase.desc)
            purchase_orignal.setText(purchase.originalPrice)
            purchase_actual.setText(purchase.actualPrice)
            purchase_extra_info.setText(purchase.extraPurchaseInfo)
        }

        edit_content.setOnClickListener {
            if(section.studyMaterials.isEmpty()) {
                // choose section type
                val values = arrayOf("Quiz", "Study Material", "Video Domain")
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Select the type of Section:")
                lateinit var dialog:AlertDialog

                builder.setSingleChoiceItems(values,-1) { _, which->
                    val value = values[which]
                    when (value) {
                        "Quiz" -> {
                            Intent(this, AddQuestionBankActivity::class.java).apply {
                                putExtra(QUIZ_ID, 1L)
                                putExtra(SECTION_ID, section.id)
                                putExtra(COURSE_ID, courseId)
                                putExtra(QUESTION_BANK_NAME, section.name)
                                startActivity(this)
                            }
                        }
                        "Study Material" -> {
                            Intent(this, AddStudyMaterialActivity::class.java).apply {
                                putExtra(QUIZ_ID, 1L)
                                putExtra(SECTION_ID, section.id)
                                putExtra(COURSE_ID, courseId)
                                putExtra(QUESTION_BANK_NAME, section.name)
                                startActivity(this)
                            }
                        }
                        "Video Domain" -> {
                            Intent(this, EditQuizActivity::class.java).apply {
                                putExtra(QUIZ_ID, 1L)
                                putExtra(SECTION_ID, section.id)
                                putExtra(COURSE_ID, courseId)
                                putExtra(IS_NEW_QUIZ, true)
                                startActivity(this)
                            }
                        }
                    }
                    dialog.dismiss()
                }

                dialog = builder.create()
                dialog.show()
            } else {
                val studyMaterial = section.studyMaterials.values.first()
                when {
                    studyMaterial.type == STUDY_MATERIAL_TYPE_STUDY_MATERIAL -> {
                        Intent(this, AddStudyMaterialActivity::class.java).apply {
                            putExtra(QUIZ_ID, 1L)
                            putExtra(SECTION_ID, section.id)
                            putExtra(COURSE_ID, courseId)
                            putExtra(QUESTION_BANK_NAME, section.name)
                            startActivity(this)
                        }
                    }
                    studyMaterial.type == STUDY_MATERIAL_TYPE_QUESTION_BANK -> {
                        Intent(this, AddQuestionBankActivity::class.java).apply {
                            putExtra(QUIZ_ID, 1L)
                            putExtra(SECTION_ID, section.id)
                            putExtra(COURSE_ID, courseId)
                            putExtra(QUESTION_BANK_NAME, section.name)
                            startActivity(this)
                        }
                    }
                    studyMaterial.type == STUDY_MATERIAL_TYPE_VIDEO -> {
                        Intent(this, EditQuizActivity::class.java).apply {
                            putExtra(QUIZ_ID, 1L)
                            putExtra(SECTION_ID, section.id)
                            putExtra(COURSE_ID, courseId)
                            putExtra(IS_NEW_QUIZ, true)
                            startActivity(this)
                        }
                    }
                }
            }
        }

        val options = arrayOf("Yes", "No")
        visibility_spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)

        if(section.isVisible) {
            visibility_spinner.setSelection(0)
        } else {
            visibility_spinner.setSelection(1)
        }

        order_value.setText(section.order.toString())
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

        try {
            Integer.parseInt(order_value.text.toString())
        } catch (e : Exception) {
            ConfirmationDialog(this, "Please fill correct numeric value for Order", R.string.yes, R.string.ok, 0) { }
            return
        }

        ConfirmDialog(this, "Are you sure you want to update the Course?") {
            section.name = edit_name.text.toString()
            section.desc = edit_desc.text.toString()

            section.purchaseInfo.clear()
            if (monetization == "Monetized") {
                section.purchaseInfo.addAll(
                    arrayListOf(
                        Purchase(
                            purchase_id.text.toString() , purchase_name.text.toString(), purchase_desc.text.toString(),
                            PURCHASE_TYPE_IAP, purchase_actual.text.toString(), purchase_orignal.text.toString(), true, null, null, purchase_extra_info.text.toString())
                    ))
            }

            try {
                section.order = Integer.parseInt(order_value.text.toString())
            } catch (e : Exception) { }

            when {
                visibility_spinner.selectedItemPosition == 0 -> section.isVisible = true
                visibility_spinner.selectedItemPosition == 1 -> section.isVisible = false
            }
            try {
                section.order = Integer.parseInt(order_value.text.toString())
            } catch (e : Exception) { }

            when {
                visibility_spinner.selectedItemPosition == 0 -> section.isVisible = true
                visibility_spinner.selectedItemPosition == 1 -> section.isVisible = false
            }

            if(isNew) {
                dataSource.addCourseSection(courseId, section)
            } else {
                dataSource.updateCourseSectionProperties(courseId, section)
            }
            ConfirmationDialog(this, "Section has been updated successfully", R.string.yes, R.string.ok, 0) { }
        }
    }
}

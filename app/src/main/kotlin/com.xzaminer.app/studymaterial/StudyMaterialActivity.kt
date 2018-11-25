package com.xzaminer.app.studymaterial

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.Toolbar
import android.view.Window
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.views.MyGridLayoutManager
import com.xzaminer.app.R
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.data.User
import com.xzaminer.app.extensions.config
import com.xzaminer.app.extensions.dataSource
import com.xzaminer.app.result.QuestionsAnswerAdapter
import com.xzaminer.app.utils.COURSE_ID
import com.xzaminer.app.utils.SECTION_ID
import com.xzaminer.app.utils.STUDY_MATERIAL_ID
import com.xzaminer.app.utils.STUDY_MATERIAL_TYPE
import kotlinx.android.synthetic.main.activity_quiz.*



class StudyMaterialActivity : SimpleActivity() {

    private var studyMaterialId: Long = -1
    private var toolbar: Toolbar? = null
    private lateinit var user: User
    private var courseId: Long = -1
    private var sectionId: Long = -1
    private var studyMaterialType: String? = null

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
            studyMaterialId = getLongExtra(STUDY_MATERIAL_ID, -1)
            courseId = getLongExtra(COURSE_ID, -1)
            sectionId = getLongExtra(SECTION_ID, -1)
            studyMaterialType = getStringExtra(STUDY_MATERIAL_TYPE)
            if(studyMaterialType == null || courseId == (-1).toLong() || sectionId == (-1).toLong() || studyMaterialId == (-1).toLong()) {
                toast("Error opening Study Material")
                finish()
                return
            }
        }
        user = config.getLoggedInUser() as User
        setupGridLayoutManager()

        dataSource.getCourseStudyMaterialById(courseId, sectionId, studyMaterialId) { studyMaterial ->
            if(studyMaterial != null) {
                loadStudyMaterial(studyMaterial)
            } else {
                toast("Error opening Study Material")
                finish()
                return@getCourseStudyMaterialById
            }
        }
    }

    private fun loadStudyMaterial(studyMaterial: StudyMaterial) {
        if (studyMaterial.questions.size <= 0) {
            toast("Error opening Study Material. No Questions in this Study Material")
            finish()
            return
        }

        supportActionBar?.title = studyMaterial.name
        setupAdapter(studyMaterial.questions)
    }

    private fun setupGridLayoutManager() {
        val layoutManager = quiz_grid.layoutManager as MyGridLayoutManager
        layoutManager.orientation = GridLayoutManager.VERTICAL
        layoutManager.spanCount = 1
    }

    private fun setupAdapter(questions: ArrayList<Question>) {
        val currAdapter = quiz_grid.adapter
        if (currAdapter == null) {
            StudyMaterialAdapter(this, questions.clone() as ArrayList<Question>, quiz_grid) {
            }.apply {
                quiz_grid.adapter = this
            }
        } else {
            (currAdapter as QuestionsAnswerAdapter).updateQuestions(questions)
        }
    }
}

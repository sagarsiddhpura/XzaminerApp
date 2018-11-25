package com.xzaminer.app.course

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.Toolbar
import android.view.Window
import com.simplemobiletools.commons.extensions.beGone
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.views.MyGridLayoutManager
import com.simplemobiletools.commons.views.MyRecyclerView
import com.xzaminer.app.R
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.data.User
import com.xzaminer.app.extensions.config
import com.xzaminer.app.extensions.dataSource
import com.xzaminer.app.studymaterial.QuizActivity
import com.xzaminer.app.studymaterial.StudyMaterial
import com.xzaminer.app.studymaterial.StudyMaterialActivity
import com.xzaminer.app.utils.*
import kotlinx.android.synthetic.main.activity_course_section.*


class CourseSectionActivity : SimpleActivity() {

    private var toolbar: Toolbar? = null
    private lateinit var user: User
    private var courseId: Long = -1
    private var sectionId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_ACTION_MODE_OVERLAY)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_section)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar?.setNavigationOnClickListener { onBackPressed() }

        intent.apply {
            courseId = getLongExtra(COURSE_ID, -1)
            sectionId = getLongExtra(SECTION_ID, -1)
            if(courseId == (-1).toLong() || sectionId == (-1).toLong()) {
                toast("Error opening Section")
                finish()
                return
            }
        }
        user = config.getLoggedInUser() as User

        dataSource.getCourseById(courseId) { course ->
            if(course != null && course.sections[sectionId.toString()] != null) {
                supportActionBar?.title = course.name
                loadSection(course.sections[sectionId.toString()]!!)
            } else {
                toast("Error opening course.")
                finish()
                return@getCourseById
            }
        }
    }

    private fun loadSection(loadedSection: CourseSection) {
        section_title.text = loadedSection.name
        if(loadedSection.description != null && loadedSection.description != "") {
            section_desc.text = loadedSection.description
        } else {
            section_desc.beGone()
            divider_desc_courses.beGone()
        }
        setupAdapter(section_rv, loadedSection)
    }

    private fun setupAdapter(recyclerView: MyRecyclerView, section: CourseSection) {
        val values = ArrayList(section.studyMaterials.values)
        values.sortWith(compareBy { it.id })

        if(section.type == STUDY_MATERIAL_TYPE_STUDY_MATERIAL) {
            CourseStudyMaterialsAdapter(this, values.clone() as ArrayList<StudyMaterial>, recyclerView, GridLayoutManager.VERTICAL) {
                Intent(this, StudyMaterialActivity::class.java).apply {
                    putExtra(STUDY_MATERIAL_ID, (it as StudyMaterial).id)
                    putExtra(COURSE_ID, courseId)
                    putExtra(SECTION_ID, section.id)
                    putExtra(STUDY_MATERIAL_TYPE, section.type)
                    startActivity(this)
                }
            }.apply {
                recyclerView.adapter = this
            }
        }
        if(section.type == STUDY_MATERIAL_TYPE_QUESTION_BANK) {
            CourseQuestionBanksAdapter(this, values.clone() as ArrayList<StudyMaterial>, recyclerView, GridLayoutManager.VERTICAL) {
                Intent(this, QuizActivity::class.java).apply {
                    putExtra(QUIZ_ID, (it as StudyMaterial).id)
                    putExtra(COURSE_ID, courseId)
                    putExtra(SECTION_ID, section.id)
                    putExtra(IS_NEW_QUIZ, true)
                    startActivity(this)
                }
            }.apply {
                recyclerView.adapter = this
            }
        }
        val layoutManager = recyclerView.layoutManager as MyGridLayoutManager
        layoutManager.orientation = GridLayoutManager.VERTICAL
        layoutManager.spanCount = 3
    }
}

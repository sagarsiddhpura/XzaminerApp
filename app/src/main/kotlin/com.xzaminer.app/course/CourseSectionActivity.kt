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
import com.xzaminer.app.extensions.config
import com.xzaminer.app.extensions.dataSource
import com.xzaminer.app.studymaterial.QuizActivity
import com.xzaminer.app.studymaterial.StudyMaterial
import com.xzaminer.app.studymaterial.StudyMaterialActivity
import com.xzaminer.app.user.User
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
                supportActionBar?.title = course.shortName ?: course.name
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
        if(loadedSection.desc != null && loadedSection.desc != "") {
            section_desc.text = loadedSection.desc
        } else {
            section_desc.beGone()
            divider_title_desc.beGone()
        }
        setupAdapter(section_rv, loadedSection)
    }

    private fun setupAdapter(recyclerView: MyRecyclerView, section: CourseSection) {
        val values = ArrayList(section.studyMaterials.values)
        values.sortWith(compareBy { it.id })

        CourseStudyMaterialsAdapter(this, values.clone() as ArrayList<StudyMaterial>, recyclerView, GridLayoutManager.VERTICAL) {
            if(it is StudyMaterial && it.type == STUDY_MATERIAL_TYPE_STUDY_MATERIAL) {
                Intent(this, StudyMaterialActivity::class.java).apply {
                    putExtra(STUDY_MATERIAL_ID, (it as StudyMaterial).id)
                    putExtra(COURSE_ID, courseId)
                    putExtra(SECTION_ID, section.id)
                    putExtra(STUDY_MATERIAL_TYPE, section.type)
                    startActivity(this)
                }
            } else if((it as StudyMaterial).type == STUDY_MATERIAL_TYPE_QUESTION_BANK ) {
                Intent(this, QuizActivity::class.java).apply {
                    putExtra(QUIZ_ID, (it as StudyMaterial).id)
                    putExtra(SECTION_ID, section.id)
                    putExtra(COURSE_ID, courseId)
                    putExtra(IS_NEW_QUIZ, true)
                    startActivity(this)
                }
            } else if((it).type == STUDY_MATERIAL_TYPE_VIDEO ) {
                Intent(this, CourseSectionVideosDomainActivity::class.java).apply {
                    putExtra(DOMAIN_ID, (it).id)
                    putExtra(SECTION_ID, section.id)
                    putExtra(COURSE_ID, courseId)
                    startActivity(this)
                }
            }
        }.apply {
            recyclerView.adapter = this
            addVerticalDividers(true)
        }
        val layoutManager = recyclerView.layoutManager as MyGridLayoutManager
        layoutManager.orientation = GridLayoutManager.VERTICAL
        layoutManager.spanCount = 1
    }
}

package com.xzaminer.app.course

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.Toolbar
import android.view.Window
import com.simplemobiletools.commons.extensions.beVisible
import com.simplemobiletools.commons.extensions.getAdjustedPrimaryColor
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.views.MyGridLayoutManager
import com.simplemobiletools.commons.views.MyRecyclerView
import com.xzaminer.app.R
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.data.User
import com.xzaminer.app.extensions.config
import com.xzaminer.app.extensions.dataSource
import com.xzaminer.app.quiz.QuestionBank
import com.xzaminer.app.studymaterial.StudyMaterial
import com.xzaminer.app.studymaterial.StudyMaterialActivity
import com.xzaminer.app.utils.*
import kotlinx.android.synthetic.main.activity_course.*


class CourseActivity : SimpleActivity() {

    private var toolbar: Toolbar? = null
    private lateinit var user: User
    private var courseId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_ACTION_MODE_OVERLAY)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar?.setNavigationOnClickListener { onBackPressed() }

        intent.apply {
            courseId = getLongExtra(COURSE_ID, -1)
            if(courseId == (-1).toLong()) {
                toast("Error Course")
                finish()
                return
            }
        }
        user = config.getLoggedInUser() as User

        dataSource.getCourseById(courseId) { course ->
            if(course != null) {
                supportActionBar?.title = course.name
                loadCourse(course)
            } else {
                toast("Error opening course.")
                finish()
                return@getCourseById
            }
        }
    }

    private fun loadCourse(loadedCourse: Course) {
        course_title.text = loadedCourse.name
        course_desc.text = loadedCourse.description

        concepts_title.setTextColor(getAdjustedPrimaryColor())
        question_banks_title.setTextColor(getAdjustedPrimaryColor())
        manuals_title.setTextColor(getAdjustedPrimaryColor())
        flash_title.setTextColor(getAdjustedPrimaryColor())

        if(!loadedCourse.concepts.isEmpty()) {
            concepts_title.beVisible()
            concepts_root.beVisible()
            setupAdapter(concepts_grid, ArrayList(loadedCourse.concepts.values), STUDY_MATERIAL_TYPE_CONCEPT)
        }
        if(!loadedCourse.questionBanks.isEmpty()) {
            question_banks_title.beVisible()
            question_banks_root.beVisible()
            setupAdapter(question_banks_grid, ArrayList(loadedCourse.questionBanks.values))
        }
        if(!loadedCourse.reviewManuals.isEmpty()) {
            manuals_root.beVisible()
            manuals_title.beVisible()
            setupAdapter(manuals_grid, ArrayList(loadedCourse.reviewManuals.values), STUDY_MATERIAL_TYPE_REVIEW)
        }
        if(!loadedCourse.flashCards.isEmpty()) {
            flash_root.beVisible()
            flash_title.beVisible()
            setupAdapter(flash_grid, ArrayList(loadedCourse.flashCards.values), STUDY_MATERIAL_TYPE_FLASH)
        }
    }

    private fun setupAdapter(recyclerView: MyRecyclerView, values: ArrayList<StudyMaterial>, type: String) {
        values.sortWith(compareBy { it.id })
        CourseStudyMaterialsAdapter(this, values.clone() as ArrayList<StudyMaterial>, recyclerView) {
            Intent(this, StudyMaterialActivity::class.java).apply {
                putExtra(STUDY_MATERIAL_ID, (it as StudyMaterial).id)
                putExtra(COURSE_ID, courseId)
                putExtra(STUDY_MATERIAL_TYPE, type)
                startActivity(this)
            }
        }.apply {
            recyclerView.adapter = this
        }
        val layoutManager = recyclerView.layoutManager as MyGridLayoutManager
        layoutManager.orientation = GridLayoutManager.HORIZONTAL
        layoutManager.spanCount = 1
    }

    private fun setupAdapter(recyclerView: MyRecyclerView, values: ArrayList<QuestionBank>) {
        values.sortWith(compareBy { it.id })
        CourseQuestionBanksAdapter(this, values.clone() as ArrayList<QuestionBank>, recyclerView) {
            Intent(this, StudyMaterialActivity::class.java).apply {
                putExtra(QUIZ_ID, (it as QuestionBank).id)
                putExtra(IS_NEW_QUIZ, true)
                startActivity(this)
            }
        }.apply {
            recyclerView.adapter = this
        }
        val layoutManager = recyclerView.layoutManager as MyGridLayoutManager
        layoutManager.orientation = GridLayoutManager.HORIZONTAL
        layoutManager.spanCount = 1
    }
}

package com.xzaminer.app.admin

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutCompat
import android.support.v7.widget.Toolbar
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.TypedValue
import android.view.*
import android.widget.LinearLayout
import com.simplemobiletools.commons.extensions.beGone
import com.simplemobiletools.commons.extensions.beVisible
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.views.MyButton
import com.simplemobiletools.commons.views.MyGridLayoutManager
import com.simplemobiletools.commons.views.MyRecyclerView
import com.simplemobiletools.commons.views.MyTextView
import com.xzaminer.app.R
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.course.*
import com.xzaminer.app.extensions.dataSource
import com.xzaminer.app.studymaterial.ConfirmDialog
import com.xzaminer.app.studymaterial.StudyMaterial
import com.xzaminer.app.utils.*
import kotlinx.android.synthetic.main.activity_course.*
import ss.com.bannerslider.Slider


class ManageCourseActivity : SimpleActivity() {

    private var toolbar: Toolbar? = null
    private var courseId: Long = -1
    private var course: Course? = null

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
                toast("Error opening Course")
                finish()
                return
            }
        }

        dataSource.getCourseById(courseId) { course ->
            if(course != null) {
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
        supportActionBar?.title = loadedCourse.shortName ?: loadedCourse.name
        course = loadedCourse

         if(loadedCourse.descImages.isEmpty()) {
            desc_slider.beGone()
        } else {
            Slider.init(PicassoImageLoadingService(this))
            desc_slider.setAdapter(CourseDescriptionImageAdapter(loadedCourse.descImages))
        }

        initSections()
    }

    private fun initSections() {
        val sections = course!!.fetchAllSections()
        sections_root.removeAllViews()
        for(i in 0 until sections.size) {
            val section = sections[i]
            val view = LayoutInflater.from(this).inflate(R.layout.course_section_item2, null)

            sections_root.addView(view)
            val layoutParams = view.layoutParams as (LinearLayout.LayoutParams)
            val margin8Dp = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 8F, resources
                    .displayMetrics
            ).toInt()
            layoutParams.setMargins(0, margin8Dp, 0, margin8Dp)

            val title = view.findViewById<MyTextView>(R.id.section_title)
            val content = SpannableString(section.name)
            content.setSpan(UnderlineSpan(), 0, content.length, 0)
            title.text = content

            val rv = view.findViewById<MyRecyclerView>(R.id.section_rv)
            setupAdapter(rv, section)

            val sectionRoot = view.findViewById<View>(R.id.section_root)
            sectionRoot.setOnClickListener {
                toast("This functionality is being implemented")
            }

            view.findViewById<LinearLayoutCompat>(R.id.section_buy_root).beGone()
            view.findViewById<LinearLayoutCompat>(R.id.manage_root).beVisible()

            view.findViewById<MyButton>(R.id.manage_edit).setOnClickListener {
                if(section.id != -1L) {
                    // Section
                    Intent(this, EditSectionActivity::class.java).apply {
                        putExtra(COURSE_ID, courseId)
                        putExtra(SECTION_ID, section.id)
                        startActivity(this)
                    }
                }
            }

            view.findViewById<MyButton>(R.id.manage_delete).setOnClickListener {
                if(section.id != -1L) {
                    ConfirmDialog(this, "Are you sure you want to Delete this Section?") {
                        dataSource.deleteCourseSection(courseId, section)
                        dataSource.getCourseById(courseId) { course ->
                            if(course != null) {
                                loadCourse(course)
                            } else {
                                toast("Error opening course.")
                                finish()
                                return@getCourseById
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupAdapter(recyclerView: MyRecyclerView, section: CourseSection) {
        val values = ArrayList(section.studyMaterials.values)
        values.sortWith(compareBy { it.order })

        CourseStudyMaterialsAdapter(this, values.clone() as ArrayList<StudyMaterial>, recyclerView, GridLayoutManager.HORIZONTAL) {
            if(it is StudyMaterial && it.type == STUDY_MATERIAL_TYPE_STUDY_MATERIAL) {
                Intent(this, EditQuizActivity::class.java).apply {
                    putExtra(QUIZ_ID, (it).id)
                    putExtra(SECTION_ID, section.id)
                    putExtra(COURSE_ID, courseId)
                    startActivity(this)
                }
            } else if((it as StudyMaterial).type == STUDY_MATERIAL_TYPE_QUESTION_BANK ) {
                Intent(this, EditQuizActivity::class.java).apply {
                    putExtra(QUIZ_ID, (it).id)
                    putExtra(SECTION_ID, section.id)
                    putExtra(COURSE_ID, courseId)
                    startActivity(this)
                }
            } else if((it).type == STUDY_MATERIAL_TYPE_VIDEO ) {
                Intent(this, EditQuizActivity::class.java).apply {
                    putExtra(QUIZ_ID, (it).id)
                    putExtra(SECTION_ID, section.id)
                    putExtra(COURSE_ID, courseId)
                    startActivity(this)
                }
            }
        }.apply {
            recyclerView.adapter = this
        }
        val layoutManager = recyclerView.layoutManager as MyGridLayoutManager
        layoutManager.orientation = GridLayoutManager.HORIZONTAL
        layoutManager.spanCount = 1
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_add, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.manage_add -> addSection()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun addSection() {
        // Section
        Intent(this, EditSectionActivity::class.java).apply {
            putExtra(COURSE_ID, courseId)
            putExtra(SECTION_ID, 1L)
            putExtra(IS_NEW_QUIZ, true)
            startActivity(this)
        }
    }

    override fun onResume() {
        super.onResume()
        dataSource.getCourseById(courseId) { course ->
            if(course != null) {
                loadCourse(course)
            } else {
                toast("Error opening course.")
                finish()
                return@getCourseById
            }
        }
    }
}

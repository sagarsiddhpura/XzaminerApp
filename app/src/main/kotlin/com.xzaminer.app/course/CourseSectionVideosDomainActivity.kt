package com.xzaminer.app.course

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.Toolbar
import android.view.Window
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.views.MyGridLayoutManager
import com.simplemobiletools.commons.views.MyRecyclerView
import com.xzaminer.app.R
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.billing.ShowPurchasesActivity
import com.xzaminer.app.data.User
import com.xzaminer.app.extensions.config
import com.xzaminer.app.extensions.dataSource
import com.xzaminer.app.studymaterial.StudyMaterial
import com.xzaminer.app.studymaterial.Video
import com.xzaminer.app.studymaterial.VideoActivity
import com.xzaminer.app.utils.*
import kotlinx.android.synthetic.main.activity_course_section.*


class CourseSectionVideosDomainActivity : SimpleActivity() {

    private var toolbar: Toolbar? = null
    private lateinit var user: User
    private var courseId: Long = -1
    private var sectionId: Long = -1
    private var domainId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_ACTION_MODE_OVERLAY)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_section_video_domain)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar?.setNavigationOnClickListener { onBackPressed() }

        intent.apply {
            courseId = getLongExtra(COURSE_ID, -1)
            sectionId = getLongExtra(SECTION_ID, -1)
            domainId = getLongExtra(DOMAIN_ID, -1)
            if(courseId == (-1).toLong() || sectionId == (-1).toLong() || domainId == (-1).toLong()) {
                showErrorAndExit()
                return
            }
        }
        user = config.getLoggedInUser() as User

        dataSource.getCourseById(courseId) { course ->
            if(course != null) {
                val section = course.fetchSection(sectionId)
                if(section != null) {
                    val studyMaterial = section.fetchStudyMaterialById(domainId)
                    if(studyMaterial != null) {
                        val studyMaterialPurchased = user.isStudyMaterialPurchased(course, section, studyMaterial)
                        if(studyMaterialPurchased) {
                            loadDomain(studyMaterial)
                        } else {
                            // show purchase popup
                            Intent(this, ShowPurchasesActivity::class.java).apply {
                                putExtra(COURSE_ID, courseId)
                                putExtra(SECTION_ID, sectionId)
                                putExtra(STUDY_MATERIAL_ID, domainId)
                                startActivity(this)
                            }
                            finish()
                            return@getCourseById
                        }
                    } else {
                        showErrorAndExit()
                        return@getCourseById
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
    }

    private fun showErrorAndExit() {
        toast("Error opening Domain")
        finish()
    }

    private fun loadDomain(loadedSection: StudyMaterial) {
        section_title.text = loadedSection.name
        setupAdapter(section_rv, loadedSection)
    }

    private fun setupAdapter(recyclerView: MyRecyclerView, studyMaterial: StudyMaterial) {
        val values = ArrayList(studyMaterial.videos)
        values.sortWith(compareBy { it.order })

        CourseSectionDomainVideosAdapter(this, values.clone() as ArrayList<Video>, recyclerView) {
            Intent(this, VideoActivity::class.java).apply {
                putExtra(COURSE_ID, courseId)
                putExtra(SECTION_ID, sectionId)
                putExtra(DOMAIN_ID, domainId)
                putExtra(VIDEO_ID, (it as Video).id)
                startActivity(this)
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

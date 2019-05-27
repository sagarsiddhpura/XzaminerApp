package com.xzaminer.app.admin

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.views.MyGridLayoutManager
import com.simplemobiletools.commons.views.MyRecyclerView
import com.xzaminer.app.R
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.extensions.config
import com.xzaminer.app.extensions.dataSource
import com.xzaminer.app.studymaterial.ConfirmDialog
import com.xzaminer.app.studymaterial.SortEntitiesDialog
import com.xzaminer.app.studymaterial.StudyMaterial
import com.xzaminer.app.studymaterial.Video
import com.xzaminer.app.user.User
import com.xzaminer.app.utils.*
import kotlinx.android.synthetic.main.activity_course_section_video_domain.*


class ManageCourseSectionVideosDomainActivity : SimpleActivity() {

    private var toolbar: Toolbar? = null
    private lateinit var user: User
    private var courseId: Long = -1
    private var sectionId: Long = -1
    private var domainId: Long = -1
    private lateinit var values: ArrayList<Video>
    private var studyMaterial: StudyMaterial? = null

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
        supportActionBar?.title = "Manage Videos"

        intent.apply {
            courseId = getLongExtra(COURSE_ID, -1)
            sectionId = getLongExtra(SECTION_ID, -1)
            domainId = getLongExtra(QUIZ_ID, -1)
            if(courseId == (-1).toLong() || sectionId == (-1).toLong() || domainId == (-1).toLong()) {
                showErrorAndExit()
                return
            }
        }
        user = config.getLoggedInUser() as User
        loadDomain()
    }

    private fun loadDomain() {
        dataSource.getCourseById(courseId) { course ->
            if(course != null) {
                val section = course.fetchSection(sectionId)
                if(section != null) {
                    studyMaterial = section.fetchStudyMaterialById(domainId)
                    if(studyMaterial != null) {
                        loadStudyMaterial(studyMaterial!!)
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

    private fun loadStudyMaterial(loadedSection: StudyMaterial) {
        section_title.text = loadedSection.name
        setupAdapter(section_rv, loadedSection)
    }

    private fun setupAdapter(recyclerView: MyRecyclerView, studyMaterial: StudyMaterial) {
        values = ArrayList(studyMaterial.videos)
        values.sortWith(compareBy { it.order })

        val currAdapter = recyclerView.adapter
        if (currAdapter == null) {
            ManageCourseSectionDomainVideosAdapter(this, values.clone() as ArrayList<Video>, recyclerView) {
                Intent(this, EditVideoActivity::class.java).apply {
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
        } else {
            (currAdapter as ManageCourseSectionDomainVideosAdapter).updateVideos(values)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_manage, menu)
        menu.apply {
            findItem(R.id.manage_add).isVisible = true
            findItem(R.id.manage_re_order).isVisible = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.manage_add -> addVideo()
            R.id.manage_finish -> validateAndSaveEntity()
            R.id.manage_re_order -> reorderEntities()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun addVideo() {
        Intent(this, EditVideoActivity::class.java).apply {
            putExtra(COURSE_ID, courseId)
            putExtra(SECTION_ID, sectionId)
            putExtra(DOMAIN_ID, domainId)
            putExtra(VIDEO_ID, 1L)
            putExtra(IS_NEW_QUIZ, true)
            startActivity(this)
        }
    }

    fun editVideo(video: Video) {
        Intent(this, EditVideoActivity::class.java).apply {
            putExtra(COURSE_ID, courseId)
            putExtra(SECTION_ID, sectionId)
            putExtra(DOMAIN_ID, domainId)
            putExtra(VIDEO_ID, video.id)
            startActivity(this)
        }
    }

    fun deleteVideo(video: Video) {
        ConfirmDialog(this, "Are you sure you want to Delete this Video?") {
            values = values.filter {  it.id != video.id } as ArrayList<Video>
            (section_rv.adapter as ManageCourseSectionDomainVideosAdapter).updateVideos(values)
        }
    }

    private fun validateAndSaveEntity() {
        ConfirmDialog(this, "Are you sure you want to update the Video Domain?") {
            studyMaterial!!.videos = values
            dataSource.updateVideoProperties(courseId, sectionId, studyMaterial!!)
            ConfirmationDialog(this, "Video Domain has been updated successfully", R.string.yes, R.string.ok, 0) { }
        }
    }

    override fun onBackPressed() {
        ConfirmationDialog(this, "Are you sure you want to exit? All unsaved changes will be lost", R.string.yes, R.string.ok, 0) {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        loadDomain()
    }

    private fun reorderEntities() {
        val list = arrayListOf<String>()
        values.forEach {
            if(it.name != null) {
                list.add(it.name!!)
            }
        }
        SortEntitiesDialog(this, list) {
            it.forEachIndexed { index, entity ->
                val updatedEntity = values?.find { it.name == entity }
                if (updatedEntity != null) {
                    updatedEntity.order = index + 1
                }
            }
            values.sortWith(compareBy { it.order })
            dataSource.updateVideoProperties(courseId, sectionId, studyMaterial!!)
            toast("Videos re-ordering saved successfully")
            (section_rv.adapter as ManageCourseSectionDomainVideosAdapter).updateVideos(values)
        }
    }
}

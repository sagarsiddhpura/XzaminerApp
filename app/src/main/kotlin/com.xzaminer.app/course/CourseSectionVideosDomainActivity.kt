package com.xzaminer.app.course

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.Toolbar
import android.view.Window
import com.google.firebase.storage.FirebaseStorage
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.views.MyGridLayoutManager
import com.simplemobiletools.commons.views.MyRecyclerView
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.DownloadBlock
import com.tonyodev.fetch2core.Extras
import com.tonyodev.fetch2core.Func
import com.tonyodev.fetch2core.MutableExtras
import com.xzaminer.app.R
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.billing.ShowPurchasesActivity
import com.xzaminer.app.extensions.config
import com.xzaminer.app.extensions.dataSource
import com.xzaminer.app.extensions.getXzaminerDataDir
import com.xzaminer.app.studymaterial.StudyMaterial
import com.xzaminer.app.studymaterial.Video
import com.xzaminer.app.studymaterial.VideoActivity
import com.xzaminer.app.user.User
import com.xzaminer.app.utils.*
import kotlinx.android.synthetic.main.activity_course_section.*
import java.io.File




class CourseSectionVideosDomainActivity : SimpleActivity() {

    private var toolbar: Toolbar? = null
    private lateinit var user: User
    private var courseId: Long = -1
    private var sectionId: Long = -1
    private var domainId: Long = -1
    private lateinit var fetch: Fetch
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
                    studyMaterial = section.fetchStudyMaterialById(domainId)
                    if(studyMaterial != null) {
                        val studyMaterialPurchased = user.isStudyMaterialPurchased(course, section, studyMaterial!!)
                        if(studyMaterialPurchased) {
                            loadStudyMaterial(studyMaterial!!)
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

        val fetchConfiguration = FetchConfiguration.Builder(this)
            .setDownloadConcurrentLimit(3)
//            .setNotificationManager(DefaultFetchNotificationManager(this))
            .build()

        fetch = Fetch.getInstance(fetchConfiguration)
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
        } else {
            (currAdapter as CourseSectionDomainVideosAdapter).updateVideos(values)
        }
    }

    fun addDownload(video: Video) {
        toast("Preparing Download....")
        FirebaseStorage.getInstance().getReference(video.url + video.fileName).downloadUrl.addOnSuccessListener {

            val request = Request(it.toString(), fetchDataDirFile(getXzaminerDataDir(), "videos/" + video.fileName + "_temp").absolutePath)
            request.priority = Priority.HIGH
            request.extras = getExtrasForRequest(video)
            request.networkType = NetworkType.ALL
            if(!fetchDataDirFile(getXzaminerDataDir(), "videos/" + video.fileName + "_temp").exists()) {
                request.enqueueAction = EnqueueAction.REPLACE_EXISTING
            }

            fetch.enqueue(request, Func { updatedRequest ->

            }, Func { error ->
                toast("Error adding download to queue" + error.name)
            })

            val fetchListener = object : FetchListener {
                override fun onAdded(download: Download) {
                }

                override fun onDownloadBlockUpdated(
                    download: Download,
                    downloadBlock: DownloadBlock,
                    totalBlocks: Int
                ) {
                }

                override fun onError(download: Download, error: Error, throwable: Throwable?) {
                    val error = download.error
                    toast("Error downloading. Error:" + error.name)
                }

                override fun onStarted(download: Download, downloadBlocks: List<DownloadBlock>, totalBlocks: Int) {
                    toast("Starting downloading for " + download.extras.getString(VIDEO_DOWNLOAD_NAME, ""))
                }

                override fun onWaitingNetwork(download: Download) {
                }

                override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
                }

                override fun onCompleted(download: Download) {
                    toast("Download complete for " + download.extras.getString(VIDEO_DOWNLOAD_NAME, ""))
                    val name = download.extras.getString(VIDEO_DOWNLOAD_NAME, "")
                    val videoFound = values.find { it -> it.name == name }
                    videoFound!!.details.remove(VIDEO_DOWNLOAD_PROGRESS)
                    setupAdapter(section_rv, studyMaterial!!)
                    val tempFile = File(download.file)
                    val destFile =
                        File(fetchDataDirFile(getXzaminerDataDir(), "videos/" + videoFound.fileName).absolutePath)
                    if(tempFile.exists() && !destFile.exists()) {
                        tempFile.copyTo(destFile)
                    }
                    setupAdapter(section_rv, studyMaterial!!)
                }

                override fun onProgress(
                    download: Download, etaInMilliSeconds: Long,
                    downloadedBytesPerSecond: Long
                ) {
                    val name = download.extras.getString(VIDEO_DOWNLOAD_NAME, "")
                    val videoFound = values.find { it -> it.name == name }
                    videoFound!!.details[VIDEO_DOWNLOAD_PROGRESS] = arrayListOf(download.progress.toString() + " %")
                    setupAdapter(section_rv, studyMaterial!!)
                }

                override fun onPaused(download: Download) {
                }

                override fun onResumed(download: Download) {
                }

                override fun onCancelled(download: Download) {
                    toast("Download cancelled for " + download.extras.getString(VIDEO_DOWNLOAD_NAME, ""))
                    val name = download.extras.getString(VIDEO_DOWNLOAD_NAME, "")
                    val videoFound = values.find { it -> it.name == name }
                    videoFound!!.details.remove(VIDEO_DOWNLOAD_PROGRESS)
                    File(download.file).delete()
                    setupAdapter(section_rv, studyMaterial!!)
                }

                override fun onRemoved(download: Download) {

                }

                override fun onDeleted(download: Download) {

                }
            }
            fetch.addListener(fetchListener)
        }.addOnFailureListener {
            toast("Failed to download file. Error:" + it.message)
        }

    }

    private fun getExtrasForRequest(video: Video): Extras {
        val extras = MutableExtras()
        extras.putString(VIDEO_DOWNLOAD_NAME, video.name)
        return extras
    }

    fun cancelDownload(video: Video) {
        //Get all downloads with a status
        fetch.getDownloadsWithStatus(Status.DOWNLOADING, Func {
            it.forEach {
                if(it.extras.getString(VIDEO_DOWNLOAD_NAME, "") == video.name) {
                    fetch.cancel(it.id)
                }
            }
        })
    }
}

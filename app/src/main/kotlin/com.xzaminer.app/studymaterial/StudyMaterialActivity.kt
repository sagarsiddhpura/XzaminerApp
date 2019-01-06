package com.xzaminer.app.studymaterial

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.Toolbar
import android.view.Window
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mikkipastel.videoplanet.player.PlaybackStatus
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.views.MyGridLayoutManager
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import com.xzaminer.app.R
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.billing.ShowPurchasesActivity
import com.xzaminer.app.extensions.config
import com.xzaminer.app.extensions.dataSource
import com.xzaminer.app.user.User
import com.xzaminer.app.utils.*
import kotlinx.android.synthetic.main.activity_quiz.*



class StudyMaterialActivity : SimpleActivity() {

    private var studyMaterialId: Long = -1
    private var toolbar: Toolbar? = null
    private lateinit var user: User
    private var courseId: Long = -1
    private var sectionId: Long = -1
    private var studyMaterialType: String? = null
    lateinit var bus: Bus
    private lateinit var questions: ArrayList<Question>

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
                showErrorAndExit()
                return
            }
        }
        bus = BusProvider.instance
        bus.register(this)
        user = config.getLoggedInUser() as User
        setupGridLayoutManager()

        dataSource.getCourseById(courseId) { course ->
            if(course != null) {
                val section = course.fetchSection(sectionId)
                if(section != null) {
                    val studyMaterial = section.fetchStudyMaterialById(studyMaterialId)
                    if(studyMaterial != null) {
                        val studyMaterialPurchased = user.isStudyMaterialPurchased(course, section, studyMaterial)
                        if(studyMaterialPurchased) {
                            loadStudyMaterial(studyMaterial)
                        } else {
                            // show purchase popup
                            Intent(this, ShowPurchasesActivity::class.java).apply {
                                putExtra(COURSE_ID, courseId)
                                putExtra(SECTION_ID, sectionId)
                                putExtra(STUDY_MATERIAL_ID, studyMaterialId)
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
        toast("Error opening Study Material.")
        finish()
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
        this.questions = questions
        val currAdapter = quiz_grid.adapter
        if (currAdapter == null) {
            StudyMaterialAdapter(this, questions.clone() as ArrayList<Question>, quiz_grid) {
            }.apply {
                quiz_grid.adapter = this
            }
        } else {
            (currAdapter as StudyMaterialAdapter).updateQuestions(questions)
        }
    }

    fun handleAudioPlayback(audio: Video) {
        if(audio.details[AUDIO_PLAYBACK_STATE] != null && !audio.details[AUDIO_PLAYBACK_STATE]!!.isEmpty()
            && audio.details[AUDIO_PLAYBACK_STATE]!!.first() == PlaybackStatus.PLAYING) {
            Intent(this, AudioPlayerService::class.java).apply {
                action = ACTION_PAUSE
                startService(this)
            }
        } else if(audio.details[AUDIO_PLAYBACK_STATE] != null && !audio.details[AUDIO_PLAYBACK_STATE]!!.isEmpty()
            && audio.details[AUDIO_PLAYBACK_STATE]!!.first() == PlaybackStatus.PAUSED) {
            Intent(this, AudioPlayerService::class.java).apply {
                action = ACTION_PLAY
                startService(this)
            }
        } else {
            toast("Starting audio playback for " + audio.name + "...")
            val s = audio.url + audio.fileName
            FirebaseStorage.getInstance().getReference(s).downloadUrl.addOnSuccessListener { uri ->
                audio.details[DOWNLOAD_URL] = arrayListOf(uri.toString())
                val listType = object : TypeToken<Video>() {}.type
                val json = Gson().toJson(audio, listType)

                Intent(this, AudioPlayerService::class.java).apply {
                    putExtra(PLAYER_AUDIOBOOK, json)
                    action = ACTION_INIT_PLAY
                    startService(this)
                }

            }.addOnFailureListener {
                toast("Error playing Audio")
            }
        }
    }

    @Subscribe
    fun songStateChanged(event: Events.playBackStateChanged) {
        val state = event.state
        val audio = event.audio
        if(audio != null) {
            var questionId = audio.details[QUESTION_ID]?.first()
            if(questionId != null) {
                val currentQuestion = questions.find { it.id.toString() == questionId } as Question

                // Mark change
                if(!currentQuestion.audios.isEmpty()) {
                    val audioQuestion = currentQuestion.audios.first()
                    audioQuestion.details[AUDIO_PLAYBACK_STATE] = arrayListOf(state)
                    // Refresh Questions
                    setupAdapter(questions)
                }
            }
        }
    }

    override fun onDestroy() {
        bus.unregister(this)
        super.onDestroy()
    }
}

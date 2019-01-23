package com.xzaminer.app.studymaterial

import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.Window
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.firebase.storage.FirebaseStorage
import com.simplemobiletools.commons.extensions.beGone
import com.simplemobiletools.commons.extensions.beVisible
import com.simplemobiletools.commons.extensions.toast
import com.xzaminer.app.R
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.extensions.config
import com.xzaminer.app.extensions.dataSource
import com.xzaminer.app.extensions.getXzaminerDataDir
import com.xzaminer.app.user.User
import com.xzaminer.app.utils.*
import kotlinx.android.synthetic.main.activity_intro.*
import kotlinx.android.synthetic.main.exo_playback_control_view.*
import java.io.File
import android.view.WindowManager
import android.view.Display
import android.content.pm.ActivityInfo
import android.R.attr.orientation
import android.content.Context
import android.content.res.Configuration


class VideoActivity : SimpleActivity() {

    private var videoId: Long = -1
    private var toolbar: Toolbar? = null
    private lateinit var user: User
    private var courseId: Long = -1
    private var sectionId: Long = -1
    private var domainId: Long = -1
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_ACTION_MODE_OVERLAY)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar?.setNavigationOnClickListener { onBackPressed() }

        intent.apply {
            courseId = getLongExtra(COURSE_ID, -1)
            sectionId = getLongExtra(SECTION_ID, -1)
            domainId = getLongExtra(DOMAIN_ID, -1)
            videoId = getLongExtra(VIDEO_ID, -1)
            if (domainId == (-1).toLong() || courseId == (-1).toLong() || sectionId == (-1).toLong() || videoId == (-1).toLong()) {
                toast("Error opening Video")
                finish()
                return
            }
        }
        user = config.getLoggedInUser() as User

        video_view.beVisible()
        intro_holder.beGone()

        dataSource.getCourseStudyMaterialById(courseId, sectionId, domainId) { studyMaterial ->
            val video = studyMaterial?.fetchVideo(videoId)
            if (video != null) {
                val videoFile = File(fetchDataDirFile(getXzaminerDataDir(), "videos/" + video.fileName).absolutePath)
                if(videoFile.exists()) {
                    loadVideo(videoFile)
                } else {
                    FirebaseStorage.getInstance().getReference(video.url + video.fileName).downloadUrl.addOnSuccessListener { uri ->
                        loadVideo(uri)
                    }.addOnFailureListener {
                        toast("Error opening video")
                        finish()
                    }
                }
            } else {
                toast("Error opening Video")
                finish()
                return@getCourseStudyMaterialById
            }
        }

        exo_rotate.setOnClickListener {
            val display = (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
            val orientation = display.orientation

            when (orientation % 2) {
                0 -> {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }
                1 -> {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            }
        }
    }

    private fun loadVideo(video: Uri) {
        if (player == null) {
            player = ExoPlayerFactory.newSimpleInstance( this,
                DefaultRenderersFactory(this),
                DefaultTrackSelector(),
                DefaultLoadControl()
            )
            video_view?.player = player
            player!!.playWhenReady = true
            video_view!!.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
        val mediaSource = buildMediaSource(video)
        player!!.prepare(mediaSource, true, false)
    }

    private fun loadVideo(video: File) {
        if (player == null) {
            player = ExoPlayerFactory.newSimpleInstance( this,
                DefaultRenderersFactory(this),
                DefaultTrackSelector(),
                DefaultLoadControl()
            )
            video_view?.player = player
            player!!.playWhenReady = true
            video_view!!.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
        val mediaSource = buildMediaSource(video)
        player!!.prepare(mediaSource, true, false)
    }

    private fun buildMediaSource(video: Uri): MediaSource {
        return ExtractorMediaSource.Factory(DefaultHttpDataSourceFactory("Xzaminer"))
            .createMediaSource(video)
    }

    private fun buildMediaSource(video: File): MediaSource {
        return ExtractorMediaSource(Uri.fromFile(video), DefaultDataSourceFactory(this,"ua"),
         DefaultExtractorsFactory(),null,null);
//        return ExtractorMediaSource.Factory(DefaultHttpDataSourceFactory("Xzaminer"))
//            .createMediaSource(Uri.fromFile(video))
    }

    private fun releasePlayer() {
        if (player != null) {
            player!!.release()
            player = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }
}

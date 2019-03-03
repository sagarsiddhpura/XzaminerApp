package com.xzaminer.app.studymaterial

import android.content.Context
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
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


class VideoActivity : SimpleActivity() {

    private var videoId: Long = -1
    private var toolbar: Toolbar? = null
    private lateinit var user: User
    private var courseId: Long = -1
    private var sectionId: Long = -1
    private var domainId: Long = -1
    private var player: ExoPlayer? = null
    private var concatenatingMediaSource: ConcatenatingMediaSource? = null
    private lateinit var studyMaterial: StudyMaterial

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

        dataSource.getCourseStudyMaterialById(courseId, sectionId, domainId) { studyMaterial_ ->
            if(studyMaterial_ != null && !studyMaterial_.fetchVisibleVideos().isEmpty() && studyMaterial_.fetchVideo(videoId) != null) {
                studyMaterial = studyMaterial_
                val video = studyMaterial_.fetchVideo(videoId) as Video
                val index = studyMaterial_.fetchVideoIndex(videoId)
                concatenatingMediaSource = ConcatenatingMediaSource()
                val videos = ArrayList(studyMaterial_.fetchVisibleVideos())

                val videoFile = File(fetchDataDirFile(getXzaminerDataDir(), "videos/" + video.fileName).absolutePath)
                for(i in 0..studyMaterial_.fetchVisibleVideos().size) {
                    buildMediaSource(videoFile, i)
                }

                if(videoFile.exists()) {
                    buildMediaSource(videoFile, index)
                    startPlayback(index)
                    buildPlaylist(videos)
                } else {
                    FirebaseStorage.getInstance().getReference(video.url + video.fileName).downloadUrl.addOnSuccessListener { uri ->
                        buildMediaSource(uri, index)
                        startPlayback(index)
                        buildPlaylist(videos)
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

    private fun buildPlaylist(videos: ArrayList<Video>) {
        // Load rest of videos to playlist
        videos.forEachIndexed { index, video ->
            if (video != null) {
                if(video.id != videoId) {
                    val videoFile = fetchDataDirFile(getXzaminerDataDir(), "videos/" + video.fileName)
                    if(videoFile.exists()) {
                        buildMediaSource(videoFile, index)
                    } else {
                        FirebaseStorage.getInstance().getReference(video.url + video.fileName).downloadUrl.addOnSuccessListener { uri ->
                            buildMediaSource(uri, index)
                        }.addOnFailureListener {
                            toast("Error loading playlist...")
//                                    finish()
                        }
                    }
                }
            } else {
                toast("Error opening Video")
                finish()
                return@forEachIndexed
            }
        }
    }

    private fun startPlayback(index: Int) {
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
        player!!.prepare(concatenatingMediaSource, true, false)
        player!!.seekTo(index, C.TIME_UNSET)
        player!!.addListener(object : Player.DefaultEventListener() {
            override fun onPlayerStateChanged(
                playWhenReady: Boolean,playbackState: Int) {
                when (playbackState) {
                    Player.STATE_IDLE -> {}
                    Player.STATE_BUFFERING -> {}
                    Player.STATE_READY -> {}
                    Player.STATE_ENDED -> {
                        checkAndPlayNextFile(index)
                    }
                }
            }
        })
    }

    private fun checkAndPlayNextFile(index: Int) {
        if(concatenatingMediaSource != null && concatenatingMediaSource!!.getMediaSource(index + 1) != null) {
            startPlayback(index+1)
        }
    }

    private fun buildMediaSource(video: Uri, index: Int): MediaSource {
        if(index < concatenatingMediaSource!!.size && concatenatingMediaSource?.getMediaSource(index) != null) {
            concatenatingMediaSource?.removeMediaSource(index)
        }
        concatenatingMediaSource?.addMediaSource(index, ExtractorMediaSource.Factory(DefaultHttpDataSourceFactory("Xzaminer"))
            .createMediaSource(video))
        return concatenatingMediaSource as ConcatenatingMediaSource
    }

    private fun buildMediaSource(video: File, index: Int): MediaSource {
        if(index < concatenatingMediaSource!!.size && concatenatingMediaSource?.getMediaSource(index) != null) {
            concatenatingMediaSource?.removeMediaSource(index)
        }
        concatenatingMediaSource?.addMediaSource(index, ExtractorMediaSource(Uri.fromFile(video), DefaultDataSourceFactory(this,"ua"),
            DefaultExtractorsFactory(),null,null))
        return concatenatingMediaSource as ConcatenatingMediaSource
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

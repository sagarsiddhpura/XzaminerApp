package com.xzaminer.app

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.Window
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.extractor.mp4.Mp4Extractor
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.simplemobiletools.commons.activities.BaseSplashActivity
import kotlinx.android.synthetic.main.activity_intro.*



class IntroActivity : BaseSplashActivity() {
    private var player: ExoPlayer? = null

    override fun initActivity() {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_ACTION_MODE_OVERLAY)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

//        initializePlayer()
        Handler().postDelayed({
            finish()
            return@postDelayed
        }, 2000)
    }

    private fun initializePlayer() {
        if (player == null) {
            player = ExoPlayerFactory.newSimpleInstance( this,
                DefaultRenderersFactory(this),
                DefaultTrackSelector(),
                DefaultLoadControl()
            )
            video_view?.player = player
            player!!.playWhenReady = true
            video_view.useController = false
            video_view!!.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

            player!!.addListener(object : ExoPlayer.EventListener {
                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    when (playbackState) {
                        ExoPlayer.STATE_ENDED -> {
                            finish()
                            return
                        }
                    }
                }

            })
        }
        val mediaSource = buildMediaSource()
        player!!.prepare(mediaSource, true, false)
    }

    private fun buildMediaSource(): MediaSource {
        val uri = RawResourceDataSource.buildRawResourceUri(R.raw.intro)
        val dataSource = RawResourceDataSource(this)
        dataSource.open(DataSpec(uri))

        return ExtractorMediaSource(uri, DataSource.Factory { dataSource }, Mp4Extractor.FACTORY, null, null)
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

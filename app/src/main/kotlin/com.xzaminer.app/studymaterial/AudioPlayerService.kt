package com.xzaminer.app.studymaterial

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.text.TextUtils
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mikkipastel.videoplanet.player.PlaybackStatus
import com.simplemobiletools.commons.helpers.isOreoPlus
import com.squareup.otto.Bus
import com.xzaminer.app.extensions.getXzaminerDataDir
import com.xzaminer.app.utils.*
import java.io.File

class AudioPlayerService : Service(), Player.EventListener {

    private val playerBind = PlayerBinder()
    lateinit var audio: Video
    lateinit var exoPlayer: SimpleExoPlayer
    var mediaSession: MediaSessionCompat? = null
    private var notificationManager: PlayerNotificationManager? = null
    private var transportControls: MediaControllerCompat.TransportControls? = null
    var status: String? = null
    private var streamUrl: String? = null
    private lateinit var service: AudioPlayerService
    private var mBus: Bus? = null

    private val mediasSessionCallback = object : MediaSessionCompat.Callback() {
        override fun onPause() {
            super.onPause()

            pause()
        }

        override fun onStop() {
            super.onStop()
            stop()
        }

        override fun onPlay() {
            super.onPlay()

            resume()
        }
    }

    val isPlaying: Boolean
        get() = this.status == PlaybackStatus.PLAYING

    protected inner class PlayerBinder : Binder() {
        internal val service: AudioPlayerService
            get() = this@AudioPlayerService
    }

    override fun onCreate() {
        super.onCreate()
        service = this

        if (mBus == null) {
            mBus = BusProvider.instance
            mBus!!.register(this)
        }

        notificationManager = PlayerNotificationManager(
            this,
            "Xzaminer",
            1337,
            DescriptionAdapter()
        )
        notificationManager!!.setNotificationListener(object : PlayerNotificationManager.NotificationListener {
            override fun onNotificationCancelled(notificationId: Int) {
                stopSelf()
            }

            override fun onNotificationStarted(notificationId: Int, notification: Notification?) {
                startForeground(notificationId, notification)
            }
        })

        mediaSession = MediaSessionCompat(this, javaClass.simpleName)
        transportControls = mediaSession!!.controller.transportControls
        mediaSession!!.isActive = true
        mediaSession!!.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        mediaSession!!.setCallback(mediasSessionCallback)

        val renderersFactory = DefaultRenderersFactory(applicationContext)
        val bandwidthMeter = DefaultBandwidthMeter()
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
        val loadControl = DefaultLoadControl()

        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, renderersFactory, trackSelector, loadControl)
        exoPlayer.addListener(this)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.CONTENT_TYPE_SPEECH)
            .build()
        exoPlayer.setAudioAttributes(audioAttributes, /* handleAudioFocus= */ true)
        notificationManager!!.setPlayer(exoPlayer)
        notificationManager!!.setUseNavigationActions(false)
        notificationManager!!.setMediaSessionToken(mediaSession!!.sessionToken)
        status = PlaybackStatus.IDLE
    }

    override fun onBind(intent: Intent): IBinder? {
        return playerBind
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        val action = intent.action

        if (TextUtils.isEmpty(action))
            return Service.START_STICKY

        if (action!!.equals(ACTION_INIT_PLAY, ignoreCase = true)) {
            val audioString = intent.getStringExtra(PLAYER_AUDIOBOOK)
            val type = object : TypeToken<Video>() {}.type
            this.audio = Gson().fromJson<Video>(audioString, type)
            if(this.audio.details[DOWNLOAD_URL] == null) {
                init(File(getXzaminerDataDir(), "audios/" + audio.fileName))
            } else {
                val url = this.audio.details[DOWNLOAD_URL]!!.first()
                init(url)
            }
//            Thread {
//            }.start()
        } else if (action.equals(ACTION_PLAY, ignoreCase = true)) {
            transportControls!!.play()

        } else if (action.equals(ACTION_PAUSE, ignoreCase = true)) {
            if (PlaybackStatus.STOPPED === status) {
                transportControls!!.stop()
            } else {
                transportControls!!.pause()
            }
        } else if (action.equals(ACTION_STOP, ignoreCase = true)) {
            pause()
        }

        return Service.START_STICKY
    }

    override fun onUnbind(intent: Intent): Boolean {
        if (status == PlaybackStatus.IDLE)
            stopSelf()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        pause()
        notificationManager?.setPlayer(null)
        exoPlayer.release()
        exoPlayer.removeListener(this)
        mediaSession!!.release()

        if (mBus != null) {
            mBus!!.post(Events.playBackStateChanged(PlaybackStatus.STOPPED, audio))
            mBus!!.unregister(this)
        }

        super.onDestroy()
    }

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {

    }

    override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {

    }

    override fun onLoadingChanged(isLoading: Boolean) {

    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when (playbackState) {
            Player.STATE_BUFFERING -> status = PlaybackStatus.LOADING
            Player.STATE_ENDED -> status = PlaybackStatus.STOPPED
            Player.STATE_IDLE -> status = PlaybackStatus.IDLE
            Player.STATE_READY -> status = if (playWhenReady) PlaybackStatus.PLAYING else PlaybackStatus.PAUSED
            else -> status = PlaybackStatus.IDLE
        }

        mBus!!.post(Events.playBackStateChanged(status!!, audio))
    }

    override fun onRepeatModeChanged(repeatMode: Int) {

    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {

    }

    override fun onPlayerError(error: ExoPlaybackException?) {
        mBus!!.post(Events.playBackStateChanged(PlaybackStatus.ERROR, audio))
    }

    override fun onPositionDiscontinuity(reason: Int) {
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
    }

    override fun onSeekProcessed() {
    }

    private fun buildMediaSource(uri: Uri): MediaSource {

        val userAgent = Util.getUserAgent(applicationContext, "Xzaminer")

        if (uri.lastPathSegment!!.contains("mp3") || uri.lastPathSegment!!.contains("mp4") || uri.lastPathSegment!!.contains("m4a")) {
            return ExtractorMediaSource.Factory(DefaultHttpDataSourceFactory(userAgent))
                .createMediaSource(uri)
        } else if (uri.lastPathSegment!!.contains("m3u8")) {
            return HlsMediaSource.Factory(DefaultHttpDataSourceFactory(userAgent))
                .createMediaSource(uri)
        } else {
            return ExtractorMediaSource.Factory(DefaultHttpDataSourceFactory(userAgent))
                .createMediaSource(uri)
//            val dashChunkSourceFactory = DefaultDashChunkSource.Factory(
//                DefaultHttpDataSourceFactory("ua", DefaultBandwidthMeter())
//            )
//            val manifestDataSourceFactory = DefaultHttpDataSourceFactory(userAgent)
//            return DashMediaSource.Factory(dashChunkSourceFactory, manifestDataSourceFactory).createMediaSource(uri)
        }
    }

    fun play() {
        exoPlayer.playWhenReady = true
    }

    fun pause() {
        exoPlayer.playWhenReady = false
    }

    fun resume() {
        if (streamUrl != null)
            play()
    }

    fun stop() {
        exoPlayer.stop()
    }

    @SuppressLint("NewApi")
    fun init(streamUrl_: String) {
        this.streamUrl = streamUrl_
        val mediaSource = buildMediaSource(Uri.parse(streamUrl_))
        exoPlayer.prepare(mediaSource)
        exoPlayer.playWhenReady = true

        if (isOreoPlus()) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val importance = NotificationManager.IMPORTANCE_LOW
            NotificationChannel("Xzaminer", "Xzaminer", importance).apply {
                enableLights(false)
                enableVibration(false)
                notificationManager.createNotificationChannel(this)
            }
        }
    }

    @SuppressLint("NewApi")
    fun init(audioFile: File) {
        this.streamUrl = audioFile.absolutePath
        val mediaSource = buildMediaSourceFile(audioFile)
        exoPlayer.prepare(mediaSource)
        exoPlayer.playWhenReady = true

        if (isOreoPlus()) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val importance = NotificationManager.IMPORTANCE_LOW
            NotificationChannel("Xzaminer", "Xzaminer", importance).apply {
                enableLights(false)
                enableVibration(false)
                notificationManager.createNotificationChannel(this)
            }
        }
    }

    private fun buildMediaSourceFile(audioFile: File): MediaSource? {
        return ExtractorMediaSource(Uri.fromFile(audioFile), DefaultDataSourceFactory(this,"ua"),
            DefaultExtractorsFactory(),null,null);
    }

    fun playOrPause(url: String) {
        if (streamUrl != null && streamUrl == url) {
            play()
        } else {
            init(url)
        }
    }

    private inner class DescriptionAdapter : PlayerNotificationManager.MediaDescriptionAdapter {

        override fun getCurrentContentTitle(player: Player): String {
            return audio.name
        }

        override fun getCurrentContentText(player: Player): String? {
            return audio.desc
        }

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            return BitmapFactory.decodeResource(getResources(), R.drawable.exo_controls_fastforward)
        }

        override fun createCurrentContentIntent(player: Player): PendingIntent? {
//            val intent = Intent(this@AudioPlayerService, SplashActivity::class.java)
//            val i = PendingIntent.getActivity(this@AudioPlayerService, 1331, intent, PendingIntent.FLAG_UPDATE_CURRENT)
//            return i
            return null
        }
    }
}
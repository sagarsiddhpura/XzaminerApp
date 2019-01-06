package com.xzaminer.app.studymaterial

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mikkipastel.videoplanet.player.PlaybackStatus
import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.extensions.beVisible
import com.simplemobiletools.commons.extensions.highlightTextPart
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.views.MyGridLayoutManager
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import com.xzaminer.app.R
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.billing.ShowPurchasesActivity
import com.xzaminer.app.extensions.config
import com.xzaminer.app.extensions.dataSource
import com.xzaminer.app.result.ResultActivity
import com.xzaminer.app.user.User
import com.xzaminer.app.utils.*
import kotlinx.android.synthetic.main.activity_quiz.*
import kotlin.concurrent.thread




class QuizActivity : SimpleActivity() {

    private var quizId: Long = -1
    private var toolbar: Toolbar? = null
    private lateinit var user: User
    private var isMarkedForLaterQuestionsShown: Boolean = false
    private var isNewQuiz: Boolean = true
    private var runningTimer: Long = -1L
    private var timer : CountDownTimer? = null
    private var courseId: Long = -1
    private var sectionId: Long = -1
    lateinit var bus: Bus

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
            quizId = getLongExtra(QUIZ_ID, -1)
            isNewQuiz = getBooleanExtra(IS_NEW_QUIZ, true)
            courseId = getLongExtra(COURSE_ID, -1)
            sectionId = getLongExtra(SECTION_ID, -1)
            if(quizId == (-1).toLong()) {
                showErrorAndExit()
                return
            }
        }
        bus = BusProvider.instance
        bus.register(this)

        user = config.getLoggedInUser() as User
        setupGridLayoutManager()

        if(isNewQuiz) {
//            dataSource.getQuestionBank(quizId) { loadedQuiz ->
//                loadQuestionBank(loadedQuiz)
//            }
            dataSource.getCourseById(courseId) { course ->
                if(course != null) {
                    val section = course.fetchSection(sectionId)
                    if(section != null) {
                        val studyMaterial = section.fetchStudyMaterialById(quizId)
                        if(studyMaterial != null) {
                            val studyMaterialPurchased = user.isStudyMaterialPurchased(course, section, studyMaterial)
                            if(studyMaterialPurchased) {
                                studyMaterial.id = System.nanoTime()
                                user.startQuiz(studyMaterial)
                                config.setLoggedInUser(user)
                                dataSource.addUser(user)
                                loadQuestionBank(studyMaterial)
                            } else {
                                // show purchase popup
                                Intent(this, ShowPurchasesActivity::class.java).apply {
                                    putExtra(COURSE_ID, courseId)
                                    putExtra(SECTION_ID, sectionId)
                                    putExtra(STUDY_MATERIAL_ID, quizId)
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
        } else {
            val loadedQuiz = user.quizzes[quizId.toString()]
            if(loadedQuiz != null && loadedQuiz.status == QB_STATUS_IN_PROGRESS) {
                loadedQuiz.updateLastAccessed()
                loadQuestionBank(loadedQuiz)
            } else {
                toast("Error Resuming Quiz.")
                finish()
                return
            }
        }
    }

    private fun showErrorAndExit() {
        toast("Error Opening Question Bank")
        finish()
    }

    private fun loadQuestionBank(loadedQuiz: StudyMaterial?) {
        if (loadedQuiz != null) {
            if (loadedQuiz.questions.size <= 0) {
                toast("Error Opening Question Bank. No Questions in this Question Bank")
                finish()
                return
            }

            supportActionBar?.title = loadedQuiz.name
            quizId = loadedQuiz.id
            setupAdapter(loadedQuiz.questions)
            setupTimer(loadedQuiz.fetchTotalOrResumeTimer())
        } else {
            toast("Error Opening Question Bank")
            finish()
        }
    }

    private fun setupTimer(timerSecs: Long?) {
        if(timerSecs != null) {
            quiz_timer_label.text = convertToText(timerSecs)
            quiz_timer_label.beVisible()

            timer = object : CountDownTimer(timerSecs*1000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val timeText = convertToText(millisUntilFinished / 1000)
                    if(millisUntilFinished < 120000) { quiz_timer_label.text = timeText.highlightTextPart(timeText, resources.getColor(R.color.md_red)) } else { quiz_timer_label.text = timeText }
                    runningTimer = millisUntilFinished / 1000
                }
                override fun onFinish() {
                    finishQuiz()
                }
            }
            (timer as CountDownTimer).start()
        }
    }

    private fun getRecyclerAdapter() = quiz_grid.adapter as? QuestionsAdapter

    private fun setupGridLayoutManager() {
        val layoutManager = quiz_grid.layoutManager as MyGridLayoutManager
        layoutManager.orientation = GridLayoutManager.VERTICAL
        layoutManager.spanCount = 1
    }

    private fun setupAdapter(questions: ArrayList<Question>) {
        val currAdapter = quiz_grid.adapter
        if (currAdapter == null) {
            QuestionsAdapter(
                this,
                questions.clone() as ArrayList<Question>,
                quiz_grid
            ) {
            }.apply {
                quiz_grid.adapter = this
            }
        } else {
            (currAdapter as QuestionsAdapter).updateQuestions(questions)
        }
    }

    fun optionClicked(question: Question, id: Long) {
        val questions = getCurrentQuizQuestionsFromUser()
        val currentQuestion = questions.find { it.id == question.id }
        if(currentQuestion!!.selectedAnswer == id) {
            currentQuestion.selectedAnswer = 0L
        } else {
            currentQuestion.selectedAnswer = id
            currentQuestion.isMarkedForLater = false
        }
        refreshQuestions(questions)
        thread { dataSource.addUser(user) }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_quiz, menu)
        menu.apply {
            findItem(R.id.show_all).isVisible = isMarkedForLaterQuestionsShown
            findItem(R.id.show_marked_later).isVisible = !isMarkedForLaterQuestionsShown
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.finish_quiz -> confirmAndFinishQuiz()
            R.id.show_marked_later -> showMarkedLaterQuestions()
            R.id.show_all -> showMarkedLaterQuestions()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    fun markForLater(question: Question) {
        val questions = getCurrentQuizQuestionsFromUser()
        val currentQuestion = questions.find { it.id == question.id } as Question
        // Mark change
        currentQuestion.isMarkedForLater = !currentQuestion.isMarkedForLater
        if(currentQuestion.isMarkedForLater) { currentQuestion.selectedAnswer = 0 }
        // Refresh Questions
        refreshQuestions(questions)
        toast( if(currentQuestion.isMarkedForLater) "Marking Question for Later" else "Question removed from Marked for Later" )
        dataSource.addUser(user)
    }

    private fun confirmAndFinishQuiz() {
        ConfirmDialog(this, "Are you sure you want to finish Quiz?") {
            finishQuiz()
        }
    }

    private fun finishQuiz() {
        getCurrentQuizFromUser()!!.status = QB_STATUS_FINISHED
        getCurrentQuizFromUser()!!.updateCompleted()
        config.setLoggedInUser(user)
        dataSource.addUser(user)
        Intent(this, ResultActivity::class.java).apply {
            putExtra(QUIZ_ID, quizId)
            startActivity(this)
        }
        finish()
    }

    private fun getCurrentQuizFromUser(): StudyMaterial? {
        return user.quizzes.values.find { it.id == quizId }
    }

    private fun showMarkedLaterQuestions() {
        isMarkedForLaterQuestionsShown = !isMarkedForLaterQuestionsShown
        val questions = getCurrentQuizQuestionsFromUser()
        refreshQuestions(questions)
        toast( if(!isMarkedForLaterQuestionsShown) "Showing all Questions" else "Showing Marked for Later questions" )
        invalidateOptionsMenu()
    }

    private fun getCurrentQuizQuestionsFromUser(): ArrayList<Question> {
        return getCurrentQuizFromUser()!!.questions
    }

    private fun refreshQuestions(questions: ArrayList<Question>) {
        getRecyclerAdapter()?.updateQuestions(questions.filter { it -> ((it.isMarkedForLater == isMarkedForLaterQuestionsShown) || !isMarkedForLaterQuestionsShown) } as java.util.ArrayList<Question>)
    }

    override fun onPause() {
        super.onPause()
        if(runningTimer != -1L) {
            getCurrentQuizFromUser()!!.saveTimer(runningTimer)
            dataSource.addUser(user)
        }
        timer?.cancel()
    }

    override fun onResume() {
        super.onResume()
        setupTimer(getCurrentQuizFromUser()?.fetchResumeTimer())
    }

    override fun onBackPressed() {
        ConfirmationDialog(this, "Are you sure you want to exit Quiz?", R.string.yes, R.string.ok, 0) {
            config.setLoggedInUser(user)
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        config.setLoggedInUser(user)
        bus.unregister(this)
        super.onDestroy()
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
                val questions = getCurrentQuizQuestionsFromUser()
                val currentQuestion = questions.find { it.id.toString() == questionId } as Question

                // Mark change
                if(!currentQuestion.audios.isEmpty()) {
                    val audioQuestion = currentQuestion.audios.first()
                    audioQuestion.details[AUDIO_PLAYBACK_STATE] = arrayListOf(state)
                    // Refresh Questions
                    refreshQuestions(questions)
                }
            }
        }
    }
}

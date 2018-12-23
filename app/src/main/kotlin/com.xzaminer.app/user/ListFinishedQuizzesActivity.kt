package com.xzaminer.app.user

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.Toolbar
import android.view.Window
import com.simplemobiletools.commons.extensions.beVisible
import com.simplemobiletools.commons.views.MyGridLayoutManager
import com.xzaminer.app.R
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.extensions.config
import com.xzaminer.app.result.ResultActivity
import com.xzaminer.app.studymaterial.StudyMaterial
import com.xzaminer.app.utils.QUIZ_ID
import kotlinx.android.synthetic.main.activity_unfinished_quizzes.*


class ListFinishedQuizzesActivity : SimpleActivity() {

    private var toolbar: Toolbar? = null
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_ACTION_MODE_OVERLAY)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unfinished_quizzes)
        title = "Finished Quizzes"

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar?.setNavigationOnClickListener { onBackPressed() }

        user = config.getLoggedInUser() as User
        setupGridLayoutManager()
        setupAdapter(user.fetchFinishedQuizzes())
    }

    private fun setupGridLayoutManager() {
        val layoutManager = quizzes_grid.layoutManager as MyGridLayoutManager
        layoutManager.orientation = GridLayoutManager.VERTICAL
        layoutManager.spanCount = 1
    }

    private fun setupAdapter(quizzes: ArrayList<StudyMaterial>) {
        val currAdapter = quizzes_grid.adapter
        if (currAdapter == null) {
            FinishedQuizzesAdapter(
                this,
                quizzes.clone() as ArrayList<StudyMaterial>,
                quizzes_grid, media_vertical_fastscroller
            ) {
                if (it is StudyMaterial) {
                    Intent(this, ResultActivity::class.java).apply {
                        putExtra(QUIZ_ID, it.id)
                        startActivity(this)
                    }
                    finish()
                    return@FinishedQuizzesAdapter
                }
            }.apply {
                quizzes_grid.adapter = this
                addVerticalDividers(true)
            }
        } else {
            (currAdapter as FinishedQuizzesAdapter).updateQuizzes(quizzes)
        }
        if(quizzes.isEmpty()) {
            quizzes_empty_text_label.text = "No Unfinished Quizzes"
            quizzes_empty_text_label.beVisible()
        }

        media_vertical_fastscroller.isHorizontal = false
        media_vertical_fastscroller.updatePrimaryColor()
        media_vertical_fastscroller.setViews(quizzes_grid, null) {
        }
    }
}

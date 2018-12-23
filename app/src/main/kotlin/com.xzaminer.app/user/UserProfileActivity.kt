package com.xzaminer.app.user

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.Window
import com.xzaminer.app.R
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.billing.ListUserPurchasesActivity
import com.xzaminer.app.extensions.config
import kotlinx.android.synthetic.main.activity_user_profile.*

class UserProfileActivity : SimpleActivity() {

    private var toolbar: Toolbar? = null
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_ACTION_MODE_OVERLAY)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar?.setNavigationOnClickListener { onBackPressed() }
        title = "My Profile"

        setupData()
    }

    private fun setupData() {
        user = config.getLoggedInUser() as User

        attempted_text.text = user.fetchAttemptedNumberOfQuizzes() + "\nQuizzes Finished"
        average_text.text = user.fetchAttemptedQuizzesAverage().toString() + "%\nAverage Score"
        finished_quizzes_desc.text = user.fetchFinishedQuizzes().size.toString() + " Finished Quizzes"
        unfinished_quizzes_desc.text = user.fetchUnfinishedQuizzes().size.toString() + " Unfinished Quizzes"
        purchases_desc.text = user.purchases.size.toString() + " Purchases"

        finished_root.setOnClickListener {
            Intent(this, ListFinishedQuizzesActivity::class.java).apply {
                startActivity(this)
            }
        }

        unfinished_root.setOnClickListener {
            Intent(this, ListUnfinishedQuizzesActivity::class.java).apply {
                startActivity(this)
            }
        }

        purchases_root.setOnClickListener {
            Intent(this, ListUserPurchasesActivity::class.java).apply {
                startActivity(this)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setupData()
    }
}

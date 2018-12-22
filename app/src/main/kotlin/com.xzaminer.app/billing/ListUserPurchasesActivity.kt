package com.xzaminer.app.billing

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.Toolbar
import android.view.Window
import com.simplemobiletools.commons.extensions.beVisible
import com.simplemobiletools.commons.views.MyGridLayoutManager
import com.xzaminer.app.R
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.extensions.config
import com.xzaminer.app.user.User
import kotlinx.android.synthetic.main.activity_unfinished_quizzes.*


class ListUserPurchasesActivity : SimpleActivity() {

    private var toolbar: Toolbar? = null
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_ACTION_MODE_OVERLAY)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unfinished_quizzes)
        title = "Purchases"

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar?.setNavigationOnClickListener { onBackPressed() }

        user = config.getLoggedInUser() as User
        setupGridLayoutManager()
        setupAdapter(user.purchases)
    }

    private fun setupGridLayoutManager() {
        val layoutManager = quizzes_grid.layoutManager as MyGridLayoutManager
        layoutManager.orientation = GridLayoutManager.VERTICAL
        layoutManager.spanCount = 1
    }

    private fun setupAdapter(purchases: java.util.ArrayList<Purchase>) {
        val currAdapter = quizzes_grid.adapter
        if (currAdapter == null) {
            ListUserPurchasesAdapter(
                this,
                purchases.clone() as ArrayList<Purchase>,
                quizzes_grid
            ) {
            }.apply {
                quizzes_grid.adapter = this
                addVerticalDividers(true)
            }
        } else {
            (currAdapter as ListUserPurchasesAdapter).updatePurchases(purchases)
        }
        if(purchases.isEmpty()) {
            quizzes_empty_text_label.text = "No User Purchases"
            quizzes_empty_text_label.beVisible()
        }
    }
}

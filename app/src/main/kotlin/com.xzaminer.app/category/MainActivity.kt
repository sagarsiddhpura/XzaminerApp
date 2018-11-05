package com.xzaminer.app.category

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.Toolbar
import android.view.Window
import com.simplemobiletools.commons.extensions.appLaunched
import com.simplemobiletools.commons.extensions.beVisibleIf
import com.simplemobiletools.commons.extensions.isGone
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.helpers.PERMISSION_WRITE_STORAGE
import com.simplemobiletools.commons.views.MyGridLayoutManager
import com.xzaminer.app.BuildConfig
import com.xzaminer.app.R
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.extensions.config
import com.xzaminer.app.extensions.dataSource
import com.xzaminer.app.extensions.debugDataSource
import com.xzaminer.app.extensions.getCategoriesFromDb
import com.xzaminer.app.utils.CAT_ID
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : SimpleActivity() {

    private var catId: Long? = null
    private var toolbar: Toolbar? = null
    private var mStoredTextColor = 0
    private var mStoredPrimaryColor = 0
    private var mIsGettingCategories = false
    private var mShouldStopFetching = false
    private var mLoadedInitialCategories = false

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_ACTION_MODE_OVERLAY)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        appLaunched(BuildConfig.APPLICATION_ID)

        intent.apply {
            catId = getLongExtra(CAT_ID, -1)
            if(catId == (-1).toLong()) {
                catId = null
            }
        }

        storeStateVariables()
        tryLoadCategories(catId)

        if(catId != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            toolbar?.setNavigationOnClickListener { onBackPressed() }
        } else {
            supportActionBar?.title = "Xzaminer"
        }

        // debug
        if(BuildConfig.DEBUG) {

        }
        debugDataSource.initMockDataRealtimeDatabase(dataSource)
    }

    override fun onResume() {
        super.onResume()

        if (mStoredTextColor != config.textColor) {
            getRecyclerAdapter()?.updateTextColor(config.textColor)
        }

        if (mStoredPrimaryColor != config.primaryColor) {
            getRecyclerAdapter()?.updatePrimaryColor(config.primaryColor)
        }

        directories_empty_text_label.setTextColor(config.textColor)

        tryLoadCategories(catId)
    }

    override fun onPause() {
        super.onPause()
        mIsGettingCategories = false
        storeStateVariables()
    }

    private fun getRecyclerAdapter() = categories_grid.adapter as? CategoriesAdapter

    private fun storeStateVariables() {
        config.apply {
            mStoredTextColor = textColor
            mStoredPrimaryColor = primaryColor
        }
    }

    private fun tryLoadCategories(catId: Long?) {
        handlePermission(PERMISSION_WRITE_STORAGE) {
            if (it) {
                getCategories(catId)
                setupLayoutManager()
            } else {
                toast(R.string.no_storage_permissions)
                finish()
            }
        }
    }

    private fun getCategories(catId: Long?) {
        if (mIsGettingCategories) {
            return
        }

        mShouldStopFetching = true
        mIsGettingCategories = true

        getCategoriesFromDb(catId) { cats: ArrayList<Category>, name: String ->
            supportActionBar?.title = name
            gotCategories(cats)
        }
    }

    private fun setupLayoutManager() {
        setupGridLayoutManager()
    }

    private fun setupGridLayoutManager() {
        val layoutManager = categories_grid.layoutManager as MyGridLayoutManager
        layoutManager.orientation = GridLayoutManager.VERTICAL
        layoutManager.spanCount = config.dirColumnCnt
    }

    private fun itemClicked(item: Any) {
        if(item is Category) {
            if(!item.isAudioBook) {
                Intent(this, MainActivity::class.java).apply {
                    putExtra(CAT_ID, item.id)
                    startActivity(this)
                }
            } else {
//                Intent(this, AudioBookActivity::class.java).apply {
//                    putExtra(CAT_ID, item.id)
//                    startActivity(this)
//                }
            }
        }
    }

    private fun gotCategories(newDirs: ArrayList<Category>) {
        mIsGettingCategories = false
        mShouldStopFetching = false

//        val cats = getSortedCategories(newDirs)
        val cats = newDirs
        runOnUiThread {
            checkPlaceholderVisibility(cats)
            setupAdapter(cats)
        }

        mLoadedInitialCategories = true

        runOnUiThread {
            checkPlaceholderVisibility(cats)
        }
    }

    private fun checkPlaceholderVisibility(dirs: ArrayList<Category>) {
        if(dirs.isEmpty() && mLoadedInitialCategories) {
            if(catId == null) {
                directories_empty_text_label.text = "No Data to show. Connect to Internet and try again!"
            } else {
                directories_empty_text_label.text = "No files in this category. Try again later!"
            }
        }
        directories_empty_text_label.beVisibleIf(dirs.isEmpty() && mLoadedInitialCategories)
        categories_grid.beVisibleIf(directories_empty_text_label.isGone())
    }

    private fun setupAdapter(dirs: ArrayList<Category>) {
        val currAdapter = categories_grid.adapter
        if (currAdapter == null) {
            CategoriesAdapter(this, dirs.clone() as ArrayList<Category>, categories_grid) {
                itemClicked(it)
            }.apply {
                categories_grid.adapter = this
            }
        } else {
            (currAdapter as CategoriesAdapter).updateDirs(dirs)
        }
    }

    private fun getCurrentlyDisplayedCategories(): ArrayList<Category> = getRecyclerAdapter()?.cats ?: ArrayList()
}

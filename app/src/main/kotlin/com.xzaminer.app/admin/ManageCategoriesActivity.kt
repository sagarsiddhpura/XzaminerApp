package com.xzaminer.app.admin

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.PERMISSION_WRITE_STORAGE
import com.simplemobiletools.commons.views.MyGridLayoutManager
import com.xzaminer.app.R
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.category.Category
import com.xzaminer.app.course.CourseActivity
import com.xzaminer.app.extensions.dataSource
import com.xzaminer.app.extensions.getCategoriesFromDb
import com.xzaminer.app.studymaterial.ConfirmDialog
import com.xzaminer.app.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class ManageCategoriesActivity : SimpleActivity() {

    private var toolbar: Toolbar? = null
    private var mIsGettingCategories = false
    private var mShouldStopFetching = false
    private var mLoadedInitialCategories = false
    private var catId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_ACTION_MODE_OVERLAY)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Manage Categories"

        intent.apply {
            catId = getLongExtra(CAT_ID, -1)
            if(catId == (-1).toLong()) {
                catId = null
            }
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

//        Log.d("Xz_", "Main 2:"+System.nanoTime())
        getCategoriesFromDb(catId) { cats: ArrayList<Category>, name: String ->
            Log.d("Xz_", "Main 3:"+System.nanoTime())
            gotCategories(cats)
        }
    }

    private fun setupLayoutManager() {
        setupGridLayoutManager()
    }

    private fun setupGridLayoutManager() {
        val layoutManager = categories_grid.layoutManager as MyGridLayoutManager
        layoutManager.orientation = GridLayoutManager.VERTICAL
        layoutManager.spanCount = 1

        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return 1
            }
        }
    }

    private fun gotCategories(newDirs: ArrayList<Category>) {
        mIsGettingCategories = false
        mShouldStopFetching = false

        runOnUiThread {
            checkPlaceholderVisibility(newDirs)
            setupAdapter(newDirs)
        }

        mLoadedInitialCategories = true

        runOnUiThread {
            checkPlaceholderVisibility(newDirs)
        }
    }

    private fun checkPlaceholderVisibility(dirs: ArrayList<Category>) {
        if(dirs.isEmpty() && mLoadedInitialCategories) {
            categories_empty_text_label.text = "No Data to show. Connect to Internet and try again!"
        }
        categories_empty_text_label.beVisibleIf(dirs.isEmpty() && mLoadedInitialCategories)
        categories_grid.beVisibleIf(categories_empty_text_label.isGone())
    }

    private fun setupAdapter(cats: ArrayList<Category>) {
        val currAdapter = categories_grid.adapter
        if (currAdapter == null) {
            EditCategoriesAdapter(this, cats.clone() as ArrayList<Category>, categories_grid) {
                itemClicked(it)
            }.apply {
                categories_grid.adapter = this
            }
            setupLayoutManager()
        } else {
            (currAdapter as EditCategoriesAdapter).updateCategories(cats)
        }
    }

    private fun itemClicked(item: Any) {
        if(item is Category && item.id != -1L) {
            if(!item.isCourse) {
                // Category
                Intent(this, ManageCategoriesActivity::class.java).apply {
                    putExtra(CAT_ID, item.id)
                    startActivity(this)
                }
            } else {
                // Course
                Intent(this, ManageCourseActivity::class.java).apply {
                    putExtra(COURSE_ID, item.id)
                    startActivity(this)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        tryLoadCategories(if(catId == null) 1 else catId)
    }

    private fun getRecyclerAdapter() = categories_grid.adapter as? EditCategoriesAdapter

    fun editEntity(item: Category) {
        if(item.id != -1L) {
            if(!item.isCourse) {
                // Category
                toast("This functionality is being implemented")
            } else {
                // Course
                Intent(this, EditCourseActivity::class.java).apply {
                    putExtra(COURSE_ID, item.id)
                    startActivity(this)
                }
            }
        }
    }

    fun deleteEntity(item: Category) {
        if(item.id != -1L) {
            if(!item.isCourse) {
                toast("This functionality is being implemented")
            } else {
                // Course
                ConfirmDialog(this, "Are you sure you want to Delete this entire Course?") {
                    dataSource.deleteCourse(item)
                    tryLoadCategories(if(catId == null) 1 else catId)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_add, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.manage_add -> addCourse()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun addCourse() {
        toast("This functionality is being implemented....")
    }
}

package com.xzaminer.app.category

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.ColorUtils
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.extensions.beVisibleIf
import com.simplemobiletools.commons.extensions.isGone
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.helpers.PERMISSION_WRITE_STORAGE
import com.simplemobiletools.commons.views.MyGridLayoutManager
import com.xzaminer.app.BuildConfig
import com.xzaminer.app.R
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.admin.AddQuestionBankActivity
import com.xzaminer.app.course.CourseActivity
import com.xzaminer.app.extensions.config
import com.xzaminer.app.extensions.getCategoriesFromDb
import com.xzaminer.app.extensions.launchAbout
import com.xzaminer.app.utils.CAT_ID
import com.xzaminer.app.utils.COURSE_ID
import com.xzaminer.app.utils.logEvent
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.drawer_header.view.*
import java.util.HashMap
import kotlin.collections.ArrayList

class MainActivity : SimpleActivity() {

    private var catId: Long? = null
    private var toolbar: Toolbar? = null
    private var mStoredTextColor = 0
    private var mStoredPrimaryColor = 0
    private var mIsGettingCategories = false
    private var mShouldStopFetching = false
    private var mLoadedInitialCategories = false
    private var drawer: Drawer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_ACTION_MODE_OVERLAY)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        intent.apply {
            catId = getLongExtra(CAT_ID, -1)
            if(catId == (-1).toLong()) {
                catId = null
            }
        }

        storeStateVariables()

        if(catId != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            toolbar?.setNavigationOnClickListener { onBackPressed() }
        } else {
            supportActionBar?.title = "Xzaminer"
            setupDrawer()
        }

        // debug
        if(BuildConfig.DEBUG) {
            if(catId == null) {
//                Intent(this, ResultActivity::class.java).apply {
//                    putExtra(QUIZ_ID, 10111L)
//                    startActivity(this)
//                }
            }
//            Intent(this, QuizActivity::class.java).apply {
//                putExtra(CAT_ID, 10111L)
//                putExtra(IS_NEW_QUIZ, catId != null)
//                startActivity(this)
//            }
//            Intent(this, AddQuestionBankActivity::class.java).apply {
//                startActivity(this)
//            }
//            Intent(this, CourseActivity::class.java).apply {
//                putExtra(COURSE_ID, 101L)
//                startActivity(this)
//            }
        }
//        debugDataSource.initMockDataRealtimeDatabase(dataSource)
//        debugDataSource.copyQuestionBank(dataSource)
    }

    override fun onResume() {
        super.onResume()

        if (mStoredTextColor != config.textColor) {
            getRecyclerAdapter()?.updateTextColor(config.textColor)
        }

        if (mStoredPrimaryColor != config.primaryColor) {
            getRecyclerAdapter()?.updatePrimaryColor(config.primaryColor)
        }

        categories_empty_text_label.setTextColor(config.textColor)

        tryLoadCategories(if(catId == null) 1 else catId)
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

        val adapter = getRecyclerAdapter()
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val aSectionTitle = adapter?.isASectionTitle(position)
                return if (adapter?.isASectionTitle(position) == true) {
                    layoutManager.spanCount
                } else {
                    1
                }
            }
        }
    }

    private fun itemClicked(item: Any) {
        if(item is Category && item.id != -1L) {
            if(!item.isCourse) {
                Intent(this, MainActivity::class.java).apply {
                    putExtra(CAT_ID, item.id)
                    startActivity(this)
                }
            } else {
                Intent(this, CourseActivity::class.java).apply {
                    putExtra(COURSE_ID, item.id)
                    startActivity(this)
                }
            }
        }
    }

    private fun gotCategories(newDirs: ArrayList<Category>) {
        mIsGettingCategories = false
        mShouldStopFetching = false

//        val cats = getSortedCategories(newDirs)
//        val cats = arrayListOf<Category>()
//        val subCats = newDirs.filter { !it.isCourse }
//        if(!subCats.isEmpty()) {
//            cats.add(Category(-1L, "Categories"))
//            cats.addAll(subCats)
//        }
//
//        val questionBanks = newDirs.filter { it.isCourse }
//        if(!questionBanks.isEmpty()) {
//            cats.add(Category(-1L, "Question Banks"))
//            cats.addAll(questionBanks)
//        }

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
            if(catId == null) {
                categories_empty_text_label.text = "No Data to show. Connect to Internet and try again!"
            } else {
                categories_empty_text_label.text = "No files in this category. Try again later!"
            }
        }
        categories_empty_text_label.beVisibleIf(dirs.isEmpty() && mLoadedInitialCategories)
        categories_grid.beVisibleIf(categories_empty_text_label.isGone())
    }

    private fun setupAdapter(cats: ArrayList<Category>) {
        val currAdapter = categories_grid.adapter
        if (currAdapter == null) {
            CategoriesAdapter(this, cats.clone() as ArrayList<Category>, categories_grid) {
                itemClicked(it)
            }.apply {
                categories_grid.adapter = this
            }
            setupLayoutManager()
        } else {
            (currAdapter as CategoriesAdapter).updateCategories(cats)
        }
    }

    private fun getCurrentlyDisplayedCategories(): ArrayList<Category> = getRecyclerAdapter()?.cats ?: ArrayList()

    private fun setupDrawer() {
        val home = PrimaryDrawerItem().withIdentifier(1).withName(R.string.home)
            .withIcon(R.drawable.ic_drw_home).withIconTintingEnabled(true)
        val purchases = PrimaryDrawerItem().withIdentifier(9).withName(R.string.purchases)
            .withIcon(R.drawable.ic_drw_purchases).withIconTintingEnabled(true)
        val settings = PrimaryDrawerItem().withIdentifier(2).withName(R.string.settings)
            .withIcon(R.drawable.ic_settings).withIconTintingEnabled(true)
        val importQuestionBank = PrimaryDrawerItem().withIdentifier(10).withName(R.string.title_activity_import_question_bank)
            .withIcon(R.drawable.ic_drw_question_bank).withIconTintingEnabled(true)
        val logout = PrimaryDrawerItem().withIdentifier(11).withName(R.string.logout)
            .withIcon(R.drawable.ic_logout).withIconTintingEnabled(true)

//        val likeHeader = SectionDrawerItem().withName("Like this App?").withDivider(true)
//        val share = PrimaryDrawerItem().withIdentifier(4).withName(R.string.share)
//            .withIcon(R.drawable.ic_share).withIconTintingEnabled(true)
//
//        val rateUs = PrimaryDrawerItem().withIdentifier(6).withName(R.string.rate_us)
//            .withIcon(R.drawable.ic_thumb_up).withIconTintingEnabled(true)
//
//        val dislikeHeader = SectionDrawerItem().withName("Missing Functionality / Issues / Problems").withDivider(true)
//        val sendFeedback = PrimaryDrawerItem().withIdentifier(5).withName(R.string.send_feedback)
//            .withIcon(R.drawable.ic_feedback).withIconTintingEnabled(true)
        // Remaining
        // Donate/Contribute
        // tips & tutorials
        val idToMap = HashMap<Int, String>()
        idToMap.put(1, "Home")
        idToMap.put(2, "Settings")
        idToMap.put(3, "About")
        idToMap.put(4, "Share")
        idToMap.put(5, "Send Feedback")
        idToMap.put(6, "Rate Us")
        idToMap.put(7, "Whats New")
        idToMap.put(8, "Favourites")
        idToMap.put(9, "Purchases")
        idToMap.put(10, "ImportQuestionBank")
        idToMap.put(11, "Logout")

        val customPrimaryColor = baseConfig.primaryColor
        val view = LayoutInflater.from(this).inflate(R.layout.drawer_header, null)
        when (baseConfig.appRunCount % 3) {
            0 -> { view.material_drawer_account_header_background.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.header))
                val primaryColorAlpha = ColorUtils.setAlphaComponent(customPrimaryColor, 140)
                view.material_drawer_account_header_background.setColorFilter(primaryColorAlpha) }
            1 -> { view.material_drawer_account_header_background.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.header2))
                val primaryColorAlpha = ColorUtils.setAlphaComponent(customPrimaryColor, 100)
                view.material_drawer_account_header_background.setColorFilter(primaryColorAlpha) }
            2 -> { view.material_drawer_account_header_background.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.header3))
                val primaryColorAlpha = ColorUtils.setAlphaComponent(customPrimaryColor, 160)
                view.material_drawer_account_header_background.setColorFilter(primaryColorAlpha) }
        }

        view.app_version.text = "v${BuildConfig.VERSION_NAME}"
        val defaultSelection = 1L

        val header = AccountHeaderBuilder()
            .withActivity(this)
            .withAccountHeader(view)
            .build()

        val drawerBuilder = DrawerBuilder()
            .withActivity(this)
            .withToolbar(toolbar as Toolbar)
            .withAccountHeader(header)
            .withScrollToTopAfterClick(true)
            .withSelectedItem(defaultSelection)
            .withOnDrawerItemClickListener(object : Drawer.OnDrawerItemClickListener {
                override fun onItemClick(view1: View, position: Int, drawerItem: IDrawerItem<*, *>): Boolean {
                    logEvent("Drawer" + idToMap.getValue(drawerItem.identifier.toInt()))
                    when (drawerItem.identifier) {
                        1L -> {
//                                config.showMedia = IMAGES_AND_VIDEOS
//                                tryLoadCategories()
                        }
                        2L -> {
//                            launchSettings()
                            toast("This functionality is being implemented")
                            resetSelection()
                        }
                        3L -> {
                            launchAbout()
                            resetSelection()
                        }
                        4L -> {
                            toast("This functionality is being implemented")
                            resetSelection()
                        }
                        5L -> {
                            toast("This functionality is being implemented")
                            resetSelection()
                        }
                        6L -> {
                            toast("This functionality is being implemented")
                            resetSelection()
                        }
                        7L -> {
                            resetSelection()
                        }
                        8L -> {
                            toast("This functionality is being implemented")
                            resetSelection()
                        }
                        9L -> {
                            toast("This functionality is being implemented")
                            resetSelection()
                        }
                        10L -> {
                            startActivity(Intent(applicationContext, AddQuestionBankActivity::class.java))
                            resetSelection()
                        }
                        11L -> {
//                            dataSource.logout()
//                            config.setLoggedInUser(null)
//                            startActivity(Intent(applicationContext, SplashActivity::class.java))
//                            finish()
                            resetSelection()
                        }
                    }
                    return false
                }
            })
        drawerBuilder.addDrawerItems(
            home,
            purchases,
            settings,
            importQuestionBank,
            logout
        )
        drawer = drawerBuilder.build()

        if(baseConfig.appRunCount == 1) {
            Handler().postDelayed({
                drawer?.openDrawer()
            }, 500)
            Handler().postDelayed({
                drawer?.closeDrawer()
            }, 1500)
        }
    }

    private fun resetSelection() {
        drawer?.setSelection(1L, false)
    }
}

package com.xzaminer.app.category

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.ColorUtils
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.extensions.beVisibleIf
import com.simplemobiletools.commons.extensions.isGone
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.helpers.PERMISSION_WRITE_STORAGE
import com.simplemobiletools.commons.views.MyGridLayoutManager
import com.xzaminer.app.*
import com.xzaminer.app.admin.*
import com.xzaminer.app.billing.Purchase
import com.xzaminer.app.course.CourseActivity
import com.xzaminer.app.extensions.*
import com.xzaminer.app.studymaterial.ConfirmDialog
import com.xzaminer.app.user.User
import com.xzaminer.app.user.UserProfileActivity
import com.xzaminer.app.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.drawer_header.view.*
import java.util.HashMap
import kotlin.collections.ArrayList

class MainActivity : SimpleActivity(), BillingProcessor.IBillingHandler {

    private var catId: Long? = null
    private var toolbar: Toolbar? = null
    private var mStoredTextColor = 0
    private var mStoredPrimaryColor = 0
    private var mIsGettingCategories = false
    private var mShouldStopFetching = false
    private var mLoadedInitialCategories = false
    private var drawer: Drawer? = null
    private var billing: BillingProcessor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_ACTION_MODE_OVERLAY)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        Log.d("Xz_", "Main 1:"+System.nanoTime())

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        intent.apply {
            catId = getLongExtra(CAT_ID, -1)
            if(catId == (-1).toLong()) {
                catId = null
                // TODO: debug
                if(!BuildConfig.DEBUG) {
                    startActivity(Intent(applicationContext, IntroActivity::class.java))
                }
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

        if(catId == null) {
            checkBilling()
            checkUser()
        }

        // debug
        if(BuildConfig.DEBUG) {
            if(catId == null) {
//                Intent(this, ResultActivity::class.java).apply {
//                    putExtra(QUIZ_ID, 10111L)
//                    startActivity(this)
//                }

//            Intent(this, QuizActivity::class.java).apply {
//                putExtra(QUIZ_ID, 101200L)
//                putExtra(SECTION_ID, 1012L)
//                putExtra(COURSE_ID, 101L)
//                putExtra(IS_NEW_QUIZ, true)
//                startActivity(this)
//            }

//            Intent(this, AddQuestionBankActivity::class.java).apply {
//                startActivity(this)
//            }
//            Intent(this, AddStudyMaterialActivity::class.java).apply {
//                startActivity(this)
//            }
//            Intent(this, IntroActivity::class.java).apply {
//                startActivity(this)
//            }
//                Intent(this, CourseActivity::class.java).apply {
//                    putExtra(COURSE_ID, 101L)
//                    startActivity(this)
//                }
//                Intent(this, UserProfileActivity::class.java).apply {
//                    startActivity(this)
//                }
//                Intent(this, VideoActivity::class.java).apply {
//                    putExtra(COURSE_ID, 101L)
//                    putExtra(SECTION_ID, 1016L)
//                    putExtra(DOMAIN_ID, 101601L)
//                    putExtra(VIDEO_ID, 1016001L)
//                    startActivity(this)
//                }

//                Intent(this, CourseSectionVideosDomainActivity::class.java).apply {
//                    putExtra(COURSE_ID, 101L)
//                    putExtra(SECTION_ID, 1016L)
//                    putExtra(DOMAIN_ID, 101601L)
//                    startActivity(this)
//                }

//                Intent(this, ManageCategoriesActivity::class.java).apply {
//                    startActivity(this)
//                }

//                Intent(this, EditCourseActivity::class.java).apply {
//                    putExtra(COURSE_ID, 101L)
//                    startActivity(this)
//                }

//                Intent(this, ManageCategoriesActivity::class.java).apply {
//                    putExtra(COURSE_ID, 101L)
//                    startActivity(this)
//                }

//                Intent(this, ManageCourseActivity::class.java).apply {
//                    putExtra(COURSE_ID, 101L)
//                    startActivity(this)
//                }

//                Intent(this, EditSectionActivity::class.java).apply {
//                    putExtra(COURSE_ID, 101L)
//                    putExtra(SECTION_ID, 1012L)
//                    startActivity(this)
//                }

//                Intent(this, EditQuizActivity::class.java).apply {
//                    putExtra(COURSE_ID, 101L)
//                    putExtra(SECTION_ID, 1012L)
//                    putExtra(QUIZ_ID, 101200L)
//                    startActivity(this)
//                }

//                Intent(this, EditQuizQuestionsActivity::class.java).apply {
//                    putExtra(COURSE_ID, 101L)
//                    putExtra(SECTION_ID, 1012L)
//                    putExtra(QUIZ_ID, 101200L)
//                    startActivity(this)
//                }

//                Intent(this, EditQuizActivity::class.java).apply {
//                    putExtra(COURSE_ID, 101L)
//                    putExtra(SECTION_ID, 1014L)
//                    putExtra(QUIZ_ID, 101400L)
//                    startActivity(this)
//                }

                Intent(this, EditStudyMaterialQuestionsActivity::class.java).apply {
                    putExtra(COURSE_ID, 101L)
                    putExtra(SECTION_ID, 1014L)
                    putExtra(QUIZ_ID, 101400L)
                    startActivity(this)
                }
            }
            debugDataSource.initMockDataRealtimeDatabase(dataSource)
//            debugDataSource.copyQuestionBank(dataSource)
//            debugDataSource.uploadImages(this, dataSource)
        }
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

//        Log.d("Xz_", "Main 2:"+System.nanoTime())
        getCategoriesFromDb(catId) { cats: ArrayList<Category>, name: String ->
            Log.d("Xz_", "Main 3:"+System.nanoTime())
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
        layoutManager.spanCount = 1

        val adapter = getRecyclerAdapter()
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (adapter?.isASectionTitle(position) == true) {
                    1
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
        val purchases = PrimaryDrawerItem().withIdentifier(9).withName(R.string.myprofile)
            .withIcon(R.drawable.ic_drw_my_profile).withIconTintingEnabled(true)
        val settings = PrimaryDrawerItem().withIdentifier(2).withName(R.string.settings)
            .withIcon(R.drawable.ic_settings).withIconTintingEnabled(true)
        val importQuestionBank = PrimaryDrawerItem().withIdentifier(10).withName(R.string.title_activity_import_question_bank)
            .withIcon(R.drawable.ic_drw_question_bank).withIconTintingEnabled(true)
        val logout = PrimaryDrawerItem().withIdentifier(11).withName(R.string.logout)
            .withIcon(R.drawable.ic_logout).withIconTintingEnabled(true)
        val importStudyMaterial = PrimaryDrawerItem().withIdentifier(12).withName(R.string.title_activity_import_study_material)
            .withIcon(R.drawable.ic_drw_question_bank).withIconTintingEnabled(true)
        val manageCategories = PrimaryDrawerItem().withIdentifier(13).withName(R.string.maange_categories)
            .withIcon(R.drawable.drw_manage_categories).withIconTintingEnabled(true)

        val idToMap = HashMap<Int, String>()
        idToMap.put(1, "Home")
        idToMap.put(2, "Settings")
        idToMap.put(3, "About")
        idToMap.put(4, "Share")
        idToMap.put(5, "Send Feedback")
        idToMap.put(6, "Rate Us")
        idToMap.put(7, "Whats New")
        idToMap.put(8, "Favourites")
        idToMap.put(9, "My Profile")
        idToMap.put(10, "ImportQuestionBank")
        idToMap.put(11, "Logout")
        idToMap.put(12, "ImportStudyMaterial")
        idToMap.put(13, "ManageCategories")

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
                            startActivity(Intent(applicationContext, UserProfileActivity::class.java))
                            resetSelection()
                        }
                        10L -> {
                            startActivity(Intent(applicationContext, AddQuestionBankActivity::class.java))
                            resetSelection()
                        }
                        11L -> {
                            ConfirmDialog(this@MainActivity, "Are you sure you want to Logout?") {
                                dataSource.logout()
                                config.setLoggedInUser(null)
                                config.isOtpVerified = false
                                startActivity(Intent(applicationContext, SplashActivity::class.java))
                                resetSelection()
                                finish()
                            }
                        }
                        12L -> {
                            startActivity(Intent(applicationContext, AddStudyMaterialActivity::class.java))
                            resetSelection()
                        }
                        13L -> {
                            startActivity(Intent(applicationContext, ManageCategoriesActivity::class.java))
                            resetSelection()
                        }
                    }
                    return false
                }
            })
        if(config.getLoggedInUser() != null && (config.getLoggedInUser() as User).userType == USERTYPE_ADMIN || BuildConfig.DEBUG) {
            drawerBuilder.addDrawerItems(
                home,
                purchases,
                settings,
                logout,
                DividerDrawerItem(),
                importQuestionBank,
                importStudyMaterial,
                manageCategories
            )
        } else {
            drawerBuilder.addDrawerItems(
                home,
                purchases,
                settings,
                logout
            )
        }

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

    override fun onBillingInitialized() {
        if (billing!!.loadOwnedPurchasesFromGoogle()) {
            val user = config.getLoggedInUser() as User
            val subs = billing!!.listOwnedSubscriptions()
            val iaps = billing!!.listOwnedProducts()

            //subs.add("com.audiboo.android.sub.yearly")
//            toast("listOwnedProducts:"+iaps)

            iaps.forEach {
                val purchaseTransactionDetails = billing!!.getPurchaseTransactionDetails(it)

                if (purchaseTransactionDetails != null) {
                    if (!user.hasPurchase(purchaseTransactionDetails.productId) &&
                        purchaseTransactionDetails.purchaseInfo != null &&
                        purchaseTransactionDetails.purchaseInfo.purchaseData != null &&
                        purchaseTransactionDetails.purchaseInfo.purchaseData.developerPayload != null &&
                        purchaseTransactionDetails.purchaseInfo.purchaseData.developerPayload.contains(
                            purchaseTransactionDetails.productId + "::user::" + user.getId())) {

                        val listType = object : TypeToken<TransactionDetails>() {}.type
                        val json = Gson().toJson(purchaseTransactionDetails, listType)

                        // Add purchase to user
                        val purchase = Purchase(purchaseTransactionDetails.productId, getProductName(purchaseTransactionDetails.productId),
                            getProductType(purchaseTransactionDetails.productId), "","", "", true, getExpiry(purchaseTransactionDetails.productId), json, getNowDate())
                        user.purchases.add(purchase)
                        config.setLoggedInUser(user)
                        // save to db
                        dataSource.addUser(user)
                        ConfirmationDialog(this, "We have restored your purchase " + purchase.name + ". Please enjoy the benefits", R.string.yes, R.string.ok, 0) { }
                        debugDataSource.addDebugObject(dataSource, "purchases/purchaseRestoredCategoriesActivity", purchaseTransactionDetails)
                    }

                    debugDataSource.addDebugObject(dataSource, "purchases/purchaseFoundCategoriesActivity", purchaseTransactionDetails)
                }
            }

            subs.forEach {
                val subscriptionTransactionDetails = billing!!.getSubscriptionTransactionDetails(it)
//                val dbRef = dataSource.getDatabase().getReference("purchaseLog/v1/1541008680775/user/purchases/0/details")
//
//                dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                        val string = snapshot.getValue(String::class.java)
//                        val type = object : TypeToken<TransactionDetails>() {}.type
//                        val det = Gson().fromJson<TransactionDetails>(string, type)
//
//                        val listType = object : TypeToken<TransactionDetails>() {}.type
//                        val json = Gson().toJson(det, listType)
//
//                        if(!user.hasPurchase(det.productId) &&
//                                det.purchaseInfo != null &&
//                                det.purchaseInfo.purchaseData != null &&
//                                det.purchaseInfo.purchaseData.developerPayload != null &&
//                                det.purchaseInfo.purchaseData.developerPayload.contains(
//                                        det.productId + "::user::" + user.getId())) {
//                            // Add purchase to user
//                            val purchase = Purchase(det.productId, getProductName(det.productId),
//                                    getProductType(det.productId), getExpiry(det.productId), json)
//                            user.purchases.add(purchase)
//                            config.setLoggedInUser(user)
//                            // save to db
//                            dataSource.addUser(user)
//                            ConfirmationDialog(this@CategoryActivity, "We have restored your purchase " + purchase.name + ". Please enjoy the benefits", R.string.yes, R.string.ok, 0) { }
//                        }
//
//                        if(subs.contains(det.productId) && !det.purchaseInfo.purchaseData.autoRenewing) {
//                            user.removePurchase(det.productId)
//                            ConfirmationDialog(this@CategoryActivity, "You have cancelled your subscription. Please subscribe or renew it to enjoy Subscription benefits.", R.string.yes, R.string.ok, 0) { }
//                            config.setLoggedInUser(user)
//                            dataSource.addUser(user)
//                        }
//
//                    }
//
//                    override fun onCancelled(databaseError: DatabaseError) {
//                        println("The read failed: " + (databaseError.code))
//                    }
//                })

                if(subscriptionTransactionDetails != null) {
                    if( subscriptionTransactionDetails.purchaseInfo != null &&
                        subscriptionTransactionDetails.purchaseInfo.purchaseData != null &&
                        subscriptionTransactionDetails.purchaseInfo.purchaseData.developerPayload != null &&
                        subscriptionTransactionDetails.purchaseInfo.purchaseData.developerPayload.contains(
                            subscriptionTransactionDetails.productId + "::user::" + user.getId())) {
                        if(!user.hasPurchase(subscriptionTransactionDetails.productId)) {
                            val listType = object : TypeToken<TransactionDetails>() {}.type
                            val json = Gson().toJson(subscriptionTransactionDetails, listType)

                            // Add purchase to user
                            val purchase = Purchase(subscriptionTransactionDetails.productId, getProductName(subscriptionTransactionDetails.productId),
                                getProductType(subscriptionTransactionDetails.productId), "", "", "", true, getExpiry(subscriptionTransactionDetails.productId), json, getNowDate())
                            user.purchases.add(purchase)
                            config.setLoggedInUser(user)
                            // save to db
                            dataSource.addUser(user)
                            ConfirmationDialog(this, "We have restored your purchase " + purchase.name + ". Please enjoy the benefits", R.string.yes, R.string.ok, 0) { }
                            debugDataSource.addDebugObject(dataSource, "purchaseAddedCategoriesActivity", subscriptionTransactionDetails)
                        }

                        debugDataSource.addDebugObject(dataSource, "purchaseCategoriesActivity", subscriptionTransactionDetails)
                        if(subs.contains(subscriptionTransactionDetails.productId) && !subscriptionTransactionDetails.purchaseInfo.purchaseData.autoRenewing) {
                            user.removePurchase(subscriptionTransactionDetails.productId)
                            ConfirmationDialog(this, "You have cancelled your subscription. Please subscribe or renew it to enjoy Subscription benefits.", R.string.yes, R.string.ok, 0) { }
                            config.setLoggedInUser(user)
                            dataSource.addUser(user)
                        }
                    }
                }
            }
            if(!subs.contains(IAP_SUB_YEARLY) && user.hasPurchase(IAP_SUB_YEARLY)) {
                user.removePurchase(IAP_SUB_YEARLY)
                dataSource.addUser(user)
            }
            if(!subs.contains(IAP_SUB_MONTHLY) && user.hasPurchase(IAP_SUB_MONTHLY)) {
                user.removePurchase(IAP_SUB_MONTHLY)
                dataSource.addUser(user)
            }
        }
    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
    }

    override fun onPurchaseHistoryRestored() {
    }

    private fun checkBilling() {
        val isAvailable = BillingProcessor.isIabServiceAvailable(this)
        if (!isAvailable) {
            toast("You do not have Google Play installed. Please install Google Play Services to continue using this App.")
            finish()
            return
        }
        // refresh user purchases
        billing = BillingProcessor(this, null, this)
        billing!!.initialize()
    }

    private fun checkUser() {
        val user = config.getLoggedInUser() as User
        dataSource.getUser(user.getId()) {
            if(it != null) {
                config.setLoggedInUser(it)
            }
        }
    }
}

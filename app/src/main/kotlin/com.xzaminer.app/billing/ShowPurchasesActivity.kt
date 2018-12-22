package com.xzaminer.app.billing

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.Toolbar
import android.view.Window
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.views.MyGridLayoutManager
import com.xzaminer.app.R
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.course.Course
import com.xzaminer.app.course.CourseSection
import com.xzaminer.app.course.CourseSectionVideosDomainActivity
import com.xzaminer.app.extensions.config
import com.xzaminer.app.extensions.dataSource
import com.xzaminer.app.extensions.debugDataSource
import com.xzaminer.app.studymaterial.QuizActivity
import com.xzaminer.app.studymaterial.StudyMaterial
import com.xzaminer.app.studymaterial.StudyMaterialActivity
import com.xzaminer.app.user.User
import com.xzaminer.app.utils.*
import kotlinx.android.synthetic.main.activity_show_purchases.*


class ShowPurchasesActivity : SimpleActivity(), BillingProcessor.IBillingHandler {

    private var studyMaterialId: Long = -1
    private var toolbar: Toolbar? = null
    private lateinit var user: User
    private var courseId: Long = -1
    private var sectionId: Long = -1
    private var purchaseId: String? = null
    private var isBillingInitialized: Boolean = false
    private var billing: BillingProcessor? = null
    private var purchases: ArrayList<Purchase> = arrayListOf()
    private var loadedCourse: Course? = null
    private var section: CourseSection? = null
    private var studyMaterial: StudyMaterial? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_ACTION_MODE_OVERLAY)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_purchases)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar?.setNavigationOnClickListener { onBackPressed() }
        supportActionBar?.title = "Purchase"

        intent.apply {
            studyMaterialId = getLongExtra(STUDY_MATERIAL_ID, -1)
            courseId = getLongExtra(COURSE_ID, -1)
            sectionId = getLongExtra(SECTION_ID, -1)
            purchaseId = getStringExtra(PURCHASE_ID)
//            if(purchaseId == null) {
//                toast("Error opening Purchase screen")
//                finish()
//                return
//            }
        }
        user = config.getLoggedInUser() as User
        setupGridLayoutManager()

        if(!isBillingInitialized) {
            billing = BillingProcessor(this, null, this)
            billing!!.initialize()
        }

        var purchases : ArrayList<Purchase> = arrayListOf()
        dataSource.getCourseById(courseId) { course ->
            loadedCourse = course
            if(course != null) {
                if(!course.fetchVisiblePurchases().isEmpty()) {
                    purchases.add(course.fetchVisiblePurchases().first())
                }

                if(sectionId != -1L) {
                    section = course.fetchSection(sectionId)
                    if(section != null) {
                        if(!section!!.fetchVisiblePurchases().isEmpty()) {
                            purchases.add(section!!.fetchVisiblePurchases().first())
                        }

                        if(sectionId != -1L) {
                            studyMaterial = section!!.fetchStudyMaterialById(studyMaterialId)
                            if(studyMaterial != null) {
                                if(!studyMaterial!!.fetchVisiblePurchases().isEmpty()) {
                                    purchases.add(studyMaterial!!.fetchVisiblePurchases().first())
                                }

                                showPurchases(purchases)
                                return@getCourseById
                            } else {
                                toast("Error opening Purchase screen.")
                                finish()
                                return@getCourseById
                            }
                        } else {
                            showPurchases(purchases)
                            return@getCourseById
                        }
                    } else {
                        toast("Error opening Purchase screen.")
                        finish()
                        return@getCourseById
                    }
                } else {
                    showPurchases(purchases)
                    return@getCourseById
                }
            } else {
                toast("Error opening Purchase screen.")
                finish()
                return@getCourseById
            }
        }
    }

    private fun showPurchases(purchasesParam: ArrayList<Purchase>) {
        this.purchases = purchasesParam
        val currAdapter = purchases_grid.adapter
        if (currAdapter == null) {
            ShowPurchasesAdapter(this, purchases.clone() as ArrayList<Purchase>, purchases_grid) {
            }.apply {
                purchases_grid.adapter = this
            }
        } else {
            (currAdapter as ShowPurchasesAdapter).updatePurchases(purchases)
        }
        setupGridLayoutManager()
    }

    private fun setupGridLayoutManager() {
        val layoutManager = purchases_grid.layoutManager as MyGridLayoutManager
        layoutManager.orientation = GridLayoutManager.VERTICAL
        layoutManager.spanCount = 1
    }

    fun initPurchase(purchase: Purchase) {
        val user = config.getLoggedInUser() as User
        if(isBillingInitialized) {
            val extraParams = Bundle()
            extraParams.putString("accountId", user.getId())
            billing!!.purchase(this@ShowPurchasesActivity, purchase.id, "::purchaseid::" + purchase.id + "::user::" + user.getId(), extraParams)
        }
    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        val listType = object : TypeToken<TransactionDetails>() {}.type
        val json = Gson().toJson(details, listType)

        val purchase = purchases.find { it.id == productId } as Purchase
        purchase.details = json
        purchase.purchased = getNowDate()

        val user = config.getLoggedInUser() as User

        if(!user.hasPurchase(productId)) {
            // Add purchase to user
            user.purchases.add(purchase)
            // save to db
            dataSource.addUser(user)
            // save to local
            config.setLoggedInUser(user)
        }

        val purLog = PurchaseLog(productId, details, user)
        dataSource.addPurchaseLog(purLog)
        debugDataSource.addDebugObject(dataSource, "purchases/PurchaseAudiobookActivity", details!!)

        navigate()
    }

    private fun navigate() {
        if(studyMaterialId != -1L) {
            if(studyMaterial!!.type == STUDY_MATERIAL_TYPE_STUDY_MATERIAL) {
                Intent(this, StudyMaterialActivity::class.java).apply {
                    putExtra(STUDY_MATERIAL_ID, studyMaterial!!.id)
                    putExtra(SECTION_ID, section!!.id)
                    putExtra(COURSE_ID, courseId)
                    putExtra(STUDY_MATERIAL_TYPE, section!!.type)
                    startActivity(this)
                }

                finish()
                return
            } else if(studyMaterial!!.type == STUDY_MATERIAL_TYPE_QUESTION_BANK ) {
                Intent(this, QuizActivity::class.java).apply {
                    putExtra(QUIZ_ID, studyMaterial!!.id)
                    putExtra(SECTION_ID, section!!.id)
                    putExtra(COURSE_ID, courseId)
                    putExtra(IS_NEW_QUIZ, true)
                    startActivity(this)
                }

                finish()
                return
            } else if(studyMaterial!!.type == STUDY_MATERIAL_TYPE_VIDEO ) {
                Intent(this, CourseSectionVideosDomainActivity::class.java).apply {
                    putExtra(DOMAIN_ID, studyMaterial!!.id)
                    putExtra(SECTION_ID, section!!.id)
                    putExtra(COURSE_ID, courseId)
                    startActivity(this)
                }

                finish()
                return
            }
        }

        finish()
        return
    }

    override fun onBillingInitialized() {
        isBillingInitialized = true
        billing!!.consumePurchase(PURCHASE_COURSE_IAP +"101")
        billing!!.consumePurchase(PURCHASE_SECTION_IAP +"1017")
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
        toast("Billing error: " + errorCode + ", details:" + (error?.message ?: ""))
    }

    override fun onPurchaseHistoryRestored() {
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (billing != null && !billing!!.handleActivityResult(requestCode, resultCode, resultData)) {
            super.onActivityResult(requestCode, resultCode, resultData)
        }
    }

    override fun onDestroy() {
        if (billing != null) {
            billing!!.release()
        }
        super.onDestroy()
    }
}

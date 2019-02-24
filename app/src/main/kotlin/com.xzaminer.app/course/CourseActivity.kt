package com.xzaminer.app.course

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.Toolbar
import android.text.SpannableString
import android.text.style.StrikethroughSpan
import android.text.style.UnderlineSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.LinearLayout
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simplemobiletools.commons.extensions.beGone
import com.simplemobiletools.commons.extensions.beVisible
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.views.MyButton
import com.simplemobiletools.commons.views.MyGridLayoutManager
import com.simplemobiletools.commons.views.MyRecyclerView
import com.simplemobiletools.commons.views.MyTextView
import com.xzaminer.app.R
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.billing.Purchase
import com.xzaminer.app.billing.PurchaseLog
import com.xzaminer.app.extensions.config
import com.xzaminer.app.extensions.dataSource
import com.xzaminer.app.extensions.debugDataSource
import com.xzaminer.app.studymaterial.QuizActivity
import com.xzaminer.app.studymaterial.StudyMaterial
import com.xzaminer.app.studymaterial.StudyMaterialActivity
import com.xzaminer.app.user.User
import com.xzaminer.app.utils.*
import kotlinx.android.synthetic.main.activity_course.*
import ss.com.bannerslider.Slider


class CourseActivity : SimpleActivity(), BillingProcessor.IBillingHandler {

    private var toolbar: Toolbar? = null
    private var courseId: Long = -1
    private var isBillingInitialized: Boolean = false
    private var billing: BillingProcessor? = null
    private var course: Course? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_ACTION_MODE_OVERLAY)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar?.setNavigationOnClickListener { onBackPressed() }

        intent.apply {
            courseId = getLongExtra(COURSE_ID, -1)
            if(courseId == (-1).toLong()) {
                toast("Error opening Course")
                finish()
                return
            }
        }

        dataSource.getCourseById(courseId) { course ->
            if(course != null) {
                loadCourse(course)
            } else {
                toast("Error opening course.")
                finish()
                return@getCourseById
            }
        }
    }

    private fun loadCourse(loadedCourse: Course) {
        course_title.text = loadedCourse.name
        supportActionBar?.title = loadedCourse.shortName ?: loadedCourse.name
        course = loadedCourse

         if(loadedCourse.descImages.isEmpty()) {
            desc_slider.beGone()
        } else {
            Slider.init(PicassoImageLoadingService(this))
            desc_slider.setAdapter(CourseDescriptionImageAdapter(loadedCourse.descImages))
        }

        initPurchases()
        initSections()
    }

    private fun initSections() {
        val sections = course!!.fetchVisibleSections()
        sections_root.removeAllViews()
        for(i in 0 until sections.size) {
            val section = sections[i]
            val view = LayoutInflater.from(this).inflate(R.layout.course_section_item2, null)

            sections_root.addView(view)
            val layoutParams = view.layoutParams as (LinearLayout.LayoutParams)
            val margin6Dp = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 6F, resources
                    .displayMetrics
            ).toInt()
            val margin8Dp = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 8F, resources
                    .displayMetrics
            ).toInt()
            layoutParams.setMargins(0, margin8Dp, 0, margin8Dp)

            val title = view.findViewById<MyTextView>(R.id.section_title)
            val content = SpannableString(section.name)
            content.setSpan(UnderlineSpan(), 0, content.length, 0)
            title.text = content

            val rv = view.findViewById<MyRecyclerView>(R.id.section_rv)
            setupAdapter(rv, section)

            val sectionRoot = view.findViewById<View>(R.id.section_root)
            sectionRoot.setOnClickListener {
                Intent(this, CourseSectionActivity::class.java).apply {
                    putExtra(COURSE_ID, courseId)
                    putExtra(SECTION_ID, section.id)
                    startActivity(this)
                }
            }

            initSectionPurchase(section, view)
        }
    }

    private fun setupAdapter(recyclerView: MyRecyclerView, section: CourseSection) {
        val values = ArrayList(section.studyMaterials.values)
        values.sortWith(compareBy { it.order })

        CourseStudyMaterialsAdapter(this, values.clone() as ArrayList<StudyMaterial>, recyclerView, GridLayoutManager.HORIZONTAL) {
            if(it is StudyMaterial && it.type == STUDY_MATERIAL_TYPE_STUDY_MATERIAL) {
                Intent(this, StudyMaterialActivity::class.java).apply {
                    putExtra(STUDY_MATERIAL_ID, it.id)
                    putExtra(SECTION_ID, section.id)
                    putExtra(COURSE_ID, courseId)
                    putExtra(STUDY_MATERIAL_TYPE, STUDY_MATERIAL_TYPE_STUDY_MATERIAL)
                    startActivity(this)
                }
            } else if((it as StudyMaterial).type == STUDY_MATERIAL_TYPE_QUESTION_BANK ) {
                Intent(this, QuizActivity::class.java).apply {
                    putExtra(QUIZ_ID, (it).id)
                    putExtra(SECTION_ID, section.id)
                    putExtra(COURSE_ID, courseId)
                    putExtra(IS_NEW_QUIZ, true)
                    startActivity(this)
                }
            } else if((it).type == STUDY_MATERIAL_TYPE_VIDEO ) {
                Intent(this, CourseSectionVideosDomainActivity::class.java).apply {
                    putExtra(DOMAIN_ID, (it).id)
                    putExtra(SECTION_ID, section.id)
                    putExtra(COURSE_ID, courseId)
                    startActivity(this)
                }
            }
        }.apply {
            recyclerView.adapter = this
        }
        val layoutManager = recyclerView.layoutManager as MyGridLayoutManager
        layoutManager.orientation = GridLayoutManager.HORIZONTAL
        layoutManager.spanCount = 1
    }

    private fun initPurchases() {
        price_course_root.beVisible()
        if(!isBillingInitialized) {
            billing = BillingProcessor(this, null, this)
            billing!!.initialize()
        }

        val purchases = course!!.fetchVisiblePurchases()
        if(!purchases.isEmpty()) {
            val purchase = purchases.first()
            val user = config.getLoggedInUser() as User

            if(!user.hasPurchase(purchase.id)) {
                if(purchase.originalPrice != "") {
                    val content = SpannableString("INR "+ purchase.originalPrice + " " + purchase.actualPrice)
                    content.setSpan(StrikethroughSpan(), 4, purchase.originalPrice.length + 4, 0)
                    price_value.text = content
                } else {
                    price_value.text = "INR "+ purchase.actualPrice
                }

                price_buy.setOnClickListener {
                    val user = config.getLoggedInUser() as User
                    if(isBillingInitialized) {
                        val extraParams = Bundle()
                        extraParams.putString("accountId", user.getId())
                        billing!!.purchase(this@CourseActivity, purchase.id, "::purchaseid::" + purchase.id + "::user::" + user.getId(), extraParams)
                    }
                }
            } else {
                price_course_root.beGone()
            }
        } else {
            price_course_root.beGone()
        }
    }

    private fun initSectionPurchase(section: CourseSection, view: View) {
        val user = config.getLoggedInUser() as User
        val purchasesSection = course!!.fetchVisiblePurchases()
        if(!purchasesSection.isEmpty()) {
            val purchase = purchasesSection.first()
            if(user.hasPurchase(purchase.id)) {
                view.findViewById<MyTextView>(R.id.section_buy_root).beGone()
                return
            }
        }

        val purchases = section.fetchVisiblePurchases()
        if(!purchases.isEmpty()) {
            val purchase = purchases.first()

            if(!user.hasPurchase(purchase.id)) {
                if(purchase.originalPrice != "") {
                    val content = SpannableString("INR "+ purchase.originalPrice + " " + purchase.actualPrice)
                    content.setSpan(StrikethroughSpan(), 4, purchase.originalPrice.length + 4, 0)
                    view.findViewById<MyTextView>(R.id.section_buy_price).text = content
                } else {
                    view.findViewById<MyTextView>(R.id.section_buy_price).text = "INR "+ purchase.actualPrice
                }

                view.findViewById<MyButton>(R.id.section_buy).setOnClickListener {
                    val user = config.getLoggedInUser() as User
                    if(isBillingInitialized) {
                        val extraParams = Bundle()
                        extraParams.putString("accountId", user.getId())
                        billing!!.purchase(this@CourseActivity, purchase.id, "::purchaseid::" + purchase.id + "::user::" + user.getId(), extraParams)
                    }
                }
            } else {
                view.findViewById<MyTextView>(R.id.section_buy_root).beGone()
            }
        } else {
            view.findViewById<MyTextView>(R.id.section_buy_root).beGone()
        }
    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        val listType = object : TypeToken<TransactionDetails>() {}.type
        val json = Gson().toJson(details, listType)
        val purchase = course!!.fetchPurchaseById(productId) as Purchase
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

        initPurchases()
        initSections()

        val purLog = PurchaseLog(productId, details, user)
        dataSource.addPurchaseLog(purLog)
        debugDataSource.addDebugObject(dataSource, "purchases/PurchaseAudiobookActivity", details!!)
    }

    override fun onBillingInitialized() {
        isBillingInitialized = true
//        billing!!.consumePurchase(PURCHASE_COURSE_IAP+"101")
//        billing!!.consumePurchase(PURCHASE_SECTION_IAP+"1017")
//        billing!!.consumePurchase(PURCHASE_SECTION_IAP+"1015")
//        billing!!.consumePurchase(PURCHASE_SECTION_IAP+"1012")
//        billing!!.consumePurchase(PURCHASE_SECTION_IAP+"1015")
//        billing!!.consumePurchase(PURCHASE_SECTION_IAP+"1014")
//        billing!!.consumePurchase(PURCHASE_SECTION_STUDY_MATERIAL+"101402")
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

    override fun onResume() {
        super.onResume()

        if(course != null) {
            initPurchases()
            initSections()
        }
    }
}

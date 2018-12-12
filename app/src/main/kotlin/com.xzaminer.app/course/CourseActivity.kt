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
import com.simplemobiletools.commons.extensions.getAdjustedPrimaryColor
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.views.MyGridLayoutManager
import com.simplemobiletools.commons.views.MyRecyclerView
import com.simplemobiletools.commons.views.MyTextView
import com.xzaminer.app.R
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.billing.PurchaseLog
import com.xzaminer.app.data.User
import com.xzaminer.app.extensions.config
import com.xzaminer.app.extensions.dataSource
import com.xzaminer.app.extensions.debugDataSource
import com.xzaminer.app.studymaterial.QuizActivity
import com.xzaminer.app.studymaterial.StudyMaterial
import com.xzaminer.app.studymaterial.StudyMaterialActivity
import com.xzaminer.app.utils.*
import kotlinx.android.synthetic.main.activity_course.*
import ss.com.bannerslider.Slider


class CourseActivity : SimpleActivity(), BillingProcessor.IBillingHandler {

    private var toolbar: Toolbar? = null
    private lateinit var user: User
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
        user = config.getLoggedInUser() as User

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

        initPurchases(loadedCourse)

        val sections = loadedCourse.fetchVisibleSections()
        sections_root.removeAllViews()
        for(i in 0 until sections.size) {
            val section = sections[i]
            val view = LayoutInflater.from(this).inflate(R.layout.course_section_item2, null)

            sections_root.addView(view)
            val layoutParams = view.layoutParams as (LinearLayout.LayoutParams)
            val marginInDp = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 6F, resources
                    .displayMetrics
            ).toInt()
            val margin8Dp = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 8F, resources
                    .displayMetrics
            ).toInt()
            layoutParams.setMargins(marginInDp, margin8Dp, marginInDp, margin8Dp)

            val title = view.findViewById<MyTextView>(R.id.section_title)
            val content = SpannableString(section.name)
            content.setSpan(UnderlineSpan(), 0, content.length, 0)
            title.text = content
            title.setTextColor(getAdjustedPrimaryColor())

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
        }
    }

    private fun setupAdapter(recyclerView: MyRecyclerView, section: CourseSection) {
        val values = ArrayList(section.studyMaterials.values)
        values.sortWith(compareBy { it.id })

        CourseStudyMaterialsAdapter(this, values.clone() as ArrayList<StudyMaterial>, recyclerView, GridLayoutManager.HORIZONTAL) {
            if(it is StudyMaterial && it.type == STUDY_MATERIAL_TYPE_STUDY_MATERIAL) {
                Intent(this, StudyMaterialActivity::class.java).apply {
                    putExtra(STUDY_MATERIAL_ID, (it as StudyMaterial).id)
                    putExtra(COURSE_ID, courseId)
                    putExtra(SECTION_ID, section.id)
                    putExtra(STUDY_MATERIAL_TYPE, section.type)
                    startActivity(this)
                }
            } else if((it as StudyMaterial).type == STUDY_MATERIAL_TYPE_QUESTION_BANK ) {
                Intent(this, QuizActivity::class.java).apply {
                    putExtra(QUIZ_ID, (it).id)
                    putExtra(COURSE_ID, courseId)
                    putExtra(SECTION_ID, section.id)
                    putExtra(IS_NEW_QUIZ, true)
                    startActivity(this)
                }
            } else if((it).type == STUDY_MATERIAL_TYPE_VIDEO ) {
                Intent(this, CourseSectionVideosDomainActivity::class.java).apply {
                    putExtra(DOMAIN_ID, (it).id)
                    putExtra(COURSE_ID, courseId)
                    putExtra(SECTION_ID, section.id)
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

    private fun initPurchases(loadedCourse: Course) {
        val purchases = loadedCourse.fetchVisiblePurchases()
        if(!purchases.isEmpty()) {
            val purchase = purchases.first()

            if(purchase.originalPrice != "") {
                val content = SpannableString(purchase.originalPrice + " " + purchase.actualPrice)
                content.setSpan(StrikethroughSpan(), 0, purchase.originalPrice.length, 0)
                price_value.text = content
            } else {
                price_value.text = purchase.actualPrice
            }

            price_buy.setOnClickListener {
                val user = config.getLoggedInUser() as User
                if(isBillingInitialized) {
                    val extraParams = Bundle()
                    extraParams.putString("accountId", user.getId())
                    billing!!.subscribe(this@CourseActivity, purchase.id, "::purchaseid::" + purchase.id + "::user::" + user.getId(), extraParams)
                }
            }

            billing = BillingProcessor(this, null, this)
            billing!!.initialize()
        } else {
            price_course_root.beGone()
        }
    }

    override fun onBillingInitialized() {
        isBillingInitialized = true
    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        val listType = object : TypeToken<TransactionDetails>() {}.type
        val json = Gson().toJson(details, listType)
        val purchaseFromCourse = course!!.fetchVisiblePurchases().first()
        purchaseFromCourse.details = json

        val user = config.getLoggedInUser() as User

        if(!user.hasPurchase(productId)) {
            // Add purchase to user
            user.purchases.add(purchaseFromCourse)
            // save to db
            dataSource.addUser(user)
        }

        initPurchases(course!!)

        val purLog = PurchaseLog(productId, details, user)
        dataSource.addPurchaseLog(purLog)
        debugDataSource.addDebugObject(dataSource, "purchases/PurchaseAudiobookActivity", details!!)
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
        toast("Billing error:"+errorCode+", details:"+ (error?.message ?: ""))
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

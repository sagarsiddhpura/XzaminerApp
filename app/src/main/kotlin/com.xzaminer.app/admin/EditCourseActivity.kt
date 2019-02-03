package com.xzaminer.app.admin

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.extensions.*
import com.xzaminer.app.R
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.billing.Purchase
import com.xzaminer.app.course.Course
import com.xzaminer.app.extensions.dataSource
import com.xzaminer.app.studymaterial.ConfirmDialog
import com.xzaminer.app.utils.*
import kotlinx.android.synthetic.main.activity_edit_course.*

class EditCourseActivity : SimpleActivity() {

    private var courseId: Long? = null
    var monetization = ""
    private lateinit var course: Course

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_course)
        supportActionBar?.title = "Edit Course"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        intent.apply {
            courseId = getLongExtra(CAT_ID, -1)
            if(courseId == (-1).toLong()) {
                toast("Error editing this course.")
                finish()
                return
            }
        }

        dataSource.getCourseById(courseId) { loadedCourse ->
            if(loadedCourse != null) {
                course = loadedCourse
                loadCourse(loadedCourse)
            } else {
                toast("Error opening course.")
                finish()
                return@getCourseById
            }
        }
    }

    private fun loadCourse(course: Course) {
        edit_name.setText(course.name)
        edit_desc.setText(course.desc)
        edit_short_name.setText(course.shortName)
        val options = arrayOf("None", "Monetized")
        monetization_spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)

        //item selected listener for spinner
        monetization_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, selected: Int, p3: Long) {
                monetization = options[selected]
                if(monetization == "None") {
                    monetization_root.beGone()
                } else {
                    monetization_root.beVisible()
                }
            }
        }

        if(course.fetchVisiblePurchases().isEmpty()) {
            monetization_root.beGone()
        } else if(!course.fetchVisiblePurchases().isEmpty()) {
            monetization_spinner.setSelection(1)
            monetization_root.beVisible()
            val purchase = course.fetchVisiblePurchases().first()
            purchase_id.setText(purchase.id)
            purchase_name.setText(purchase.name)
            purchase_desc.setText(purchase.desc)
            purchase_orignal.setText(purchase.originalPrice)
            purchase_actual.setText(purchase.actualPrice)
            purchase_extra_info.setText(purchase.extraPurchaseInfo)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_manage, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.manage_finish -> validateAndSaveEntity()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun validateAndSaveEntity() {
        if(edit_name.text == null || edit_name.text.toString() == "") {
            ConfirmationDialog(this, "Please enter Name", R.string.yes, R.string.ok, 0) { }
            return
        }
        if(monetization == "Monetized") {
            if(purchase_name.text == null || purchase_name.text.toString() == "") {
                ConfirmationDialog(this, "Please fill purchase name", R.string.yes, R.string.ok, 0) { }
                return
            }
            if(purchase_actual.text == null || purchase_actual.text.toString() == "") {
                ConfirmationDialog(this, "Please fill Actual Price", R.string.yes, R.string.ok, 0) { }
                return
            }
            if(purchase_id.text == null || purchase_id.text.toString() == "") {
                ConfirmationDialog(this, "Please fill Purchase id", R.string.yes, R.string.ok, 0) { }
                return
            }
        }

        ConfirmDialog(this, "Are you sure you want to finish Quiz?") {
            course.name = edit_name.text.toString()
            course.desc = edit_desc.text.toString()
            course.shortName = edit_short_name.text.toString()

            course.purchaseInfo.clear()
            if (monetization == "Monetized") {
                course.purchaseInfo.addAll(
                    arrayListOf(
                        Purchase(
                            purchase_id.text.toString() , purchase_name.text.toString(), purchase_desc.text.toString(),
                            PURCHASE_TYPE_IAP, purchase_actual.text.toString(), purchase_orignal.text.toString(), true, null, null, purchase_extra_info.text.toString())
                    ))
            }

            dataSource.updateCourseProperties(course)
            ConfirmationDialog(this, "Course has been updated successfully", R.string.yes, R.string.ok, 0) { }
        }
    }
}

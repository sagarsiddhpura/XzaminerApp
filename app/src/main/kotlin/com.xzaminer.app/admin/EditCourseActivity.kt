package com.xzaminer.app.admin

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.dialogs.FilePickerDialog
import com.simplemobiletools.commons.extensions.beGone
import com.simplemobiletools.commons.extensions.beVisible
import com.simplemobiletools.commons.extensions.getAdjustedPrimaryColor
import com.simplemobiletools.commons.extensions.toast
import com.xzaminer.app.R
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.billing.Purchase
import com.xzaminer.app.course.Course
import com.xzaminer.app.extensions.dataSource
import com.xzaminer.app.extensions.loadIconImageView
import com.xzaminer.app.extensions.loadImageImageView
import com.xzaminer.app.studymaterial.ConfirmDialog
import com.xzaminer.app.utils.COURSE_ID
import com.xzaminer.app.utils.PURCHASE_TYPE_IAP
import kotlinx.android.synthetic.main.activity_edit_course.*
import java.io.File

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
            courseId = getLongExtra(COURSE_ID, -1)
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

        edit_edit_image.setOnClickListener {
            FilePickerDialog(this, Environment.getExternalStorageDirectory().toString(), true, false, false) {
                toast("Uploading image. Please wait till image is uploaded.")
                val imageFile = File(it)
                val file = Uri.fromFile(imageFile)
                val name = course.id.toString() + "_" + imageFile.name
                val riversRef = dataSource.getStorage().getReference("courses/" + course.id + "/").child(name)
                val uploadTask = riversRef.putFile(file)

                uploadTask.addOnFailureListener {
                    toast("Failed to Upload Image")
                }.addOnSuccessListener {
                    toast("Image Uploaded successfully. Save to update course")
                    course.image = "courses/" + courseId + "/" + name
                    loadImageImageView(course.image!!, edit_course_image, false, null, false, R.drawable.im_placeholder_video)
                }
            }
        }

        edit_delete_image.setOnClickListener {
            course.image = null
            edit_course_image.setImageDrawable(null)
        }

        edit_short_name_root.beVisible()
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

        edit_edit_image.setColorFilter(getAdjustedPrimaryColor())
        edit_delete_image.setColorFilter(getAdjustedPrimaryColor())
    }

    private fun loadCourse(course: Course) {
        edit_name.setText(course.name)
        edit_desc.setText(course.desc)
        edit_short_name.setText(course.shortName)
        if(course.image != null && course.image != "") {
            loadImageImageView(course.image!!, edit_course_image, false, null, false, R.drawable.im_placeholder_video)
        } else {
            val img : Int = R.drawable.im_placeholder
            loadIconImageView(img, edit_course_image, false)
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

        val options = arrayOf("Yes", "No")
        visibility_spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)

        if(course.isVisible) {
            visibility_spinner.setSelection(0)
        } else {
            visibility_spinner.setSelection(1)
        }

        order_value.setText(course.order.toString())
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
        try {
            Integer.parseInt(order_value.text.toString())
        } catch (e : Exception) {
            ConfirmationDialog(this, "Please fill correct numeric value for Order", R.string.yes, R.string.ok, 0) { }
            return
        }

        ConfirmDialog(this, "Are you sure you want to update the Course?") {
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
            try {
                course.order = Integer.parseInt(order_value.text.toString())
            } catch (e : Exception) { }

            when {
                visibility_spinner.selectedItemPosition == 0 -> course.isVisible = true
                visibility_spinner.selectedItemPosition == 1 -> course.isVisible = false
            }
            try {
                course.order = Integer.parseInt(order_value.text.toString())
            } catch (e : Exception) { }

            when {
                visibility_spinner.selectedItemPosition == 0 -> course.isVisible = true
                visibility_spinner.selectedItemPosition == 1 -> course.isVisible = false
            }

            dataSource.updateCourseProperties(course)
            ConfirmationDialog(this, "Course has been updated successfully", R.string.yes, R.string.ok, 0) { }
        }
    }
}

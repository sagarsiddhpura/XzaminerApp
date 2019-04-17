package com.xzaminer.app.admin

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.Menu
import android.view.MenuItem
import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.dialogs.FilePickerDialog
import com.simplemobiletools.commons.extensions.beGone
import com.simplemobiletools.commons.extensions.beVisible
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.views.MyGridLayoutManager
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.course.Course
import com.xzaminer.app.extensions.dataSource
import com.xzaminer.app.studymaterial.ConfirmDialog
import com.xzaminer.app.utils.COURSE_ID
import kotlinx.android.synthetic.main.activity_edit_course.*
import java.io.File
import java.util.*


class EditCourseImagesActivity : SimpleActivity(), OnStartDragListener {

    private var courseId: Long? = null
    private lateinit var course: Course
    private var mItemTouchHelper: ItemTouchHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.xzaminer.app.R.layout.activity_edit_course)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        intent.apply {
            courseId = getLongExtra(COURSE_ID, -1)
            if(courseId == (-1).toLong()) {
                toast("Error editing this course.")
                finish()
                return
            }
        }

        supportActionBar?.title = "Edit Shideshow Images"
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
        setupGridLayoutManager()
    }

    private fun loadCourse(course: Course) {
        edit_image_root.beGone()
        edit_file_root.beGone()
        edit_content.beGone()
        monetization_root.beGone()
        order_root.beGone()
        monetization_label.beGone()
        monetization_spinner.beGone()
        edit_short_name_root.beGone()
        edit_name.beGone()
        edit_desc.beGone()
        visibility_label.beGone()
        visibility_spinner.beGone()
        purchases_holder.beVisible()
        purchases_label.beGone()

        setupAdapter(course.descImages)
    }

    private fun setupGridLayoutManager() {
        val layoutManager = quizzes_grid.layoutManager as MyGridLayoutManager
        layoutManager.orientation = GridLayoutManager.VERTICAL
        layoutManager.spanCount = 1
    }

    private fun setupAdapter(images: ArrayList<String>) {
        val currAdapter = quizzes_grid.adapter
        if (currAdapter == null) {
            EditCourseImagesAdapter(
                this,
                images.clone() as ArrayList<String>,
                quizzes_grid
            ) {
            }.apply {
                quizzes_grid.adapter = this
                val callback = SimpleItemTouchHelperCallback(this)
                mItemTouchHelper = ItemTouchHelper(callback)
                mItemTouchHelper!!.attachToRecyclerView(quizzes_grid)
            }
        } else {
            (currAdapter as EditCourseImagesAdapter).updateEntities(images)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(com.xzaminer.app.R.menu.menu_manage, menu)
        menu.apply {
            findItem(com.xzaminer.app.R.id.manage_add).isVisible = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            com.xzaminer.app.R.id.manage_finish -> validateAndSaveEntity()
            com.xzaminer.app.R.id.manage_add -> addImage()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun addImage() {
        FilePickerDialog(this, Environment.getExternalStorageDirectory().toString(), true, false, false) {
            val imageFile = File(it)
            val file = Uri.fromFile(imageFile)
            val name = courseId.toString() + "_desc_" + imageFile.name
            val riversRef = dataSource.getStorage().getReference("courses/" + courseId + "/").child(name)
            val uploadTask = riversRef.putFile(file)
            toast("Uploading image...")
            uploadTask.addOnFailureListener {
                toast("Failed to add Image")
            }.addOnSuccessListener {
                course.descImages.add("courses/" + courseId + "/" + name)
                setupAdapter(course.descImages)
            }
        }
    }

    private fun validateAndSaveEntity() {
        ConfirmDialog(this, "Are you sure you want to update the Course Images?") {
            dataSource.updateCourseImages(course)
            ConfirmationDialog(this, "Course images has been updated successfully", com.xzaminer.app.R.string.yes, com.xzaminer.app.R.string.ok, 0) { }
        }
    }

    fun deleteEntity(image: String) {
        course.descImages.remove(image)
        setupAdapter(course.descImages)
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        mItemTouchHelper!!.startDrag(viewHolder)
    }

    fun onItemMove(fromPosition: Int, toPosition: Int) {
        Collections.swap(course.descImages, fromPosition, toPosition)
        setupAdapter(course.descImages)
    }
}

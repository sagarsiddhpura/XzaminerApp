package com.xzaminer.app.user

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.extensions.beGone
import com.simplemobiletools.commons.extensions.beVisible
import com.xzaminer.app.R
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.admin.EditCourseImagesActivity
import com.xzaminer.app.course.Course
import com.xzaminer.app.extensions.config
import com.xzaminer.app.extensions.dataSource
import com.xzaminer.app.studymaterial.ConfirmDialog
import com.xzaminer.app.utils.COURSE_ID
import kotlinx.android.synthetic.main.activity_edit_course.*

class EditUserProfileActivity : SimpleActivity() {

    private lateinit var course: Course

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_course)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportActionBar?.title = "Edit User Profile"
        val user = dataSource.getLoggedInUser(this) as User
        loadUser(user)

        edit_desc.isEnabled = false
        edit_image_root.beGone()
        edit_file_root.beGone()
        edit_content.beGone()
        visibility_label.beGone()
        visibility_spinner.beGone()
        order_root.beGone()
        monetization_label.beGone()
        monetization_root.beGone()
        monetization_spinner.beGone()
        edit_short_name_root.beVisible()
        edit_desc_root.hint = "Email"
        edit_short_name_root.hint = "Phone"
        edit_short_name.isEnabled = false
    }

    private fun loadUser(user: User) {
        edit_name.setText(user.name)
        edit_desc.setText(user.email)
        edit_short_name.setText(user.phone)

        edit_content.setOnClickListener {
            Intent(this, EditCourseImagesActivity::class.java).apply {
                putExtra(COURSE_ID, course.id)
                startActivity(this)
            }
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

        ConfirmDialog(this, "Are you sure you want to update the Details?") {
            val user = dataSource.getLoggedInUser(this) as User
            user.name = edit_name.text.toString()
            config.setLoggedInUser(user)
            dataSource.updateUser(user)
            val userFirebase = FirebaseAuth.getInstance().currentUser
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(user.name).build()
            userFirebase?.updateProfile(profileUpdates)
            ConfirmationDialog(this, "Details has been updated successfully", R.string.yes, R.string.ok, 0) { }
        }
    }
}

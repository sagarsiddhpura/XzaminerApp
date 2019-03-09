package com.xzaminer.app.admin

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.extensions.beGone
import com.simplemobiletools.commons.extensions.beVisible
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.views.MyGridLayoutManager
import com.xzaminer.app.BuildConfig
import com.xzaminer.app.R
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.billing.ListUserPurchasesAdapter
import com.xzaminer.app.billing.Purchase
import com.xzaminer.app.extensions.config
import com.xzaminer.app.extensions.dataSource
import com.xzaminer.app.studymaterial.ConfirmDialog
import com.xzaminer.app.user.User
import com.xzaminer.app.utils.USERTYPE_ADMIN
import com.xzaminer.app.utils.USERTYPE_NORMAL
import com.xzaminer.app.utils.USER_ID
import kotlinx.android.synthetic.main.activity_edit_course.*


class EditUserActivity : SimpleActivity() {

    private var userId: String? = null
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_course)
        supportActionBar?.title = "Edit Section"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        intent.apply {
            userId = getStringExtra(USER_ID)
            if(userId == null) {
                toast("Error editing User")
                finish()
                return
            }
        }

        val currentUser = config.getLoggedInUser() as User
        if(!BuildConfig.DEBUG) {
            if(currentUser.userType != USERTYPE_ADMIN) {
                toast("This user is not authorized to Edit Users.")
                finish()
            }
        }

        dataSource.getUser(userId!!) { loadedUser ->
            if(loadedUser != null) {
                user = loadedUser
                loadUser(loadedUser!!)
            } else {
                toast("Error editing User")
                finish()
                return@getUser
            }
        }
    }

    private fun loadUser(loadedUser: User) {
        edit_name.setText(loadedUser.name)
        edit_desc_root.hint = "Email ID"
        edit_desc.setText(loadedUser.email)
        edit_short_name_root.hint = "Phone Number"
        edit_short_name.setText(loadedUser.phone)
        visibility_label.text = "User Type"

        edit_image_root.beGone()
        edit_file_root.beGone()
        edit_content.beGone()
        monetization_root.beGone()
        order_root.beGone()
        monetization_label.beGone()
        monetization_spinner.beGone()
        edit_short_name_root.beVisible()
        purchases_holder.beVisible()
        edit_name.isEnabled = false
        edit_desc.isEnabled = false
        edit_short_name.isEnabled = false

        val options = arrayOf("Normal", "Admin")
        visibility_spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)

        if(loadedUser.userType == USERTYPE_ADMIN) {
            visibility_spinner.setSelection(1)
        } else {
            visibility_spinner.setSelection(0)
        }

        val layoutManager = quizzes_grid.layoutManager as MyGridLayoutManager
        layoutManager.orientation = GridLayoutManager.VERTICAL
        layoutManager.spanCount = 1

        val currAdapter = quizzes_grid.adapter
        if (currAdapter == null) {
            ListUserPurchasesAdapter(
                this,
                user.purchases.clone() as ArrayList<Purchase>,
                quizzes_grid
            ) {
            }.apply {
                quizzes_grid.adapter = this
                addVerticalDividers(true)
            }
        } else {
            (currAdapter as ListUserPurchasesAdapter).updatePurchases(user.purchases)
        }
        if(user.purchases.isEmpty()) {
            quizzes_empty_text_label.text = "No User Purchases"
            quizzes_empty_text_label.beVisible()
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
        ConfirmDialog(this, "Are you sure you want to update the User?") {
            when {
                visibility_spinner.selectedItemPosition == 0 -> user.userType = USERTYPE_NORMAL
                visibility_spinner.selectedItemPosition == 1 -> user.userType = USERTYPE_ADMIN
            }
            dataSource.updateUser(user)
            ConfirmationDialog(this, "Section has been updated successfully", R.string.yes, R.string.ok, 0) { }
        }
    }
}

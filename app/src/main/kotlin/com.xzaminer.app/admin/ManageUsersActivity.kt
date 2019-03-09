package com.xzaminer.app.admin

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.Toolbar
import android.view.Window
import com.google.firebase.auth.FirebaseAuth
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.views.MyGridLayoutManager
import com.xzaminer.app.BuildConfig
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.SplashActivity
import com.xzaminer.app.extensions.config
import com.xzaminer.app.extensions.dataSource
import com.xzaminer.app.studymaterial.ConfirmDialog
import com.xzaminer.app.user.User
import com.xzaminer.app.utils.USERTYPE_ADMIN
import com.xzaminer.app.utils.USER_ID
import kotlinx.android.synthetic.main.activity_quiz.*


class ManageUsersActivity : SimpleActivity() {

    private var toolbar: Toolbar? = null
    private lateinit var users: java.util.ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_ACTION_MODE_OVERLAY)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(com.xzaminer.app.R.layout.activity_quiz)

        toolbar = findViewById(com.xzaminer.app.R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar?.setNavigationOnClickListener { onBackPressed() }
        supportActionBar?.title = "Manage Users"

        val user = config.getLoggedInUser() as User
        if(!BuildConfig.DEBUG) {
            if(user.userType != USERTYPE_ADMIN) {
                toast("This user is not authorized to Manage Users.")
                finish()
            }
        }

        setupGridLayoutManager()
        loadUsers()
    }

    private fun loadUsers() {
        dataSource.getUsers { users ->
            if(users != null) {
                loadUser(users)
            } else {
                showErrorAndExit()
                return@getUsers
            }
        }
    }

    private fun showErrorAndExit() {
        toast("Error Opening Manage Users")
        finish()
    }

    private fun loadUser(loadedUsers: java.util.ArrayList<User>) {
        this.users = loadedUsers
        setupAdapter(loadedUsers)
    }

    private fun getRecyclerAdapter() = quiz_grid.adapter as? ManageUsersAdapter

    private fun setupGridLayoutManager() {
        val layoutManager = quiz_grid.layoutManager as MyGridLayoutManager
        layoutManager.orientation = GridLayoutManager.VERTICAL
        layoutManager.spanCount = 1
    }

    private fun setupAdapter(loadedUsers: java.util.ArrayList<User>) {
        val currAdapter = quiz_grid.adapter
        if (currAdapter == null) {
            ManageUsersAdapter(
                this,
                loadedUsers.clone() as ArrayList<User>,
                quiz_grid
            ) {
            }.apply {
                quiz_grid.adapter = this
                addVerticalDividers(true)
            }
        } else {
            (currAdapter as ManageUsersAdapter).updateUsers(loadedUsers)
        }
    }

    private fun refreshQuestions(users: java.util.ArrayList<User>) {
        getRecyclerAdapter()?.updateUsers(users)
    }

    fun editUser(user: User) {
        Intent(this, EditUserActivity::class.java).apply {
            putExtra(USER_ID, user.getId())
            startActivity(this)
        }
    }

    fun deleteUser(userXzaminerDb: User) {
        val currentUser = config.getLoggedInUser() as User
        if(currentUser.getId() == userXzaminerDb.getId()) {
            toast("Cannot delete current logged in user")
            return
        }

        ConfirmDialog(this, "Are you sure you want to Delete this user?") {
            // Login as target user to delete itself
            FirebaseAuth.getInstance().signInWithEmailAndPassword(userXzaminerDb.email, userXzaminerDb.password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = FirebaseAuth.getInstance().currentUser
                        // Delete
                        user?.delete()?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Login as original user
                                    FirebaseAuth.getInstance().signInWithEmailAndPassword(currentUser.email, currentUser.password)
                                        .addOnCompleteListener(this) { task ->
                                            if (task.isSuccessful) {
                                                // delete from DB
                                                dataSource.deleteUser(userXzaminerDb)
                                                toast("Successfully Deleted User")
                                                users = users.filter {  it.getId() != userXzaminerDb.getId() } as ArrayList<User>
                                                refreshQuestions(users)
                                            } else {
                                                // Error logging as original user.
                                                toast("Error deleting user. Please login again to continue")
                                                dataSource.logout()
                                                config.setLoggedInUser(null)
                                                config.isOtpVerified = false
                                                startActivity(Intent(applicationContext, SplashActivity::class.java))
                                            }
                                        }
                                } else {
                                    toast("Error deleting user")
                                }
                            }
                    } else {
                        toast("Error fetching user from Firebase Database")
//                        // Login as original user
//                        FirebaseAuth.getInstance().signInWithEmailAndPassword(currentUser.email, currentUser.password)
//                            .addOnCompleteListener(this) { task ->
//                                if (task.isSuccessful) {
//                                    // delete from DB
//                                    dataSource.deleteUser(userXzaminerDb)
//                                    users = users.filter {  it.getId() != userXzaminerDb.getId() } as ArrayList<User>
//                                    refreshQuestions(users)
//                                } else {
//                                    // Error logging as original user.
//                                    toast("Error deleting user. Please login again to continue")
//                                    dataSource.logout()
//                                    config.setLoggedInUser(null)
//                                    config.isOtpVerified = false
//                                    startActivity(Intent(applicationContext, SplashActivity::class.java))
//                                }
//                            }
                    }
                }
        }
    }

    override fun onResume() {
        super.onResume()
        loadUsers()
    }
}

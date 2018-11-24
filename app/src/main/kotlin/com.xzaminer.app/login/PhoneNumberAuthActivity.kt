package com.xzaminer.app.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.Window
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.simplemobiletools.commons.extensions.toast
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.SplashActivity
import com.xzaminer.app.category.MainActivity
import com.xzaminer.app.extensions.config
import com.xzaminer.app.extensions.dataSource

class PhoneNumberAuthActivity : SimpleActivity() {

    private var email: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        supportRequestWindowFeature(Window.FEATURE_ACTION_MODE_OVERLAY)
        super.onCreate(savedInstanceState)

        val loggedInUser = config.getLoggedInUser()
        if(loggedInUser != null && (TextUtils.isEmpty(loggedInUser.email) || TextUtils.isEmpty(loggedInUser.password))) {
            config.setLoggedInUser(null)
            startActivity(Intent(this, SplashActivity::class.java))
            finish()
            return
        }

        // Choose authentication providers
        val providers = arrayListOf(
                AuthUI.IdpConfig.PhoneBuilder().build())

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                1337)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1337) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                val user = FirebaseAuth.getInstance().currentUser
                val loggedInUser = config.getLoggedInUser()
                if(loggedInUser != null && (TextUtils.isEmpty(loggedInUser.email) || TextUtils.isEmpty(loggedInUser.password))) {
                    config.setLoggedInUser(null)
                    startActivity(Intent(this, SplashActivity::class.java))
                    finish()
                    return
                }

                val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(loggedInUser!!.name).build()
                user?.updateProfile(profileUpdates)

                val credential = EmailAuthProvider.getCredential(loggedInUser.email, loggedInUser.password)
                user?.linkWithCredential(credential)
                        ?.addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                sendEmailVerification(loggedInUser.email)

                                loggedInUser.phone = user.phoneNumber
                                config.setLoggedInUser(loggedInUser)
                                dataSource.addUser(loggedInUser)

                                FirebaseStorage.getInstance().getReference("images/im_concept.png").downloadUrl.addOnSuccessListener { uri ->
                                    val idToken = uri.toString().split("token=")[1]
                                    loggedInUser.token = idToken
                                    config.setLoggedInUser(loggedInUser)
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finish()
                                    return@addOnSuccessListener
                                }
                            } else {
//                                Log.w(TAG, "linkWithCredential:failure", task.exception)
                                if(task.exception != null && task.exception!!.message == "User has already been linked to the given provider.") {
                                    toast("This mobile number is already associated with another account. Please use another number and try again.")
                                    startActivity(Intent(this, PhoneNumberAuthActivity::class.java))
                                    finish()
                                    return@addOnCompleteListener
                                }

                                if(task.exception != null && task.exception!!.message != null) {
                                    toast(task.exception!!.message!!)
                                    finish()
                                }
                                val exception = task.exception
                            }
                        }
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                if(response != null && response.error != null && response.error!!.message != null) {
                    toast(response.error!!.message!!)
                    finish()
                    return
                } else {
                    startActivity(Intent(this, EmailPasswordActivity::class.java))
                    finish()
                    return
                }
            }
        }
    }

    private fun sendEmailVerification(email: String) {
        // Send verification email
        val user = FirebaseAuth.getInstance().currentUser
        user?.sendEmailVerification()
                ?.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(baseContext,
                                "Verification email sent to ${user.email} ",
                                Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(baseContext,
                                "Failed to send verification email.",
                                Toast.LENGTH_SHORT).show()
                    }
                }
    }
}

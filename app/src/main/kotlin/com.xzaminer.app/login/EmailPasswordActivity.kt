package com.xzaminer.app.login

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.simplemobiletools.commons.extensions.beGone
import com.simplemobiletools.commons.extensions.beVisible
import com.simplemobiletools.commons.extensions.toast
import com.xzaminer.app.R
import com.xzaminer.app.category.MainActivity
import com.xzaminer.app.user.User
import com.xzaminer.app.extensions.config
import com.xzaminer.app.extensions.dataSource
import kotlinx.android.synthetic.main.activity_emailpassword.*


class EmailPasswordActivity : BaseLoginActivity(), View.OnClickListener {

    private lateinit var auth: FirebaseAuth

    public override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        supportRequestWindowFeature(Window.FEATURE_ACTION_MODE_OVERLAY)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emailpassword)

        // Buttons
        emailSignInButton.setOnClickListener(this)
        emailCreateAccountButton.setOnClickListener(this)
        forgotPasswordButton.setOnClickListener(this)
        forgotPasswordButton.paintFlags = Paint.UNDERLINE_TEXT_FLAG;

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        signin_text.setOnClickListener {
            welcome_root.beGone()
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun createAccount(email: String, password: String) {
        Log.d(TAG, "createAccount:$email")

        fieldEmail.beVisible()
        fieldName.beVisible()
        fieldPassword.beVisible()
        fieldPasswordRepeat.beVisible()

        if (!validateForm(false)) {
            return
        }

        showProgressDialog()

        val user = User(fieldName?.text?.toString() ?: "", email, password, "", null, null)
        config.setLoggedInUser(user)
        startActivity(Intent(this, PhoneNumberAuthActivity::class.java))
        hideProgressDialog()
        finish()
    }

    private fun signIn(email: String, password: String) {
        Log.d(TAG, "signIn:$email")

        fieldEmail.beVisible()
        fieldName.beGone()
        fieldPassword.beVisible()
        fieldPasswordRepeat.beGone()

        if (!validateForm(true)) {
            return
        }

        showProgressDialog()

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success")
                        val user = auth.currentUser
                        dataSource.getUser(User.replace(user?.email ?: "") + User.replace(user?.phoneNumber ?: "")) { userFirebase ->
                            if(userFirebase != null) {
                                user?.getIdToken(true)?.addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        FirebaseStorage.getInstance().getReference("images/im_concept.png").downloadUrl.addOnSuccessListener { uri ->
                                            val idToken = uri.toString().split("token=")[1]
                                            userFirebase!!.token = idToken
                                            config.setLoggedInUser(userFirebase)
                                            if(user.phoneNumber == null) {
                                                startActivity(Intent(this, PhoneNumberAuthActivity::class.java))
                                            } else {
                                                startActivity(Intent(this, MainActivity::class.java))
                                            }

                                            hideProgressDialog()
                                            finish()
                                        }
                                    }
                                }
                            } else {
                                val userFirebase = User(
                                    user!!.displayName ?: "",
                                    email,
                                    password,
                                    "",
                                    null,
                                    user.phoneNumber
                                )
                                user.getIdToken(true).addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        FirebaseStorage.getInstance().getReference("images/im_concept.png").downloadUrl.addOnSuccessListener { uri ->
                                            val idToken = uri.toString().split("token=")[1]
                                            userFirebase.token = idToken
                                            dataSource.addUser(userFirebase)
                                            if(user.phoneNumber == null) {
                                                startActivity(Intent(this, PhoneNumberAuthActivity::class.java))
                                            } else {
                                                startActivity(Intent(this, MainActivity::class.java))
                                            }

                                            hideProgressDialog()
                                            finish()
                                        }
                                    }
                                }
//                                // TODO: handle non-existing user
//                                Log.w(TAG, "signInWithEmail:failure", task.exception)
//                                Toast.makeText(baseContext, "Authentication failed.",
//                                        Toast.LENGTH_SHORT).show()
//                                updateUI(null)
//                                hideProgressDialog()
                            }
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        updateUI(null)
                        hideProgressDialog()
                    }

                    if (!task.isSuccessful) {
//                        status.setText(R.string.auth_failed)
                        hideProgressDialog()
                    }
                }
    }

    private fun signOut() {
        auth.signOut()
        updateUI(null)
    }

    private fun sendEmailVerification() {
        // Send verification email
        val user = auth.currentUser
        user?.sendEmailVerification()
                ?.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(baseContext,
                                "Verification email sent to ${user.email} ",
                                Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e(TAG, "sendEmailVerification", task.exception)
                        Toast.makeText(baseContext,
                                "Failed to send verification email.",
                                Toast.LENGTH_SHORT).show()
                    }
                }
    }

    private fun validateForm(isSignin: Boolean): Boolean {
        var valid = true

        val email = fieldEmail.text.toString()
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            fieldEmail.error = "Please enter proper email."
            valid = false
        } else {
            fieldEmail.error = null
        }

        val password = fieldPassword.text.toString()
        if (TextUtils.isEmpty(password)) {
            fieldPassword.error = "Required."
            valid = false
        } else {
            fieldPassword.error = null
        }

        if (password.length < 6) {
            fieldPassword.error = "Password should be atleast 6 characters."
            valid = false
        } else {
            fieldPassword.error = null
        }

        if(!isSignin) {
            val name = fieldName.text.toString()
            if (TextUtils.isEmpty(name)) {
                fieldName.error = "Required."
                valid = false
            } else {
                fieldName.error = null
            }

            val passwordRepeat = fieldPasswordRepeat.text.toString()
            if (TextUtils.isEmpty(passwordRepeat)) {
                fieldPasswordRepeat.error = "Required."
                valid = false
            } else {
                fieldPasswordRepeat.error = null
            }

            if (password != passwordRepeat) {
                fieldPasswordRepeat.error = "Passwords must be same."
                valid = false
            } else {
                fieldPasswordRepeat.error = null
            }
        }

        return valid
    }

    private fun updateUI(user: FirebaseUser?) {
        hideProgressDialog()
    }

    override fun onClick(v: View) {
        val i = v.id
        when (i) {
            R.id.emailCreateAccountButton -> createAccount(fieldEmail.text.toString(), fieldPassword.text.toString())
            R.id.emailSignInButton -> signIn(fieldEmail.text.toString(), fieldPassword.text.toString())
            R.id.forgotPasswordButton -> forgotPassword()
        }
    }

    private fun forgotPassword() {
        Log.d(TAG, "forgotPassword")

        fieldEmail.beVisible()
        fieldName.beGone()
        fieldPassword.beGone()
        fieldPasswordRepeat.beGone()

        val email = fieldEmail.text.toString()
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            fieldEmail.error = "Please enter proper email."
            return
        } else {
            fieldEmail.error = null
        }

        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        toast("Email Sent. Please follow email to reset password.")
                    } else {
                        if(task.exception != null && task.exception!!.message != null) {
                            toast("Error sending password reset mail: " + task.exception!!.message)
                        }
                        val exception = task.exception
                        val exception2 = task.exception
                    }
                }
    }

    companion object {
        private const val TAG = "EmailPassword"
    }
}

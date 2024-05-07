package com.example.shopease

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.shopease.dataClasses.User
import com.example.shopease.dbHelpers.UsersDatabaseHelper
import com.example.shopease.viewModels.UserViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var usernameEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var signupButton: Button
    private lateinit var auth: FirebaseAuth
    private val userViewModel: UserViewModel by viewModels()
    private lateinit var dbHelper: UsersDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        usernameEditText = findViewById(R.id.usernameLoginEditText)
        passwordEditText = findViewById(R.id.passwordLoginEditText)
        loginButton = findViewById(R.id.loginButton)
        signupButton = findViewById(R.id.signupButton)
        auth = FirebaseAuth.getInstance()
        dbHelper = UsersDatabaseHelper(applicationContext)

        // Check for locally stored user credentials
        checkForLocallyStoredUser()
    }

    private fun checkForLocallyStoredUser() {
        val locallyStoredUser = dbHelper.getLocallyStoredUser()

        if (locallyStoredUser != null) {
            showToast("היי, ${locallyStoredUser.username}.")
            navigateToHomeActivity(locallyStoredUser)
            finish()
        }
    }

    fun onLoginButtonClick(view: View) {
        val email = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()

        // Check if the login is valid
        dbHelper.login(email, password, object : UsersDatabaseHelper.LoginCallback {
            // Fetch user information from the database
            override fun onLoginResult(user: User?) {
                if (user != null) {
                    showToast("התחברת בהצלחה.${user.username}!")
                    navigateToHomeActivity(user)
                    finish()
                } else {
                    // Login failed, user object is null
                    showToast("בעיה בהתחברות")
                }
            }
        })
    }

    fun onSignUpButtonClick(view: View) {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun navigateToHomeActivity(user: User) {
        userViewModel.user = user
        startActivity(Intent(this, HomeActivity::class.java))
        finish() // Ensure no operations are performed after this line
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

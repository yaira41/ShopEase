package com.example.shopease

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.shopease.dataClasses.User
import com.example.shopease.dbHelpers.UsersDatabaseHelper
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var signupButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var dbHelper: UsersDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        usernameEditText = findViewById(R.id.usernameLoginEditText)
        passwordEditText = findViewById(R.id.passwordLoginEditText)
        loginButton = findViewById(R.id.loginButton)
        signupButton = findViewById(R.id.signupButton)
        auth = FirebaseAuth.getInstance()
        dbHelper = UsersDatabaseHelper()
        checkConnection()
    }

    private fun checkConnection() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            dbHelper.getUserByUid(currentUser.uid, object : UsersDatabaseHelper.GetUserCallback {
                override fun onUserResult(user: User?) {
                    if (user != null) {
                        showToast("היי, ראינו שהתחברת כבר.")
                        navigateToHomeActivity(user) // You can pass the currentUser object to your HomeActivity
                        finish()
                    }
                }
            })
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
                    // Login successful, user object is not null
                    // You can access user properties here
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
        // Pass user information to HomeActivity
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("USER_KEY", user)
        }
        startActivity(intent)
        finish() // Ensure no operations are performed after this line
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

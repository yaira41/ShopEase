package com.example.shopease

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.shopease.Utils.base64ToByteArray
import com.example.shopease.Utils.hashPassword

class LoginActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var signupButton: Button

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        usernameEditText = findViewById(R.id.usernameLoginEditText)
        passwordEditText = findViewById(R.id.passwordLoginEditText)
        loginButton = findViewById(R.id.loginButton)
        signupButton = findViewById(R.id.signupButton)

        dbHelper = DatabaseHelper()
    }

    fun onLoginButtonClick(view: View) {
        val username = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()

        // Check if the login is valid
        dbHelper.isValidLogin(username, hashPassword(password), object : LoginCallback {
            // Fetch user information from the database
            override fun onLoginResult(user: User?) {
                if (user != null) {
                    // Login successful, user object is not null
                    // You can access user properties here
                    showToast("Login successful. Welcome, ${user.username}!")
                    navigateToHomeActivity(user)
                    finish()
                } else {
                    // Login failed, user object is null
                    showToast("Invalid login credentials.")
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
            putExtra("USERNAME_KEY", user.username)
            putExtra("EMAIL_KEY", user.email)
            putExtra("PROFILE_IMAGE_KEY", base64ToByteArray(user.profileImage))
        }
        startActivity(intent)
        finish()
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

package com.example.shopease

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

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

        dbHelper = DatabaseHelper(this)
    }

    fun onLoginButtonClick(view: View) {
        val username = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()

        // Check if the login is valid
        if (dbHelper.isValidLogin(username, password)) {
            // Fetch user information from the database
            val user = dbHelper.getUserByUsername(username)

            if (user != null) {
                // Login successful
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                navigateToHomeActivity(user)
            } else {
                // Login failed
                Toast.makeText(this, "Invalid Username or Password", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Login is not valid
            Toast.makeText(this, "Invalid Login Information", Toast.LENGTH_SHORT).show()
        }
    }

    fun onSignUpButtonClick(view: View) {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToHomeActivity(user: User) {
        // Pass user information to HomeActivity
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("USERNAME_KEY", user.username)
            putExtra("EMAIL_KEY", user.email)
            putExtra("PROFILE_IMAGE_KEY", user.profileImage)
        }
        startActivity(intent)
        finish()
    }
}

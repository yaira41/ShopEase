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

        if (dbHelper.isValidLogin(username, password)) {
            // Login successful, navigate to HomeActivity
            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
            // Pass the data as extras in the Intent
            val email = dbHelper.getEmailByUsername(username).toString()
            navigateToHomeActivity(email, username)
        } else {
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
        }
    }

    fun onSignUpButtonClick(view: View) {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToHomeActivity(email: String, username: String) {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("USERNAME_KEY", username)
        intent.putExtra("EMAIL_KEY", email)
        startActivity(intent)
        finish()
    }
}

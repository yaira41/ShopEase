// src/main/java/com/example/myloginapp/RegisterActivity.kt
package com.example.shopease

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        emailEditText = findViewById(R.id.emailEditText)
        usernameEditText = findViewById(R.id.usernameRegisterEditText)
        passwordEditText = findViewById(R.id.passwordRegisterEditText)
        registerButton = findViewById(R.id.registerButton)

        dbHelper = DatabaseHelper(this)
    }

    fun onRegisterButtonClick(view: View) {
        val email = emailEditText.text.toString()
        val username = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()


        if (dbHelper.isUserRegistered(username)) {
            Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show()
        } else {
            // Save user data in the database
            dbHelper.saveUserData(email, username, password)

            // You can perform additional actions here, such as navigating to the main activity
            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
            navigateToLoginActivity()
        }
    }

    private fun navigateToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}

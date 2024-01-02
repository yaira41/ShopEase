package com.example.shopease

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayOutputStream

class RegisterActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var imageProfile: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        usernameEditText = findViewById(R.id.usernameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        imageProfile = findViewById(R.id.imageProfile)
        btnSelectImage = findViewById(R.id.btnSelectImage)

        databaseHelper = DatabaseHelper(this)

        // Initialize image picker launcher
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val selectedImageUri = result.data?.data
                // Set the selected image to the ImageView
                imageProfile.setImageURI(selectedImageUri)
            }
        }
    }

    fun onSelectImageClick(view: View) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    fun onRegisterClick(view: View) {
        val username = usernameEditText.text.toString()
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        // Check if registration is valid
        if (isRegistrationValid(username, email, password)) {
            // Convert the image to a byte array
            val imageByteArray = convertImageToByteArray()

            // Call your addUser function to save the user to the database
            val result = databaseHelper.addUser(username, email, password, imageByteArray)

            if (result != -1L) {
                // Registration successful
                Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
            } else {
                // Registration failed
                Toast.makeText(this, "Registration Failed", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Registration is not valid
            Toast.makeText(this, "Invalid Registration Information", Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    private fun isRegistrationValid(username: String, email: String, password: String): Boolean {
        // Check if the email is valid
        if (!isValidEmail(email)) {
            return false
        }

        // Check if the username and email are not empty
        if (username.isEmpty() || email.isEmpty()) {
            return false
        }

        // Check if the username and email do not already exist in the database
        if (databaseHelper.isUsernameExists(username) || databaseHelper.isEmailExists(email)) {
            return false
        }

        return true
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun convertImageToByteArray(): ByteArray? {
        // Convert the image to a byte array (you need to implement this based on your requirements)
        // For demonstration purposes, let's assume you have a function that converts an image URI to a byte array
        val selectedImageUri = getSelectedImageUri()
        return uriToByteArray(selectedImageUri)
    }

    private fun getSelectedImageUri(): Uri? {
        // Retrieve the URI of the selected image from the ImageView
        val drawable = imageProfile.drawable as? BitmapDrawable
        return drawable?.bitmap?.let { bitmap ->
            val bytes = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes)
            val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "Title", null)
            Uri.parse(path)
        }
    }

    private fun uriToByteArray(uri: Uri?): ByteArray? {
        // Convert the image URI to a byte array (you need to implement this based on your requirements)
        // For demonstration purposes, let's assume you have a function that converts an image URI to a byte array
        uri?.let {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                return inputStream.readBytes()
            }
        }
        return null
    }
}

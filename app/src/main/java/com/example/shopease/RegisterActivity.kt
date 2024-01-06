package com.example.shopease

import android.annotation.SuppressLint
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
import com.example.shopease.Utils.hashPassword
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

        databaseHelper = DatabaseHelper()

        // Initialize image picker launcher
        imagePickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
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
        if (isRegistrationValid(username, email)) {
            // Convert the image to a byte array
            val imageByteArray = convertImageToByteArray()

            // Call your addUser function to save the user to the database
//            val user = User(username, email, hashPassword(password), imageByteArray)
            databaseHelper.addUser(username, email, imageByteArray, hashPassword(password)) { success ->
                if (success) {
                    Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()

                } else {
                    // Registration failed
                    Toast.makeText(this, "Registration Failed", Toast.LENGTH_SHORT).show()
                }
            }
            finish()
        }
    }

    private fun isRegistrationValid(username: String, email: String): Boolean {
        var result = true
        // Check if the email is valid
        if (!isValidEmail(email)) {
            return false
        }

        // Check if the username and email are not empty
        if (username.isEmpty() || email.isEmpty()) {
            return false
        }

        // Check if the username and email do not already exist in the database
        databaseHelper.isUsernameExists(username) { exist ->
            if (exist) {
                result = false
            }
        }
        databaseHelper.isEmailExists(email) { exist ->
            if (exist) {
                result = false
            }
        }

        return result
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun convertImageToByteArray(): ByteArray? {
        // Convert the image to a byte array (you need to implement this based on your requirements)
        // For demonstration purposes, let's assume you have a function that converts an image URI to a byte array
        val selectedImageUri = getSelectedImageUri()
        return uriToByteArray(selectedImageUri) ?: getDefaultProfileImage()
    }

    private fun getSelectedImageUri(): Uri? {
        // Retrieve the URI of the selected image from the ImageView
        val drawable = imageProfile.drawable as? BitmapDrawable
        return drawable?.bitmap?.let { bitmap ->
            val bytes = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes)
            val path =
                MediaStore.Images.Media.insertImage(contentResolver, bitmap, "Title", null)
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

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun getDefaultProfileImage(): ByteArray? {
        // Provide a default image if no profile image is selected
        val drawable = resources.getDrawable(R.drawable.profile_icon, theme)
        val bitmap = (drawable as android.graphics.drawable.BitmapDrawable).bitmap
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }
}

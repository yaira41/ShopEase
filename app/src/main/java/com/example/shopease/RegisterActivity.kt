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
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.shopease.dataClasses.User
import com.example.shopease.dbHelpers.UsersDatabaseHelper
import com.google.android.material.textfield.TextInputEditText
import java.io.ByteArrayOutputStream

class RegisterActivity : AppCompatActivity() {

    private lateinit var usernameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var imageProfile: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    private lateinit var usersDatabaseHelper: UsersDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        usernameEditText = findViewById(R.id.usernameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        imageProfile = findViewById(R.id.imageProfile)
        btnSelectImage = findViewById(R.id.btnSelectImage)

        usersDatabaseHelper = UsersDatabaseHelper(applicationContext)

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
            usersDatabaseHelper.registerUser(
                username,
                email,
                password,
                imageByteArray,
                object : UsersDatabaseHelper.RegistrationCallback {
                    override fun onRegistrationResult(success: Boolean, user: User?) {
                        if (success) {
                            Toast.makeText(
                                this@RegisterActivity,
                                "נרשמת בהצלחה.",
                                Toast.LENGTH_SHORT
                            ).show()
                            navigateToLoginActivity()
                        } else {
                            // Registration failed
                            Toast.makeText(this@RegisterActivity, "משהו השתבש", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                })


        }
    }

    private fun isRegistrationValid(username: String, email: String): Boolean {
        // Check if the email is valid
        if (!isValidEmail(email)) {
            showToast("מייל בנוי בצורה שגויה.")
            return false
        }

        // Check if the username and email are not empty
        if (username.isEmpty() || email.isEmpty()) {
            showToast("ערכים לא יכולים להיות ריקים.")
            return false
        }

        if (passwordEditText.text!!.length < 6) {
            showToast("סיסמה קצרה מידי.")
            return false
        }

        // Check if the email do not already exist in the database
        var result = true
        usersDatabaseHelper.isUsernameExists(username) { exist ->
            if (exist) {
                result = false
                showToast("היוזר כבר בשימוש")
            }
        }
        usersDatabaseHelper.isEmailExists(email) { exist ->
            if (exist) {
                result = false
                showToast("המייל כבר בשימוש")
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
        val bitmap = (drawable as BitmapDrawable).bitmap
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    private fun navigateToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

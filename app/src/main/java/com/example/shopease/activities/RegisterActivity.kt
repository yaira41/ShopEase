package com.example.shopease.activities

import android.annotation.SuppressLint
import android.content.ContentValues
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
import com.example.shopease.R
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

        if (isRegistrationValid(username, email)) {
            val imageByteArray = convertImageToByteArray()
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
        usersDatabaseHelper.isUsernameExists(username) { userExist ->
            if (userExist) {
                showToast("המשתמש כבר בשימוש")
                result = false
            } else {
                usersDatabaseHelper.isEmailExists(email) { mailExist ->
                    if (mailExist) {
                        showToast("המייל כבר בשימוש")
                        result = false
                    }
                }
            }
        }

        return result
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun convertImageToByteArray(): ByteArray? {
        val selectedImageUri = getSelectedImageUri() ?: return getDefaultProfileImage()
        return contentResolver.openInputStream(selectedImageUri)?.use { inputStream ->
            inputStream.readBytes()
        }
    }

    private fun getSelectedImageUri(): Uri? {
        // Retrieve the URI of the selected image from the ImageView
        val drawable = imageProfile.drawable as? BitmapDrawable ?: return null
        val bitmap = drawable.bitmap ?: return null
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes)

        // Create a ContentValues object with the required fields
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Title")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.WIDTH, bitmap.width)
            put(MediaStore.Images.Media.HEIGHT, bitmap.height)
        }

        // Insert the image into the media store
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?.also { uri ->
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(bytes.toByteArray())
                }
            }
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

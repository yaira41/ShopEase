package com.example.shopease

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.shopease.dbHelpers.UsersDatabaseHelper
import com.example.shopease.utils.Utils.hashPassword

class ProfileFragment : Fragment() {

    private lateinit var usernameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var imageProfileView: ImageView
    private lateinit var changePasswordButton: Button
    private lateinit var dbHelper: UsersDatabaseHelper
    private var username: String? = null
    private var email: String? = null
    private var imageProfile: ByteArray? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as BaseActivity?)?.updateTitle("Profile")
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        usernameTextView = view.findViewById(R.id.usernameTextView)
        emailTextView = view.findViewById(R.id.emailTextView)
        changePasswordButton = view.findViewById(R.id.changePasswordButton)
        imageProfileView = view.findViewById(R.id.imageProfileFragment)

        // Replace these values with the actual username and email
        username = arguments?.getString("USERNAME_KEY")
        email = arguments?.getString("EMAIL_KEY")
        imageProfile = arguments?.getByteArray("PROFILE_IMAGE_KEY")

        dbHelper = UsersDatabaseHelper()
        // Set username and email in the UI
        usernameTextView.text = "Username: $username"
        emailTextView.text = "Email: $email"
        setByteArrayImageOnImageView(imageProfile, imageProfileView)

        // Handle the change password button click
        changePasswordButton.setOnClickListener {
            showChangePasswordDialog()
        }

        return view
    }

    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val newPasswordEditText: EditText = dialogView.findViewById(R.id.newPasswordEditText)
        val confirmNewPasswordEditText: EditText =
            dialogView.findViewById(R.id.confirmNewPasswordEditText)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Change") { _, _ ->
                val newPassword = newPasswordEditText.text.toString()
                val confirmNewPassword = confirmNewPasswordEditText.text.toString()

                if (newPassword == confirmNewPassword) {
                    dbHelper.updatePassword(username.toString(),  hashPassword(newPassword)) {success ->
                        if(success) {
                            Toast.makeText(requireContext(), "Password changed successfully",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(),
                        "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun setByteArrayImageOnImageView(imageByteArray: ByteArray?, imageView: ImageView) {
        // Convert the ByteArray to a Bitmap
        val bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray!!.size)

        // Set the Bitmap on the ImageView
        imageView.setImageBitmap(bitmap)
    }
}

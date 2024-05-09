package com.example.shopease.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.shopease.activities.BaseActivity
import com.example.shopease.R
import com.example.shopease.dbHelpers.UsersDatabaseHelper
import com.example.shopease.utils.Utils
import com.example.shopease.utils.Utils.base64ToByteArray
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText

class ProfileFragment : Fragment() {

    private lateinit var usernameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var imageProfileView: ShapeableImageView
    private lateinit var changePasswordButton: Button
    private lateinit var logoutButton: Button
    private lateinit var dbHelper: UsersDatabaseHelper
    private var username: String? = null
    private var email: String? = null
    private var imageProfile: String? = null
    private val GALLERY_REQUEST_CODE = 1001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as BaseActivity?)?.updateTitle("פרופיל")
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        usernameTextView = view.findViewById(R.id.tvUsernameProfile)
        emailTextView = view.findViewById(R.id.tvEmailProfile)
        changePasswordButton = view.findViewById(R.id.changePasswordButton)
        imageProfileView = view.findViewById(R.id.imageProfileFragment)
        logoutButton = view.findViewById(R.id.btnLogout)
        // Replace these values with the actual username and email
        username = (activity as BaseActivity?)?.username!!
        email = (activity as BaseActivity?)?.user?.email
        imageProfile = (activity as BaseActivity?)?.user?.profileImage

        dbHelper = UsersDatabaseHelper(requireContext())
        // Set username and email in the UI
        usernameTextView.text = username
        emailTextView.text = email
        setByteArrayImageOnImageView(base64ToByteArray(imageProfile!!), imageProfileView)

        // Handle the change password button click
        changePasswordButton.setOnClickListener {
            showChangePasswordDialog()
        }
        imageProfileView.setOnClickListener {
            showProfileImageDialog()
        }
        logoutButton.setOnClickListener {
            dbHelper.logoutUser()
            (activity as BaseActivity).finish()
        }

        return view
    }

    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val newPasswordEditText: TextInputEditText =
            dialogView.findViewById(R.id.newPasswordEditText)
        val confirmNewPasswordEditText: TextInputEditText =
            dialogView.findViewById(R.id.confirmNewPasswordEditText)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("שנה סיסמה")
            .setView(dialogView)
            .setPositiveButton("שנה סיסמה") { _, _ ->
                val newPassword = newPasswordEditText.text.toString()
                val confirmNewPassword = confirmNewPasswordEditText.text.toString()

                if (newPassword == confirmNewPassword) {
                    dbHelper.updatePassword(newPassword, OnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                requireContext(),
                                "הסיסמה שונתה בהצלחה.", Toast.LENGTH_SHORT
                            ).show()
                          } else {
                            Toast.makeText(
                                requireContext(),
                                "משהו השתבש.", Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
                } else {
                    Toast.makeText(
                        requireContext(),
                        "הסיסמאות אינן זהות", Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("בטל", null)
            .create()

        dialog.show()
    }

    private fun setByteArrayImageOnImageView(imageByteArray: ByteArray?, imageView: ImageView) {
        val bitmap = Utils.byteArrayToBitmap(imageByteArray)
        imageView.setImageBitmap(bitmap)
    }

    private fun showProfileImageDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_profile_image, null)
        val profileImageView: ShapeableImageView = dialogView.findViewById(R.id.ivProfile)
        val changeImageButton: Button = dialogView.findViewById(R.id.changeProfileImageButton)

        setByteArrayImageOnImageView(base64ToByteArray(imageProfile!!), profileImageView)
        changeImageButton.setOnClickListener {
            openGalleryForImage()
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("תמונת פרופיל")
            .setView(dialogView)
            .setPositiveButton("סגור", null)
            .create()

        dialog.show()
    }

    private fun openGalleryForImage() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri: Uri = data.data!!
            updateImage(selectedImageUri)
        }
    }

    private fun updateImage(selectedImageUri: Uri) {
        // Convert the selected image URI to a byte array or handle it based on your logic
        val selectedImageByteArray = Utils.uriToByteArray(requireContext(), selectedImageUri)

        // Update the image in the database
        dbHelper.updateImage(username.toString(), selectedImageByteArray) { success ->
            if (success) {
                Toast.makeText(
                    requireContext(),
                    "התמונה עודכנה בהצלחה",
                    Toast.LENGTH_SHORT
                ).show()
                (activity as BaseActivity).user = dbHelper.getLocallyStoredUser()
                reloadFragment()
            } else {
                Toast.makeText(
                    requireContext(),
                    "משהו השתבש.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun reloadFragment() {
        val transaction = this.requireFragmentManager().beginTransaction()
        transaction.detach(this).attach(this).commit()
    }
}

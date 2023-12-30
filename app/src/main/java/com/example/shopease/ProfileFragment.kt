import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.shopease.R

// Inside your ProfileFragment class
class ProfileFragment : Fragment() {

    private lateinit var usernameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var changePasswordButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        usernameTextView = view.findViewById(R.id.usernameTextView)
        emailTextView = view.findViewById(R.id.emailTextView)
        changePasswordButton = view.findViewById(R.id.changePasswordButton)

        // Replace these values with the actual username and email
        val username = arguments?.getString("USERNAME_KEY")
        val email = arguments?.getString("EMAIL_KEY")

        // Set username and email in the UI
        usernameTextView.text = "Username: $username"
        emailTextView.text = "Email: $email"

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

                // Check if passwords match
                if (newPassword == confirmNewPassword) {
                    // Update password in the database (you should implement this part)
                    // For demonstration purposes, we'll just show a toast message
                    Toast.makeText(
                        requireContext(),
                        "Password changed successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }
}

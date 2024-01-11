package com.example.shopease.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.shopease.R
import com.example.shopease.dataClasses.User
import com.example.shopease.dbHelpers.UsersDatabaseHelper
import com.example.shopease.utils.Utils.base64ToByteArray
import com.example.shopease.utils.Utils.byteArrayToBitmap

class FriendSearchFragment : Fragment() {

    private lateinit var usernameEditText: EditText
    private lateinit var searchUserButton: Button
    private lateinit var resultTextView: TextView
    private lateinit var profileImageView: ImageView

    private lateinit var usersDatabaseHelper: UsersDatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_friend_search, container, false)
        usernameEditText = view.findViewById(R.id.usernameSearchEditText)
        searchUserButton = view.findViewById(R.id.searchUserButton)
        resultTextView = view.findViewById(R.id.resultSearchTextView)
        profileImageView = view.findViewById(R.id.profileImageView)

        usersDatabaseHelper = UsersDatabaseHelper()

        searchUserButton.setOnClickListener {
            searchFriendByUsername(usernameEditText.text.toString())
        }

        return view
    }

    private fun searchFriendByUsername(username: String) {
        usersDatabaseHelper.getUserByUsername(username) { foundUser ->
            if (foundUser != null) {
                displayUser(foundUser)
            } else {
                resultTextView.text = "User not found"
                profileImageView.visibility = View.GONE
            }
        }
    }

    private fun displayUser(user: User?) {
        if (user != null) {
            resultTextView.text = "Username: ${user.username}"
             val bitmap = byteArrayToBitmap(base64ToByteArray(user.profileImage))
            profileImageView.setImageBitmap(bitmap)
            profileImageView.visibility = View.VISIBLE

        }
    }
}

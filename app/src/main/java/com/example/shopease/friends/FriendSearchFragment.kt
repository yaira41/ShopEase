package com.example.shopease.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.shopease.activities.BaseActivity
import com.example.shopease.R
import com.example.shopease.dataClasses.User
import com.example.shopease.dbHelpers.RequestsDatabaseHelper
import com.example.shopease.utils.Utils.base64ToByteArray
import com.example.shopease.utils.Utils.byteArrayToBitmap
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import java.lang.Thread.sleep

class FriendSearchFragment : Fragment() {

    private lateinit var usernameEditText: TextInputEditText
    private lateinit var searchUserButton: MaterialButton
    private lateinit var resultTextView: TextView
    private lateinit var profileImageView: ShapeableImageView
    private lateinit var sendFriendRequestButton: MaterialButton
    private lateinit var requestDatabaseHelper: RequestsDatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_friend_search, container, false)
        usernameEditText = view.findViewById(R.id.usernameSearchEditText)
        searchUserButton = view.findViewById(R.id.searchUserButton)
        resultTextView = view.findViewById(R.id.resultSearchTextView)
        profileImageView = view.findViewById(R.id.profileImageView)
        sendFriendRequestButton = view.findViewById(R.id.sendFriendRequestButton)

        requestDatabaseHelper = RequestsDatabaseHelper()

        searchUserButton.setOnClickListener {
            searchFriendByUsername(usernameEditText.text.toString())
        }

        sendFriendRequestButton.setOnClickListener {
            sendFriendRequest()
        }

        return view
    }

    private fun searchFriendByUsername(username: String) {
        requestDatabaseHelper.getUserByUsername(username) { foundUser ->
            if (foundUser != null) {
                displayUser(foundUser)
            } else {
                resultTextView.text = "המשתמש אינו נמצא."
                profileImageView.visibility = View.GONE
                sendFriendRequestButton.visibility = View.GONE
            }
        }
    }

    private fun displayUser(user: User?) {
        if (user != null) {
            resultTextView.text = "Username: ${user.username}"
            val bitmap = byteArrayToBitmap(base64ToByteArray(user.profileImage))
            profileImageView.setImageBitmap(bitmap)
            profileImageView.visibility = View.VISIBLE
            sendFriendRequestButton.visibility = View.VISIBLE

        }
    }

    private fun sendFriendRequest() {
        val searchedUsername = usernameEditText.text.toString()
        val senderUsername = (activity as BaseActivity).user?.username

        if (senderUsername == searchedUsername) {
            showToast("לא ניתן לשלוח לעצמך בקשת חברות.")
            return
        }

        checkFriendshipStatus(senderUsername!!, searchedUsername)
    }

    private fun checkFriendshipStatus(senderUsername: String, searchedUsername: String) {
        requestDatabaseHelper.checkDuplicateFriendRequest(senderUsername, searchedUsername) { isDuplicate ->
            if (isDuplicate) {
                showToast("כבר קיימת במערכת בקשת חברות.")
            } else {
                requestDatabaseHelper.areFriends(senderUsername, searchedUsername) { areFriends ->
                    if (areFriends) {
                        showToast("נראה שאתם כבר חברים.")
                    } else {
                        sendFriendRequest(senderUsername, searchedUsername)
                    }
                }
            }
        }
    }

    private fun sendFriendRequest(senderUsername: String, searchedUsername: String) {
        requestDatabaseHelper.addFriendRequest(senderUsername, searchedUsername)
        showToast("נשלחה בקשת חברות.")
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}
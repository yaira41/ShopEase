package com.example.shopease.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.shopease.BaseActivity
import com.example.shopease.R
import com.example.shopease.dataClasses.FriendInfo
import com.example.shopease.dbHelpers.RequestsDatabaseHelper
import com.example.shopease.utils.Utils.byteArrayToBitmap

class ShowFriendsFragment : Fragment() {

    private lateinit var gridLayout: GridLayout
    private lateinit var username: String
    private val requestsDatabaseHelper = RequestsDatabaseHelper()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_show_friends, container, false)

        // Initialize GridLayout
        gridLayout = view.findViewById(R.id.gridLayoutFriends)

        username = (activity as BaseActivity).username!!

        // Retrieve friends for the current user with image profiles
        requestsDatabaseHelper.getFriendsWithImages(username) { friends ->
            // Update the grid layout with friends
            updateGridLayout(friends)
        }

        return view
    }

    private fun updateGridLayout(friends: List<FriendInfo>) {
        for (friend in friends) {
            val friendView = layoutInflater.inflate(R.layout.item_friend, null)

            // Set username and image profile to friendView
            val usernameTextView: TextView = friendView.findViewById(R.id.friendTextView)
            usernameTextView.text = friend.username

            val userImageView: ImageView = friendView.findViewById(R.id.friendImageView)
            val bitmap = byteArrayToBitmap(friend.imageProfileByteArray)
            userImageView.setImageBitmap(bitmap)

            // Add friendView to the GridLayout
            val params = GridLayout.LayoutParams()
            params.width = GridLayout.LayoutParams.WRAP_CONTENT
            params.height = GridLayout.LayoutParams.WRAP_CONTENT
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            friendView.layoutParams = params

            gridLayout.addView(friendView)
        }
    }
}


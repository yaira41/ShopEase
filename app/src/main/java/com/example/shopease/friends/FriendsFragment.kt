package com.example.shopease.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.example.shopease.BaseActivity
import com.example.shopease.R
import com.google.android.material.imageview.ShapeableImageView

class FriendsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as BaseActivity?)?.updateTitle("Friends")
        val view = inflater.inflate(R.layout.fragment_friends, container, false)
        val friendRequestsButton: ShapeableImageView = view.findViewById(R.id.requestFriendImgButton)
        val searchFriendButton: ShapeableImageView = view.findViewById(R.id.searchFriendImgButton)
        val myFriendsButton: ShapeableImageView = view.findViewById(R.id.myFriendsImgButton)

        searchFriendButton.setOnClickListener {
            navigateToSearchFriends(view)
        }

        friendRequestsButton.setOnClickListener {
            navigateToFriendRequests(view)
        }

        myFriendsButton.setOnClickListener {
            navigateToFriends(view)
        }

        return view
    }

    private fun navigateToFriendRequests(view: View) {
        (activity as BaseActivity?)?.loadFragment(FriendRequestsFragment())
    }

    private fun navigateToSearchFriends(view: View) {
        (activity as BaseActivity?)?.loadFragment(FriendSearchFragment())
    }

    private fun navigateToFriends(view: View) {
        (activity as BaseActivity?)?.loadFragment(ShowFriendsFragment())
    }
}

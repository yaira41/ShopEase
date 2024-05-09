package com.example.shopease.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.shopease.activities.BaseActivity
import com.example.shopease.R
import com.google.android.material.imageview.ShapeableImageView

class FriendsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        (activity as BaseActivity?)?.updateTitle("חברים")
        val view = inflater.inflate(R.layout.fragment_friends, container, false)
        val friendRequestsButton: ShapeableImageView =
            view.findViewById(R.id.requestFriendImgButton)
        val searchFriendButton: ShapeableImageView = view.findViewById(R.id.searchFriendImgButton)
        val myFriendsButton: ShapeableImageView = view.findViewById(R.id.myFriendsImgButton)

        searchFriendButton.setOnClickListener {
            navigateToFragment(FriendSearchFragment())
        }

        friendRequestsButton.setOnClickListener {
            navigateToFragment(FriendRequestsFragment())
        }

        myFriendsButton.setOnClickListener {
            navigateToFragment(ShowFriendsFragment())
        }

        return view
    }

    private fun navigateToFragment(fragment: Fragment) {
        (activity as BaseActivity?)?.loadFragment(fragment)
    }

}

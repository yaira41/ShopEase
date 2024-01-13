package com.example.shopease.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopease.BaseActivity
import com.example.shopease.R
import com.example.shopease.dataClasses.FriendRequest
import com.example.shopease.dbHelpers.RequestsDatabaseHelper

class FriendRequestsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FriendRequestsAdapter
    private lateinit var username: String
    private val requestsDatabaseHelper = RequestsDatabaseHelper()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_friend_requests, container, false)

        // Initialize RecyclerView and Adapter
        recyclerView = view.findViewById(R.id.recyclerViewFriendRequests)
        adapter = FriendRequestsAdapter(emptyList()) // Initial empty list

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        username = (activity as BaseActivity).username!!

        // Retrieve friend requests for the current user
        requestsDatabaseHelper.getFriendRequests(username) { friendRequests ->
            // Update the adapter with friend requests
            adapter.updateFriendRequests(friendRequests)
        }

        return view
    }
}


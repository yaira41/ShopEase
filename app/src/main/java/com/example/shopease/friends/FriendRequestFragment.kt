package com.example.shopease.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopease.R
import com.example.shopease.dataClasses.FriendRequest

class FriendRequestsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FriendRequestsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_friend_requests, container, false)

        // Sample friend requests data
        val friendRequests = listOf(
            FriendRequest("John", R.drawable.profile_icon),
            FriendRequest("Jane", R.drawable.profile_icon),
            // Add more friend requests as needed
        )

        // Initialize RecyclerView and Adapter
        recyclerView = view.findViewById(R.id.recyclerViewFriendRequests)
        adapter = FriendRequestsAdapter(friendRequests)

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        return view
    }
}

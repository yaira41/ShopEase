package com.example.shopease.friends

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shopease.R
import com.example.shopease.dataClasses.FriendRequest

class FriendRequestsAdapter(
    private var friendRequests: List<FriendRequest>,
    private val acceptCallback: (FriendRequest) -> Unit,
    private val ignoreCallback: (FriendRequest) -> Unit
) : RecyclerView.Adapter<FriendRequestViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendRequestViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend_request, parent, false)
        return FriendRequestViewHolder(itemView, acceptCallback, ignoreCallback)
    }

    override fun onBindViewHolder(holder: FriendRequestViewHolder, position: Int) {
        val friendRequest = friendRequests[position]
        holder.bind(friendRequest)
    }

    override fun getItemCount(): Int {
        return friendRequests.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateFriendRequests(newFriendRequests: List<FriendRequest>) {
        friendRequests = newFriendRequests
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun removeFriendRequest(friendRequest: FriendRequest) {
        friendRequests = friendRequests.filter { it != friendRequest }
        notifyDataSetChanged()
    }
}

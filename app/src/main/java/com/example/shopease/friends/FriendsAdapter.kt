package com.example.shopease.friends

// FriendsAdapter.kt

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shopease.R
import com.example.shopease.dataClasses.FriendInfo

class FriendsAdapter(private var friends: List<FriendInfo>, private val context: Context) :
    RecyclerView.Adapter<FriendViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend, parent, false)
        return FriendViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friends[position]
        holder.bind(context, friend)
    }

    override fun getItemCount(): Int {
        return friends.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateFriends(newFriends: List<FriendInfo>) {
        friends = newFriends
        notifyDataSetChanged()
    }
}

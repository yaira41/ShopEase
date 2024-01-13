package com.example.shopease.friends

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shopease.R

class FriendsAdapter(private var friends: List<String>) :
    RecyclerView.Adapter<FriendViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend, parent, false)
        return FriendViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friends[position]
        holder.bind(friend)
    }

    override fun getItemCount(): Int {
        return friends.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateFriends(newFriends: List<String>) {
        friends = newFriends
        notifyDataSetChanged()
    }
}

class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val friendTextView: TextView = itemView.findViewById(R.id.friendTextView)

    fun bind(friend: String) {
        friendTextView.text = friend
    }
}

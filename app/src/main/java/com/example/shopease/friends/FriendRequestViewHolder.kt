package com.example.shopease.friends

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shopease.R

class FriendRequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
    val userImageView: ImageView = itemView.findViewById(R.id.friendImageView)
}

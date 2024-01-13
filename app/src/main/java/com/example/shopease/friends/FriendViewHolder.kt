package com.example.shopease.friends

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shopease.R
import com.example.shopease.dataClasses.FriendInfo
import com.example.shopease.utils.Utils.byteArrayToBitmap

class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val friendImageView: ImageView = itemView.findViewById(R.id.friendImageView)
    private val friendTextView: TextView = itemView.findViewById(R.id.friendTextView)

    fun bind(context: Context, friend: FriendInfo) {
        friendTextView.text = friend.username
        // Load the image profile using the byte array
        val bitmap = byteArrayToBitmap(friend.imageProfileByteArray)
        friendImageView.setImageBitmap(bitmap)
    }
}

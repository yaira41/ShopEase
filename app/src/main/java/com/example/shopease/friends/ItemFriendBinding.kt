package com.example.shopease.friends

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.example.shopease.R

class ItemFriendBinding private constructor(view: View) {
    val imageViewFriend: ImageView = view.findViewById(R.id.imageViewShowFriend)
    val textViewUsername: TextView = view.findViewById(R.id.textViewShowUsername)

    companion object {
        fun bind(view: View): ItemFriendBinding {
            return ItemFriendBinding(view)
        }
    }
}

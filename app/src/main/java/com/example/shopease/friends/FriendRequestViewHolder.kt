package com.example.shopease.friends

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.shopease.R
import com.example.shopease.dataClasses.FriendRequest
import com.example.shopease.utils.Utils.byteArrayToBitmap

class FriendRequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
    val userImageView: ImageView = itemView.findViewById(R.id.friendImageView)

    fun bind(friendRequest: FriendRequest) {
        usernameTextView.text = friendRequest.username
        val bitmap = byteArrayToBitmap(friendRequest.imageByteArray)
        userImageView.setImageBitmap(bitmap)

        itemView.setOnClickListener {
            // Handle item click
            showFriendRequestDialog(itemView.context, friendRequest)
        }
    }

    private fun showFriendRequestDialog(context: Context, friendRequest: FriendRequest) {
        val alertDialog = AlertDialog.Builder(context)
            .setTitle("Friend Request")
            .setMessage("Do you want to accept or ignore this friend request?")
            .setPositiveButton("Accept") { dialog, _ ->
                // Handle acceptance logic
                // You can perform further actions here
                dialog.dismiss()
            }
            .setNegativeButton("Ignore") { dialog, _ ->
                // Handle ignore logic
                // You can perform further actions here
                dialog.dismiss()
            }
            .create()

        alertDialog.show()
    }
}

package com.example.shopease.friends

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.shopease.R
import com.example.shopease.dataClasses.FriendRequest

class FriendRequestsAdapter(private val friendRequests: List<FriendRequest>) :
    RecyclerView.Adapter<FriendRequestViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendRequestViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend_request, parent, false)
        return FriendRequestViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FriendRequestViewHolder, position: Int) {
        val friendRequest = friendRequests[position]
        holder.usernameTextView.text = friendRequest.username
        // Load image using a library like Glide or Picasso
        // Example using Glide:
        // Glide.with(holder.userImageView.context).load(friendRequest.imageUrl).into(holder.userImageView)

        holder.itemView.setOnClickListener {
            // Handle item click
            showFriendRequestDialog(holder.itemView.context, friendRequest)
        }
    }

    override fun getItemCount(): Int {
        return friendRequests.size
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

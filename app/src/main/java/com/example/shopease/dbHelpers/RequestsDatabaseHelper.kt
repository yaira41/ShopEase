package com.example.shopease.dbHelpers

import android.content.BroadcastReceiver
import android.util.Log
import com.example.shopease.dataClasses.FriendRequest
import com.example.shopease.dataClasses.Request
import com.example.shopease.utils.LoginCallback
import com.example.shopease.dataClasses.User
import com.example.shopease.utils.Utils.byteArrayToBase64
import com.google.firebase.database.*


class RequestsDatabaseHelper : BaseDatabaseHelper() {

    fun getUserByUsername(username: String, callback: (User?) -> Unit) {
        val usersRef = databaseReference.child("users")
        usersRef.orderByChild("username").equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userData = snapshot.children.first().getValue(User::class.java)
                        callback(userData)
                    } else {
                        callback(null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseHelper", "Error getting user by username", error.toException())
                    callback(null)
                }
            })
    }

    fun addFriendRequest(senderUsername: String, receiverUsername: String) {
        // Add the friend request to the sender's list
        val friendRequest = databaseReference.child("friendRequests")
        friendRequest.child(receiverUsername).child("senderRequests").push().setValue(senderUsername)

        // Add the friend request to the receiver's list
        friendRequest.child(senderUsername).child("receiverRequests").push().setValue(receiverUsername)
    }

    fun checkDuplicateFriendRequest(senderUsername: String, receiverUsername: String, callback: (Boolean) -> Unit) {
        // Check if senderUsername already exists in the receiver's list
        val friendRequest = databaseReference.child("friendRequests")
        friendRequest.child(receiverUsername).child("senderRequests")
            .orderByValue().equalTo(senderUsername)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Duplicate request found in receiver's list
                        callback(true)
                    } else {
                        // Check if receiverUsername already exists in the sender's list
                        friendRequest.child(senderUsername).child("receiverRequests")
                            .orderByValue().equalTo(receiverUsername)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    callback(dataSnapshot.exists())
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    // Handle errors
                                    callback(false)  // Assume no duplicate on error
                                }
                            })
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle errors
                    callback(false)  // Assume no duplicate on error
                }
            })
    }
}

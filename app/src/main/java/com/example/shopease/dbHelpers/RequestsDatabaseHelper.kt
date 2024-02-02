package com.example.shopease.dbHelpers

import android.util.Log
import com.example.shopease.dataClasses.FriendInfo
import com.example.shopease.dataClasses.FriendRequest
import com.example.shopease.dataClasses.User
import com.example.shopease.utils.Utils.base64ToByteArray
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


class RequestsDatabaseHelper : BaseDatabaseHelper() {

    fun getFriendRequests(username: String, callback: (List<FriendRequest>) -> Unit) {
        val friendRequestsReference =
            databaseReference.child("friendRequests").child(username).child("senderRequests")

        friendRequestsReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val friendRequests = mutableListOf<FriendRequest>()

                for (friendSnapshot in dataSnapshot.children) {
                    val senderUsername = friendSnapshot.value as String
                    // Assume there is a function to get the profile image URL for a given username
                    getUserImageByteArray(senderUsername) { profileImage ->
                        val friendRequest =
                            FriendRequest(senderUsername, profileImage!!)
                        friendRequests.add(friendRequest)
                        callback(friendRequests)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback(emptyList())
            }
        })
    }

    private fun getUserImageByteArray(username: String, callback: (ByteArray?) -> Unit) {
        databaseReference.child("users").orderByChild("username").equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val profileImage =
                            dataSnapshot.children.firstOrNull()?.child("profileImage")
                                ?.getValue(String::class.java)
                        callback(base64ToByteArray(profileImage!!))
                    } else {
                        // User not found or profileImage not available
                        callback(null)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle errors
                    callback(null)
                }
            })
    }
    fun getFriendsWithImages(username: String, callback: (List<FriendInfo>) -> Unit) {
        val friendsRef = databaseReference.child("confirmFriends").child(username).child("friends")
        friendsRef.orderByValue()
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val friendUsernames = dataSnapshot.children.mapNotNull { it.key }
                    val friendsWithImages = mutableListOf<FriendInfo>()
                    var completedTasks = 0

                    // Fetch images for each friend
                    for (friendUsername in friendUsernames) {
                        getUserImageByteArray(friendUsername) { imageByteArray ->
                            friendsWithImages.add(FriendInfo(friendUsername, imageByteArray))
                            completedTasks++

                            // Check if all tasks are completed
                            if (completedTasks == friendUsernames.size) {
                                callback(friendsWithImages)
                            }
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle errors
                    callback(emptyList())
                }
            })
    }

    fun getUserByUsername(username: String, callback: (User?) -> Unit) {
        databaseReference.child("users").orderByChild("username").equalTo(username)
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
        friendRequest.child(receiverUsername).child("senderRequests").push()
            .setValue(senderUsername)

        // Add the friend request to the receiver's list
        friendRequest.child(senderUsername).child("receiverRequests").push()
            .setValue(receiverUsername)
    }

    fun checkDuplicateFriendRequest(username1: String, username2: String, callback: (Boolean) -> Unit) {
        // Check if the friend request already exists in both directions
        val ref1 = databaseReference.child("friendRequests").child(username1)
        ref1.child("receiverRequests").orderByValue().equalTo(username2)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val isDuplicate1 = dataSnapshot.exists()
                    val ref2 = databaseReference.child("friendRequests").child(username2)
                    ref2.child("receiverRequests").orderByValue().equalTo(username1)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                val isDuplicate2 = dataSnapshot.exists()
                                val isDuplicate = isDuplicate1 || isDuplicate2
                                callback(isDuplicate)
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                // Handle errors
                                callback(false)
                            }
                        })
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle errors
                    callback(false)
                }
            })
    }

    fun areFriends(senderUsername: String, receiverUsername: String, callback: (Boolean) -> Unit) {
        val friendsRef = databaseReference.child("confirmFriends").child(senderUsername).child("friends")
        friendsRef.child(receiverUsername)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val areFriends = dataSnapshot.exists()
                    callback(areFriends)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle errors
                    callback(false)
                }
            })
    }

    private fun removeFriendRequest(senderUsername: String, receiverUsername: String) {
        val senderRef = databaseReference.child("friendRequests").child(senderUsername)
        senderRef.child("receiverRequests").orderByValue().equalTo(receiverUsername)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    dataSnapshot.children.firstOrNull()?.ref?.removeValue()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle errors
                }
            })

        val receiverRef = databaseReference.child("friendRequests").child(receiverUsername)
        receiverRef.child("senderRequests").orderByValue().equalTo(senderUsername)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    dataSnapshot.children.firstOrNull()?.ref?.removeValue()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle errors
                }
            })
    }

    private fun removeFriend(senderUsername: String, friendUsername: String) {
        val senderFriendsRef = databaseReference.child("confirmFriends").child(senderUsername).child("friends")
        senderFriendsRef.orderByValue().equalTo(friendUsername)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    dataSnapshot.children.firstOrNull()?.ref?.removeValue()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle errors
                }
            })
    }

    private fun addFriend(username: String, friendUsername: String) {
        val friendsRef = databaseReference.child("confirmFriends").child(username).child("friends")
        friendsRef.child(friendUsername).setValue(true)
    }

    fun confirmFriendRequest(senderUsername: String, receiverUsername: String) {
        removeFriendRequest(senderUsername, receiverUsername)

        // Add sender to receiver's friend list
        addFriend(receiverUsername, senderUsername)

        // Add receiver to sender's friend list
        addFriend(senderUsername, receiverUsername)
    }

    fun ignoreFriendRequest(senderUsername: String, receiverUsername: String) {
        removeFriendRequest(senderUsername, receiverUsername)
    }
}

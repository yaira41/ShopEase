package com.example.shopease.dataClasses

data class FriendRequest(
    val username: String = "",
    val imageUrl: Int
)

data class Request(
    val senderUsername: String = "",
    val receiverUsername: String = ""
)

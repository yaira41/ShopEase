package com.example.shopease.dataClasses

data class FriendRequest(
    val username: String = "",
    val imageByteArray: ByteArray
)

data class Request(
    val senderUsername: String = "",
    val receiverUsername: String = ""
)

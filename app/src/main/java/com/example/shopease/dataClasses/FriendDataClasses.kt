package com.example.shopease.dataClasses

data class FriendRequest(
    val username: String = "",
    val imageByteArray: ByteArray
)

data class FriendInfo(val username: String, val imageProfileByteArray: ByteArray?)
